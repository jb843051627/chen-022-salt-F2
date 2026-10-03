package com.fc.v2.model.custom;

/**
 * 名录页寻页三项：挂接代号、企业代号、企业类别。
 * 三项都可给、可只给半段、也可只给片段，凡整段或片段咬得上都算寻着。
 *
 * @author fuce
 */
public class SaltLinkPageQuery {

    /** 挂接代号（片段） */
    private String billNo;

    /** 企业代号（片段） */
    private String siteNo;

    /** 企业类别（片段） */
    private String siteType;

    /** 页码（一屏摆多少条由处里定） */
    private Integer page;

    /** 每屏条数 */
    private Integer limit;

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }

    public String getSiteType() {
        return siteType;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
