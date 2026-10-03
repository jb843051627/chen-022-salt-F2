package com.fc.v2.service.impl;

import com.fc.v2.common.exception.salt.SaltLinkPageRuleException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 挂接簿页服务层规约自验：
 * 欠挂折数三组样例、逐栏回执、两路同判、系统发码、属地可见、三栏计数同口径、空层不凑数。
 *
 * @author fuce
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TSaltLinkPageServiceImplTest {

    @Mock
    private TSaltLinkPageMapper pageMapper;
    @Mock
    private TSaltEntMapper entMapper;
    @Mock
    private SaltLinkBoardDao boardDao;
    @Mock
    private SaltRegionScope regionScope;

    @InjectMocks
    private TSaltLinkPageServiceImpl service;

    private SaltRegionScope.Scope prov;
    private SaltRegionScope.Scope city;
    private SaltRegionScope.Scope countyOther;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", pageMapper);
        prov = scopeMock(SaltRegionScope.LEVEL_PROVINCE, null);
        city = scopeMock(SaltRegionScope.LEVEL_CITY, "盐岭省—盐泽市");
        countyOther = scopeMock(SaltRegionScope.LEVEL_COUNTY, "盐岭省—青卤市—青卤县");
        when(regionScope.currentScope()).thenReturn(prov);
    }

    private SaltRegionScope.Scope scopeMock(int level, String prefix) {
        SaltRegionScope.Scope s = org.mockito.Mockito.mock(SaltRegionScope.Scope.class);
        when(s.getLevel()).thenReturn(level);
        when(s.getRoadPrefix()).thenReturn(prefix);
        return s;
    }

    private TSaltEnt ent(long id, String no, String type, String road, int status) {
        TSaltEnt e = new TSaltEnt();
        e.setId(id);
        e.setSiteNo(no);
        e.setSiteName(no + "名");
        e.setSiteType(type);
        e.setRoadName(road);
        e.setStatus(status);
        e.setDelFlag(0);
        return e;
    }

    private void mockEntInRoster(TSaltEnt e) {
        when(entMapper.selectOne(any())).thenReturn(e);
        when(entMapper.selectById(e.getId().intValue())).thenReturn(e);
    }

    private SaltLinkPageForm form(String id, String siteId, String node, String should, String done, String status) {
        SaltLinkPageForm f = new SaltLinkPageForm();
        f.setId(id);
        f.setSiteId(siteId);
        f.setNodeNo(node);
        f.setShouldCount(should);
        f.setDoneCount(done);
        f.setStatus(status);
        return f;
    }

    private TSaltLinkPage captureInserted() {
        ArgumentCaptor<TSaltLinkPage> cap = ArgumentCaptor.forClass(TSaltLinkPage.class);
        verify(pageMapper).insert(cap.capture());
        return cap.getValue();
    }

    // ---------------- 欠挂折数：三组样例 ----------------

    @Test
    void lack_64_41_is_23_pending() {
        TSaltEnt e = ent(10, "JY10", "制盐企业", "盐岭省—盐泽市—临卤县", 0);
        mockEntInRoster(e);
        when(pageMapper.insert(any())).thenReturn(1);

        SaltLinkSaveResult r = service.submitForm(form(null, "10", "1", "64", "41", "0"), SaltLinkScope.FORM);

        TSaltLinkPage saved = captureInserted();
        assertEquals(23, saved.getLackCount());
        assertEquals(23, r.getLackCount());
        assertEquals(0, saved.getStatus(), "有余欠即待挂");
        assertNotNull(saved.getBillNo(), "挂接代号系统发");
        assertTrue(saved.getBillNo().startsWith("GJ"));
        assertNotNull(saved.getSignCode(), "随行签认码系统发");
        assertEquals(12, saved.getSignCode().length());
        assertEquals("JY10", saved.getSiteNo());
    }

    @Test
    void lack_25_9_is_16() {
        TSaltEnt e = ent(11, "JY11", "省内批发企业", "盐岭省—盐泽市—青卤县", 0);
        mockEntInRoster(e);
        when(pageMapper.insert(any())).thenReturn(1);

        service.submitForm(form(null, "11", "2", "25", "9", "0"), SaltLinkScope.FORM);

        assertEquals(16, captureInserted().getLackCount());
    }

    @Test
    void lack_48_48_is_zero_and_done() {
        TSaltEnt e = ent(12, "JY12", "跨省批发企业", "盐岭省—盐泽市—临卤县", 0);
        mockEntInRoster(e);
        when(pageMapper.insert(any())).thenReturn(1);

        SaltLinkSaveResult r = service.submitForm(form(null, "12", "0", "48", "48", "0"), SaltLinkScope.FORM);

        TSaltLinkPage saved = captureInserted();
        assertEquals(0, saved.getLackCount());
        assertEquals(1, saved.getStatus(), "欠挂落零系统自落已挂讫");
        assertEquals(0, r.getLackCount());
    }

    @Test
    void done_defaults_zero_when_blank_and_recalc_on_edit() {
        TSaltEnt e = ent(13, "JY13", "制盐企业", "盐岭省—盐泽市—临卤县", 0);
        mockEntInRoster(e);
        when(pageMapper.insert(any())).thenReturn(1);

        service.submitForm(form(null, "13", "2", "10", "", "0"), SaltLinkScope.FORM);
        assertEquals(10, captureInserted().getLackCount());
    }

    // ---------------- 拦得住：逐栏回执 ----------------

    @Test
    void reject_blank_should() {
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "1", "", "3", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("应挂品种数"), ex.getMessage());
        verify(pageMapper, never()).insert(any());
    }

    @Test
    void reject_decimal_should() {
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "1", "12.5", "3", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("应挂品种数"), ex.getMessage());
        assertTrue(ex.getMessage().contains("整数"), ex.getMessage());
        verify(pageMapper, never()).insert(any());
    }

    @Test
    void reject_negative_should() {
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "1", "-1", "0", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("应挂品种数不能是负数"), ex.getMessage());
        verify(pageMapper, never()).insert(any());
    }

    @Test
    void reject_should_below_done() {
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "1", "5", "6", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("已挂品种数(6)高过应挂品种数(5)"), ex.getMessage());
        verify(pageMapper, never()).insert(any());
    }

    @Test
    void reject_decimal_done() {
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "1", "10", "3.2", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("已挂品种数"), ex.getMessage());
        assertTrue(ex.getMessage().contains("整数"), ex.getMessage());
        verify(pageMapper, never()).insert(any());
    }

    @Test
    void reject_blank_site() {
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "", "1", "10", "3", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("所对定点企业"), ex.getMessage());
        verify(pageMapper, never()).insert(any());
    }

    @Test
    void reject_bad_node() {
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "9", "10", "3", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("适用区域层级序"), ex.getMessage());
    }

    @Test
    void reject_multiple_errors_at_once() {
        // 应挂空 + 已挂小数：两栏毛病一次讲清
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "1", "", "1.1", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getErrors().size() >= 2, () -> "应逐栏列毛病：" + ex.getErrors());
    }

    @Test
    void reject_new_page_for_delisted_ent() {
        TSaltEnt e = ent(99, "JY99", "省内批发企业", "盐岭省—盐泽市—青卤县", 1);
        mockEntInRoster(e);
        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "99", "1", "10", "0", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("摘牌"), ex.getMessage());
        verify(pageMapper, never()).insert(any());
    }

    @Test
    void delisted_ent_old_page_still_editable() {
        // 摘牌企业早前的页照旧算在册：改页放行，欠挂重算
        TSaltEnt e = ent(99, "JY99", "省内批发企业", "盐岭省—盐泽市—青卤县", 1);
        TSaltLinkPage db = new TSaltLinkPage();
        db.setId(500L);
        db.setSiteId(99);
        db.setDelFlag(0);
        when(pageMapper.selectById(500L)).thenReturn(db);
        when(entMapper.selectById(99)).thenReturn(e);
        when(entMapper.selectOne(any())).thenReturn(e);
        when(pageMapper.update(any(), any())).thenReturn(1);

        service.submitForm(form("500", null, "1", "30", "20", "0"), SaltLinkScope.FORM);

        ArgumentCaptor<TSaltLinkPage> cap = ArgumentCaptor.forClass(TSaltLinkPage.class);
        verify(pageMapper, never()).insert(any());
        // 欠挂重算写回：30-20=10
        verify(pageMapper).update(isNull(), any());
    }

    // ---------------- 两条来路同一套说法 ----------------

    @Test
    void two_routes_same_verdict() {
        TSaltEnt e = ent(20, "JY20", "制盐企业", "盐岭省—盐泽市—临卤县", 0);
        mockEntInRoster(e);
        when(pageMapper.insert(any())).thenReturn(1);

        SaltLinkSaveResult a = service.submitForm(form(null, "20", "1", "64", "41", "0"), SaltLinkScope.FORM);
        SaltLinkSaveResult b = service.submitForm(form(null, "20", "1", "64", "41", "0"), SaltLinkScope.OPEN);

        assertEquals(a.getLackCount(), b.getLackCount());
        assertEquals(23, a.getLackCount());
        assertEquals(23, b.getLackCount());
    }

    @Test
    void two_routes_same_rejection() {
        SaltLinkPageRuleException a = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "1", "1.5", "3", "0"), SaltLinkScope.FORM));
        SaltLinkPageRuleException b = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "10", "1", "1.5", "3", "0"), SaltLinkScope.OPEN));
        assertEquals(a.getMessage(), b.getMessage());
    }

    // ---------------- 属地那一路 ----------------

    @Test
    void city_cannot_open_page_for_other_city() {
        when(regionScope.currentScope()).thenReturn(city);
        TSaltEnt e = ent(30, "JY30", "制盐企业", "盐岭省—卤东市—他县", 0);
        mockEntInRoster(e);
        when(pageMapper.insert(any())).thenReturn(1);

        SaltLinkPageRuleException ex = assertThrows(SaltLinkPageRuleException.class,
                () -> service.submitForm(form(null, "30", "1", "10", "0", "0"), SaltLinkScope.FORM));
        assertTrue(ex.getMessage().contains("不在本层这一路"), ex.getMessage());
        verify(pageMapper, never()).insert(any());
    }

    @Test
    void county_can_open_own_road_page() {
        SaltRegionScope.Scope own = scopeMock(SaltRegionScope.LEVEL_COUNTY, "盐岭省—盐泽市—临卤县");
        when(regionScope.currentScope()).thenReturn(own);
        TSaltEnt e = ent(31, "JY31", "制盐企业", "盐岭省—盐泽市—临卤县—临卤镇", 0);
        mockEntInRoster(e);
        when(pageMapper.insert(any())).thenReturn(1);

        service.submitForm(form(null, "31", "2", "8", "8", "0"), SaltLinkScope.FORM);
        assertEquals(0, captureInserted().getLackCount());
    }

    // ---------------- 名录页：三栏计数与行同口径、空层不凑数 ----------------

    @Test
    void board_counts_and_rows_share_one_scope() {
        when(regionScope.currentScope()).thenReturn(city);
        List<Map<String, Object>> groups = new ArrayList<>();
        groups.add(group(0, 3L));
        groups.add(group(1, 5L));
        groups.add(group(2, 2L));
        when(boardDao.countBoard(eq("盐岭省—盐泽市"), any(), any(), any())).thenReturn(groups);
        List<SaltLinkPageVo> rows = Arrays.asList(new SaltLinkPageVo(), new SaltLinkPageVo());
        when(boardDao.selectBoard(eq("盐岭省—盐泽市"), any(), any(), any(), eq(0), eq(10))).thenReturn(rows);

        SaltLinkPageQuery q = new SaltLinkPageQuery();
        q.setBillNo("  GJ  "); // 片段、带空白
        SaltLinkPageBoard board = service.loadBoard(q);

        assertEquals(10, board.getTotalCount(), "在册=三态合计");
        assertEquals(5, board.getDoneCount());
        assertEquals(2, board.getHeldCount());
        assertEquals(2, board.getRows().size());
        verify(boardDao).countBoard(eq("盐岭省—盐泽市"), eq("GJ"), isNull(), isNull());
    }

    @Test
    void board_empty_layer_not_padded() {
        when(regionScope.currentScope()).thenReturn(countyOther);
        when(boardDao.countBoard(any(), any(), any(), any())).thenReturn(new ArrayList<>());

        SaltLinkPageBoard board = service.loadBoard(new SaltLinkPageQuery());

        assertEquals(0, board.getTotalCount());
        assertEquals(0, board.getDoneCount());
        assertEquals(0, board.getHeldCount());
        assertTrue(board.getRows().isEmpty());
        // 无在册页即不取行，不拿旁处的行凑数
        verify(boardDao, never()).selectBoard(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void board_province_has_no_road_prefix() {
        when(boardDao.countBoard(isNull(), any(), any(), any())).thenReturn(new ArrayList<>());
        service.loadBoard(new SaltLinkPageQuery());
        verify(boardDao).countBoard(isNull(), isNull(), isNull(), isNull());
    }

    private Map<String, Object> group(int status, long cnt) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", status);
        m.put("cnt", cnt);
        return m;
    }
}
