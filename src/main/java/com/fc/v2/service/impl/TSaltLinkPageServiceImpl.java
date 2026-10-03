package com.fc.v2.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.exception.salt.SaltLinkPageRuleException;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.common.support.SaltRegionScope;
import com.fc.v2.mapper.auto.TSaltEntMapper;
import com.fc.v2.mapper.auto.TSaltLinkPageMapper;
import com.fc.v2.mapper.custom.SaltLinkBoardDao;
import com.fc.v2.model.auto.TSaltEnt;
import com.fc.v2.model.auto.TSaltLinkPage;
import com.fc.v2.model.custom.SaltLinkPageBoard;
import com.fc.v2.model.custom.SaltLinkPageForm;
import com.fc.v2.model.custom.SaltLinkPageQuery;
import com.fc.v2.model.custom.SaltLinkPageVo;
import com.fc.v2.model.custom.SaltLinkSaveResult;
import com.fc.v2.model.custom.SaltLinkScope;
import com.fc.v2.service.ITSaltLinkPageService;
import com.fc.v2.util.MD5Util;
import com.fc.v2.util.SnowflakeIdWorker;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 品种挂接页Service业务层处理。
 * 页合不合、欠挂折出几、属地该露哪几页、名录页三栏计数，全在本层定夺。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TSaltLinkPageServiceImpl extends ServiceImpl<TSaltLinkPageMapper, TSaltLinkPage> implements ITSaltLinkPageService {

    /** 进展：待挂 */
    private static final int STATUS_PENDING = 0;
    /** 进展：已挂讫 */
    private static final int STATUS_DONE = 1;
    /** 进展：压页 */
    private static final int STATUS_HELD = 2;

    /** 默认一屏条数（处里没另定时） */
    private static final int DEFAULT_LIMIT = 10;
    /** 一屏条数上限，防一次搬空 */
    private static final int MAX_LIMIT = 200;

    @Autowired
    private TSaltEntMapper saltEntMapper;

    @Autowired
    private SaltLinkBoardDao saltLinkBoardDao;

    @Autowired
    private SaltRegionScope saltRegionScope;

    @Override
    public TSaltLinkPage selectTSaltLinkPageById(Long id) {
        if (id == null) {
            return null;
        }
        return this.baseMapper.selectOne(new QueryWrapper<TSaltLinkPage>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public SaltLinkPageVo selectBoardVoById(Long id) {
        if (id == null) {
            return null;
        }
        SaltRegionScope.Scope scope = saltRegionScope.currentScope();
        String roadPrefix = scope.getLevel() == SaltRegionScope.LEVEL_PROVINCE ? null : scope.getRoadPrefix();
        // 逐页点出来也按本层属地那一路，邻县/别市的页点不进
        return saltLinkBoardDao.selectBoardById(id, roadPrefix);
    }

    @Override
    public List<TSaltEnt> rosterList() {
        // 一家一行：在册的才供开新页，摘牌企业不开新页；三类各按各的次序排。
        // 市/县只给本属地那一路的企业，页只能压在本层底下。
        SaltRegionScope.Scope scope = saltRegionScope.currentScope();
        QueryWrapper<TSaltEnt> qw = new QueryWrapper<TSaltEnt>()
                .eq("del_flag", 0)
                .eq("status", 0);
        if (scope.getLevel() != SaltRegionScope.LEVEL_PROVINCE && StringUtils.isNotEmpty(scope.getRoadPrefix())) {
            qw.apply("road_name LIKE CONCAT({0}, '%')", scope.getRoadPrefix());
        }
        qw.last("ORDER BY FIELD(site_type, '制盐企业', '省内批发企业', '跨省批发企业'), site_no, id");
        return saltEntMapper.selectList(qw);
    }

    // ------------------------------------------------------------------
    // 保存：两条来路，一处判定
    // ------------------------------------------------------------------

    @Override
    public int insertTSaltLinkPage(TSaltLinkPage record) {
        return saveTyped(record);
    }

    @Override
    public int openTSaltLinkPage(TSaltLinkPage record) {
        // 另一种起页走法，走的是同一套判定
        return saveTyped(record);
    }

    @Override
    public int updateTSaltLinkPage(TSaltLinkPage record) {
        if (record == null || record.getId() == null) {
            throw new SaltLinkPageRuleException(Arrays.asList("页主键空着，找不到要改的那一页"));
        }
        TSaltLinkPage db = this.baseMapper.selectById(record.getId());
        if (db == null || (db.getDelFlag() != null && db.getDelFlag() == 1)) {
            throw new SaltLinkPageRuleException(Arrays.asList("这一页已不在册，改不了"));
        }
        // 已挂、应挂、层级、进展以本次递交为准；代号/签认码/欠挂都不许由人改
        TSaltLinkPage merged = new TSaltLinkPage();
        merged.setId(db.getId());
        merged.setSiteId(db.getSiteId());
        merged.setShouldCount(record.getShouldCount() != null ? record.getShouldCount() : db.getShouldCount());
        merged.setDoneCount(record.getDoneCount() != null ? record.getDoneCount() : db.getDoneCount());
        merged.setNodeNo(record.getNodeNo() != null ? record.getNodeNo() : db.getNodeNo());
        merged.setStatus(record.getStatus() != null ? record.getStatus() : db.getStatus());
        merged.setContent(record.getContent() != null ? record.getContent() : db.getContent());
        merged.setRemark(record.getRemark() != null ? record.getRemark() : db.getRemark());
        return applySave(db.getSiteId(), merged, false);
    }

    /**
     * 实体口的统一判定（insert/open 共用）。
     */
    private int saveTyped(TSaltLinkPage record) {
        if (record == null) {
            throw new SaltLinkPageRuleException(Arrays.asList("页上空空的，起不了页"));
        }
        List<String> errors = new ArrayList<>();
        Integer should = record.getShouldCount();
        if (should == null) {
            errors.add("应挂品种数那一栏空着，页起不来");
        } else if (should < 0) {
            errors.add("应挂品种数不能是负数");
        }
        int done = record.getDoneCount() == null ? 0 : record.getDoneCount();
        if (record.getDoneCount() != null && done < 0) {
            errors.add("已挂品种数不能是负数");
        }
        if (should != null && done > should) {
            errors.add("已挂品种数(" + done + ")高过应挂品种数(" + should + ")，进不了库");
        }
        if (record.getSiteId() == null) {
            errors.add("所对定点企业空着，页起不来");
        }
        if (record.getNodeNo() == null || record.getNodeNo() < 0 || record.getNodeNo() > 2) {
            errors.add("适用区域层级序只许取 0省、1市、2县");
        }
        if (!errors.isEmpty()) {
            throw new SaltLinkPageRuleException(errors);
        }
        record.setDoneCount(done);
        return applySave(record.getSiteId(), record, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SaltLinkSaveResult submitForm(SaltLinkPageForm form, SaltLinkScope scope) {
        if (form == null) {
            throw new SaltLinkPageRuleException(Arrays.asList("页上空空的，起不了页"));
        }
        List<String> errors = new ArrayList<>();
        boolean isEdit = StringUtils.isNotEmpty(form.getId());

        // —— 应挂：空着递、带小数、负数，逐栏回执 ——
        Integer should = parseIntCount(form.getShouldCount(), "应挂品种数", errors);
        if (should != null && should < 0) {
            errors.add("应挂品种数不能是负数");
        }
        // —— 已挂：空着按 0；带小数、负数、高过应挂都拦 ——
        String doneRaw = form.getDoneCount();
        int done;
        if (StringUtils.isEmpty(doneRaw)) {
            done = 0;
        } else {
            Integer parsed = parseIntCount(doneRaw, "已挂品种数", errors);
            done = parsed == null ? 0 : parsed;
            if (parsed != null && parsed < 0) {
                errors.add("已挂品种数不能是负数");
            }
        }
        if (should != null && done > should) {
            errors.add("已挂品种数(" + done + ")高过应挂品种数(" + should + ")，进不了库");
        }

        // —— 层级序 ——
        Integer nodeNo = null;
        if (StringUtils.isEmpty(form.getNodeNo())) {
            errors.add("适用区域层级序那一栏空着，页起不来");
        } else {
            Integer parsedNode = parseIntCount(form.getNodeNo(), "适用区域层级序", errors);
            if (parsedNode != null && (parsedNode < 0 || parsedNode > 2)) {
                errors.add("适用区域层级序只许取 0省、1市、2县");
            } else {
                nodeNo = parsedNode;
            }
        }

        // —— 所对企业：起页必填；摘了牌的不再开新页 ——
        TSaltEnt ent = null;
        Integer siteId = null;
        if (isEdit) {
            Long editId;
            try {
                editId = Long.valueOf(form.getId().trim());
            } catch (NumberFormatException e) {
                throw new SaltLinkPageRuleException(Arrays.asList("页主键不对，找不到要改的那一页"));
            }
            TSaltLinkPage db = this.baseMapper.selectById(editId);
            if (db == null || (db.getDelFlag() != null && db.getDelFlag() == 1)) {
                throw new SaltLinkPageRuleException(Arrays.asList("这一页已不在册，改不了"));
            }
            siteId = db.getSiteId();
            ent = saltEntMapper.selectById(siteId);
        } else {
            if (StringUtils.isEmpty(form.getSiteId())) {
                errors.add("所对定点企业那一栏空着，页起不来");
            } else {
                try {
                    siteId = Integer.valueOf(form.getSiteId().trim());
                } catch (NumberFormatException e) {
                    errors.add("所对定点企业的代号对不上名录");
                }
                if (siteId != null) {
                    ent = saltEntMapper.selectOne(new QueryWrapper<TSaltEnt>()
                            .eq("id", siteId).eq("del_flag", 0));
                    if (ent == null) {
                        errors.add("所对定点企业不在企业总名录里");
                    } else if (ent.getStatus() != null && ent.getStatus() == 1) {
                        errors.add("该企业已摘牌，不再开新页；它早前的页照旧在册可查");
                    }
                }
            }
        }

        // —— 进展：只许递 待挂/压页；已挂讫由欠挂落 ——
        Integer wantStatus = STATUS_PENDING;
        if (StringUtils.isNotEmpty(form.getStatus())) {
            Integer st = parseIntCount(form.getStatus(), "进展", errors);
            if (st != null && st != STATUS_PENDING && st != STATUS_HELD) {
                errors.add("进展这一栏只许递待挂(0)或压页(2)，已挂讫由系统按欠挂落");
            } else if (st != null) {
                wantStatus = st;
            }
        }

        if (!errors.isEmpty()) {
            throw new SaltLinkPageRuleException(errors);
        }

        TSaltLinkPage record = new TSaltLinkPage();
        record.setShouldCount(should);
        record.setDoneCount(done);
        record.setNodeNo(nodeNo);
        record.setStatus(wantStatus);
        record.setContent(form.getContent());
        record.setRemark(form.getRemark());
        if (isEdit) {
            record.setId(Long.valueOf(form.getId().trim()));
        }

        int rows = applySave(siteId, record, !isEdit);
        if (rows <= 0) {
            throw new SaltLinkPageRuleException(Arrays.asList("落库没成，页没起成"));
        }
        return SaltLinkSaveResult.ok(record.getId(), record.getBillNo(), record.getSignCode(), record.getLackCount());
    }

    /**
     * 一处判定的最后关口：属地核验、企业旧档核对、欠挂折数、进展落档、系统发码、写回页上。
     * 起页与改页都从这里过。
     */
    private int applySave(Integer siteId, TSaltLinkPage record, boolean creating) {
        // 最后关口再守一遍数：实体口（insert/open 旧签名）也不许放行进库
        List<String> guard = new ArrayList<>();
        if (record.getShouldCount() == null) {
            guard.add("应挂品种数那一栏空着，页起不来");
        } else if (record.getShouldCount() < 0) {
            guard.add("应挂品种数不能是负数");
        }
        int doneIn = record.getDoneCount() == null ? 0 : record.getDoneCount();
        if (record.getDoneCount() != null && record.getDoneCount() < 0) {
            guard.add("已挂品种数不能是负数");
        }
        if (record.getShouldCount() != null && doneIn > record.getShouldCount()) {
            guard.add("已挂品种数(" + doneIn + ")高过应挂品种数(" + record.getShouldCount() + ")，进不了库");
        }
        if (record.getNodeNo() == null || record.getNodeNo() < 0 || record.getNodeNo() > 2) {
            guard.add("适用区域层级序只许取 0省、1市、2县");
        }
        if (!guard.isEmpty()) {
            throw new SaltLinkPageRuleException(guard);
        }

        TSaltEnt ent = saltEntMapper.selectOne(new QueryWrapper<TSaltEnt>()
                .eq("id", siteId).eq("del_flag", 0));
        if (ent == null) {
            throw new SaltLinkPageRuleException(Arrays.asList("所对定点企业不在企业总名录里"));
        }
        if (creating && ent.getStatus() != null && ent.getStatus() == 1) {
            throw new SaltLinkPageRuleException(Arrays.asList(
                    "该企业已摘牌，不再开新页；它早前的页照旧在册可查"));
        }

        // 页压在属地那一层底下：市/县起的页，所对企业必须在自己属地那一路
        SaltRegionScope.Scope scope = saltRegionScope.currentScope();
        if (scope.getLevel() != SaltRegionScope.LEVEL_PROVINCE
                && StringUtils.isNotEmpty(scope.getRoadPrefix())
                && (ent.getRoadName() == null || !ent.getRoadName().startsWith(scope.getRoadPrefix()))) {
            throw new SaltLinkPageRuleException(Arrays.asList(
                    "所对企业属地(" + ent.getRoadName() + ")不在本层这一路，页压不到本层底下"));
        }

        int should = record.getShouldCount();
        int done = record.getDoneCount() == null ? 0 : record.getDoneCount();
        int lack = should - done;

        // 进展：压页之外，欠挂为零即已挂讫，有余欠即待挂——两个来路同一落法
        int status;
        if (record.getStatus() != null && record.getStatus() == STATUS_HELD) {
            status = STATUS_HELD;
        } else {
            status = lack == 0 ? STATUS_DONE : STATUS_PENDING;
        }

        record.setSiteId(ent.getId().intValue());
        record.setSiteNo(ent.getSiteNo());
        record.setLackCount(lack);
        record.setStatus(status);
        // 欠挂栏只许看不许改、代号与签认码只由系统发——人手递上来的值一律覆盖
        record.setBillNo(null);
        record.setSignCode(null);

        if (creating) {
            record.setId(Long.valueOf(SnowflakeIdWorker.getUUID()));
            String billNo = issueBillNo(record.getId());
            record.setBillNo(billNo);
            record.setSignCode(issueSignCode(billNo));
            record.setDelFlag(0);
            record.setCreateTime(new Date());
            int rows = this.baseMapper.insert(record);
            return rows;
        }

        UpdateWrapper<TSaltLinkPage> uw = new UpdateWrapper<TSaltLinkPage>()
                .eq("id", record.getId())
                .eq("del_flag", 0)
                .set("site_id", record.getSiteId())
                .set("site_no", record.getSiteNo())
                .set("node_no", record.getNodeNo())
                .set("should_count", should)
                .set("done_count", done)
                .set("lack_count", lack)
                .set("status", status)
                .set("content", record.getContent())
                .set("remark", record.getRemark())
                .set("update_time", new Date());
        return this.baseMapper.update(null, uw);
    }

    /** 挂接代号：GJ + 雪花码（系统发） */
    private String issueBillNo(Long id) {
        return "GJ" + Long.toHexString(id).toUpperCase();
    }

    /** 随行签认码：挂接代号加盐取 MD5 前 12 位（系统发） */
    private String issueSignCode(String billNo) {
        return MD5Util.encode(billNo + "|salt-link-sign|").substring(0, 12).toUpperCase();
    }

    /**
     * 把品种数原始串折成整数：空、带小数/非数字都逐栏回执，不放行进库。
     */
    private Integer parseIntCount(String raw, String column, List<String> errors) {
        if (StringUtils.isEmpty(raw)) {
            errors.add(column + "那一栏空着，页起不来");
            return null;
        }
        String s = raw.trim();
        if (!s.matches("-?\\d+")) {
            errors.add(column + "只许填整数，\"" + raw + "\"带小数或不是数，进不了库");
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            errors.add(column + "的数大得放不下，进不了库");
            return null;
        }
    }

    // ------------------------------------------------------------------
    // 名录页：属地该露哪几页、三栏计数，同一次算
    // ------------------------------------------------------------------

    @Override
    public SaltLinkPageBoard loadBoard(SaltLinkPageQuery query) {
        SaltLinkPageQuery q = query == null ? new SaltLinkPageQuery() : query;
        SaltRegionScope.Scope scope = saltRegionScope.currentScope();
        String roadPrefix = scope.getLevel() == SaltRegionScope.LEVEL_PROVINCE ? null : scope.getRoadPrefix();

        int page = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int limit = q.getLimit() == null || q.getLimit() < 1 ? DEFAULT_LIMIT : Math.min(q.getLimit(), MAX_LIMIT);
        int offset = (page - 1) * limit;

        // 三栏计数与下面的行用同一套 where：一次算清，不许各摆一处
        List<Map<String, Object>> groups = saltLinkBoardDao.countBoard(
                roadPrefix, trimToNull(q.getBillNo()), trimToNull(q.getSiteNo()), trimToNull(q.getSiteType()));
        long total = 0;
        long done = 0;
        long held = 0;
        for (Map<String, Object> g : groups) {
            Object stObj = g.get("status");
            long cnt = ((Number) g.get("cnt")).longValue();
            total += cnt;
            int st = stObj == null ? -1 : ((Number) stObj).intValue();
            if (st == STATUS_DONE) {
                done = cnt;
            } else if (st == STATUS_HELD) {
                held = cnt;
            }
        }

        List<SaltLinkPageVo> rows;
        if (total == 0) {
            // 本层这一季没起过页，这一屏就空着，绝不拿旁处的行凑数
            rows = new ArrayList<>();
        } else {
            rows = saltLinkBoardDao.selectBoard(
                    roadPrefix, trimToNull(q.getBillNo()), trimToNull(q.getSiteNo()), trimToNull(q.getSiteType()),
                    offset, limit);
        }

        SaltLinkPageBoard board = new SaltLinkPageBoard();
        board.setTotalCount(total);
        board.setDoneCount(done);
        board.setHeldCount(held);
        board.setRows(rows);
        return board;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // ------------------------------------------------------------------
    // 删除：软删，旧页随摘牌/删除仍有据可查的口径不变
    // ------------------------------------------------------------------

    @Override
    public int deleteTSaltLinkPageByIds(String ids) {
        Long[] idArr = ConvertUtil.toLongArray(ids);
        if (idArr.length == 0) {
            return 0;
        }
        UpdateWrapper<TSaltLinkPage> uw = new UpdateWrapper<TSaltLinkPage>()
                .in("id", Arrays.asList(idArr))
                .set("del_flag", 1)
                .set("update_time", new Date());
        return this.baseMapper.update(null, uw);
    }

    @Override
    public int deleteTSaltLinkPageById(Long id) {
        if (id == null) {
            return 0;
        }
        UpdateWrapper<TSaltLinkPage> uw = new UpdateWrapper<TSaltLinkPage>()
                .eq("id", id)
                .set("del_flag", 1)
                .set("update_time", new Date());
        return this.baseMapper.update(null, uw);
    }
}
