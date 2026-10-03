package com.fc.v2.common.support;

import com.fc.v2.mapper.auto.TSysDepartmentMapper;
import com.fc.v2.model.auto.TSysDepartment;
import com.fc.v2.model.auto.TSysUser;
import com.fc.v2.shiro.util.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 登录人属地那一路：沿部门树自本部门走到根，按深度定层级——
 * 根=省级（全省都露），根下一层=市级，再下一层=县级，更深的按县算。
 * 露页前缀取根以下各层部门名自上而下以“—”相接，与企业总名录“省—市—县”那一路对齐：
 * 市属部门名即市段，县属部门再续县段。前缀在名录里对不上行时，这一屏就空着，不凑数。
 *
 * @author fuce
 */
@Component
public class SaltRegionScope {

    /** 省 */
    public static final int LEVEL_PROVINCE = 0;
    /** 市 */
    public static final int LEVEL_CITY = 1;
    /** 县 */
    public static final int LEVEL_COUNTY = 2;

    /** 路段分隔符（与 t_salt_ent.road_name 的写法一致） */
    public static final String ROAD_SEP = "—";

    @Autowired
    private TSysDepartmentMapper departmentMapper;

    public static class Scope {
        private final int level;
        private final String roadPrefix;

        Scope(int level, String roadPrefix) {
            this.level = level;
            this.roadPrefix = roadPrefix;
        }

        /** 0省 1市 2县 */
        public int getLevel() {
            return level;
        }

        /** 本层可见的属地前缀；省级为 null（全省） */
        public String getRoadPrefix() {
            return roadPrefix;
        }
    }

    /**
     * 取当前登录人的属地可见范围。取不到部门时按省级算，不把别家的行多漏出来的前提是部门链齐全。
     */
    public Scope currentScope() {
        TSysUser user;
        try {
            user = ShiroUtils.getUser();
        } catch (RuntimeException e) {
            return new Scope(LEVEL_PROVINCE, null);
        }
        if (user == null || user.getDepId() == null) {
            return new Scope(LEVEL_PROVINCE, null);
        }

        // 自本部门向上收集，收完翻成根在前
        List<String> topDown = new ArrayList<>();
        Long depId = user.getDepId();
        int guard = 0;
        while (depId != null && guard++ < 32) {
            TSysDepartment dept = departmentMapper.selectById(depId);
            if (dept == null) {
                break;
            }
            topDown.add(dept.getDeptName());
            depId = (dept.getParentId() == null || dept.getParentId() == 0L) ? null : dept.getParentId();
        }
        Collections.reverse(topDown);
        if (topDown.isEmpty()) {
            return new Scope(LEVEL_PROVINCE, null);
        }

        // 根为省级本身；根下第1层市、第2层县
        int level = Math.min(topDown.size() - 1, LEVEL_COUNTY);
        if (level == LEVEL_PROVINCE) {
            return new Scope(LEVEL_PROVINCE, null);
        }
        // 前缀从省段接起：市=省—市；县=省—市—县，与 road_name 逐段对齐
        int end = Math.min(topDown.size(), level + 1);
        String prefix = String.join(ROAD_SEP, topDown.subList(0, end));
        return new Scope(level, prefix);
    }
}
