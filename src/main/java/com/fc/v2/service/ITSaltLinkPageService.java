package com.fc.v2.service;

import com.fc.v2.model.auto.TSaltEnt;
import com.fc.v2.model.auto.TSaltLinkPage;
import com.fc.v2.model.custom.SaltLinkPageBoard;
import com.fc.v2.model.custom.SaltLinkPageForm;
import com.fc.v2.model.custom.SaltLinkPageQuery;
import com.fc.v2.model.custom.SaltLinkSaveResult;
import com.fc.v2.model.custom.SaltLinkScope;

import java.util.List;

/**
 * 企业品种挂接簿页 Service接口。
 * 一页合不合、欠挂品种数折出几、按属地该露哪几页、名录页三栏那三个数，
 * 都由本服务一家定夺，接口层与屏上不另摆一处能躲开它的判路。
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITSaltLinkPageService {

    /** 按主键查询（在册页） */
    TSaltLinkPage selectTSaltLinkPageById(Long id);

    /** 按主键取名录页一行（含企业代号、全称、类别、属地），在册页才取得到 */
    com.fc.v2.model.custom.SaltLinkPageVo selectBoardVoById(Long id);

    /**
     * 企业总名录（一家一行）：只给在册、未删的企业，按制盐/省内批发/跨省批发各按各的次序。
     * 摘了牌的不在其列——不再为它开新页；它早前的页在名录页上照旧在。
     */
    List<TSaltEnt> rosterList();

    /**
     * 起页保存（来路一）——原签名维持不动。
     * 挂接代号、随行签认码由系统发；欠挂品种数由系统折回写页上；
     * 不合规抛 SaltLinkPageRuleException，页起不来，回执逐栏点明毛病。
     */
    int insertTSaltLinkPage(TSaltLinkPage record);

    /**
     * 起页保存（来路二）——另起的同义方法，接另一种起页走法，
     * 与来路一共用同一套判定，屏上不长第二种结果。
     */
    int openTSaltLinkPage(TSaltLinkPage record);

    /**
     * 改页保存——原签名维持不动。
     * 已挂数一变、换了挂靠那一层，都在此重算欠挂并写回；欠挂栏只许看不许改。
     */
    int updateTSaltLinkPage(TSaltLinkPage record);

    /**
     * 起页/改页表单的唯一起判口：两条来路（FORM/OPEN）都汇到这里。
     * 应挂、已挂以原始串承接，空着递、带小数、负数、高过应挂都逐栏回执。
     */
    SaltLinkSaveResult submitForm(SaltLinkPageForm form, SaltLinkScope scope);

    /**
     * 名录页一屏：本层属地该露的页，三栏计数与行出自同一次算。
     * 本层没起过页就空着，不拿旁处的行凑数；库里在册的每一页都找得着。
     */
    SaltLinkPageBoard loadBoard(SaltLinkPageQuery query);

    /** 批量删除（软删，在册页数随之不再计它） */
    int deleteTSaltLinkPageByIds(String ids);

    /** 按主键删除（软删） */
    int deleteTSaltLinkPageById(Long id);
}
