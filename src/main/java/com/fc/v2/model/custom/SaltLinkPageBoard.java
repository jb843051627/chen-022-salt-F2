package com.fc.v2.model.custom;

import java.util.List;

/**
 * 名录页一屏：这一屏实有的页，与三栏计数出自同一次算。
 * 本层这一季没起过页时 rows 为空、三栏都落 0，不用旁处的行凑数；
 * 库里确实起过的每一页都在 rows 里找得着，在场的页不会被抹去。
 *
 * @author fuce
 */
public class SaltLinkPageBoard {

    /** 在册页数（与 rows 同源：del_flag=0 的全部页） */
    private long totalCount;

    /** 已挂讫页数（status=1） */
    private long doneCount;

    /** 压页页数（status=2） */
    private long heldCount;

    /** 当前这一屏的页（一屏摆多少条由处里定） */
    private List<SaltLinkPageVo> rows;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(long doneCount) {
        this.doneCount = doneCount;
    }

    public long getHeldCount() {
        return heldCount;
    }

    public void setHeldCount(long heldCount) {
        this.heldCount = heldCount;
    }

    public List<SaltLinkPageVo> getRows() {
        return rows;
    }

    public void setRows(List<SaltLinkPageVo> rows) {
        this.rows = rows;
    }
}
