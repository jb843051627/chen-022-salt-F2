package com.fc.v2.model.custom;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

/**
 * 名录页上的一行：挂接簿页附上企业总名录里的代号、全称、类别、属地。
 * 企业摘牌后不再开新页，但它底下早前的页照旧查得着、照旧算在册，
 * 所以 siteStatus 一并带出供屏上辨识。
 *
 * @author fuce
 */
public class SaltLinkPageVo {

    /** 页主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 挂接代号（系统发） */
    private String billNo;

    /** 随行签认码（系统发） */
    private String signCode;

    /** 适用区域层级序 */
    private Integer nodeNo;

    /** 所对定点企业主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long siteId;

    /** 企业代号 */
    private String siteNo;

    /** 企业全称 */
    private String siteName;

    /** 企业类别（制盐企业/省内批发企业/跨省批发企业） */
    private String siteType;

    /** 属地一路写到县（省—市—县） */
    private String roadName;

    /** 名录情形 0在册 1已摘牌 */
    private Integer siteStatus;

    /** 应挂品种数 */
    private Integer shouldCount;

    /** 已挂品种数 */
    private Integer doneCount;

    /** 欠挂品种数（只许看不许改） */
    private Integer lackCount;

    /** 进展 0待挂 1已挂讫 2压页 */
    private Integer status;

    /** 随页交来的标签要件 */
    private String content;

    /** 备注 */
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getSignCode() {
        return signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteType() {
        return siteType;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public Integer getSiteStatus() {
        return siteStatus;
    }

    public void setSiteStatus(Integer siteStatus) {
        this.siteStatus = siteStatus;
    }

    public Integer getShouldCount() {
        return shouldCount;
    }

    public void setShouldCount(Integer shouldCount) {
        this.shouldCount = shouldCount;
    }

    public Integer getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(Integer doneCount) {
        this.doneCount = doneCount;
    }

    public Integer getLackCount() {
        return lackCount;
    }

    public void setLackCount(Integer lackCount) {
        this.lackCount = lackCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
