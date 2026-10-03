package com.fc.v2.model.custom;

/**
 * 起页/改页表单。应挂、已挂两栏以原始字符串承接，
 * 好让服务层把“空着递、带小数、负数、高过应挂”逐栏讲清毛病出在哪一栏，
 * 而不是在绑定环节被笼统挡下。
 * 挂接代号、随行签认码、欠挂品种数不在此表单上——系统发、系统折，人手写不收。
 *
 * @author fuce
 */
public class SaltLinkPageForm {

    /** 页主键（改页时给） */
    private String id;

    /** 所对定点企业（企业总名录代号或主键） */
    private String siteId;

    /** 适用区域层级序 0省 1市 2县 */
    private String nodeNo;

    /** 应挂品种数（原始串） */
    private String shouldCount;

    /** 已挂品种数（原始串） */
    private String doneCount;

    /** 进展 0待挂 2压页（已挂讫由系统按欠挂落） */
    private String status;

    /** 随页交来的标签要件 */
    private String content;

    /** 备注 */
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(String nodeNo) {
        this.nodeNo = nodeNo;
    }

    public String getShouldCount() {
        return shouldCount;
    }

    public void setShouldCount(String shouldCount) {
        this.shouldCount = shouldCount;
    }

    public String getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(String doneCount) {
        this.doneCount = doneCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
}
