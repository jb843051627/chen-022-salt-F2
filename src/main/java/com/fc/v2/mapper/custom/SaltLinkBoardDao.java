package com.fc.v2.mapper.custom;

import com.fc.v2.model.custom.SaltLinkPageVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 挂接簿页名录查询：页贴企业总名录（t_salt_link_page × t_salt_ent）。
 * 列表与三栏计数共用同一套 WHERE，保证名录页上的数与逐页点出来的出自同一次算。
 *
 * @author fuce
 */
public interface SaltLinkBoardDao {

    /**
     * 名录页一屏。
     *
     * @param roadPrefix 本层属地前缀（省—市—县一路），省级给 null 即全省
     * @param billNo     挂接代号片段
     * @param siteNo     企业代号片段
     * @param siteType   企业类别片段
     * @param offset     起算行
     * @param limit      一屏条数
     */
    List<SaltLinkPageVo> selectBoard(@Param("roadPrefix") String roadPrefix,
                                     @Param("billNo") String billNo,
                                     @Param("siteNo") String siteNo,
                                     @Param("siteType") String siteType,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /** 按主键取名录页一行（在册页），含企业总名录字段。 */
    SaltLinkPageVo selectBoardById(@Param("id") Long id, @Param("roadPrefix") String roadPrefix);

    /**
     * 同一套 WHERE 下按进展分组计数：每行 status、cnt。
     */
    List<Map<String, Object>> countBoard(@Param("roadPrefix") String roadPrefix,
                                         @Param("billNo") String billNo,
                                         @Param("siteNo") String siteNo,
                                         @Param("siteType") String siteType);
}
