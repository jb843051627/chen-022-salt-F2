package com.fc.v2.model.custom;

import java.util.ArrayList;
import java.util.List;

/**
 * 挂接簿页保存（起页/改页）的回执。
 * 欠挂品种数、挂接代号、随行签认码都由系统在保存时折出写回，
 * 不合规的页起不来，errors 逐条讲明毛病出在哪一栏。
 *
 * @author fuce
 */
public class SaltLinkSaveResult {

    /** 落库是否成功 */
    private boolean success;

    /** 页主键 */
    private Long id;

    /** 系统发的挂接代号 */
    private String billNo;

    /** 系统发的随行签认码 */
    private String signCode;

    /** 折回页上的欠挂品种数 */
    private Integer lackCount;

    /** 逐栏毛病（每条已点明栏位） */
    private List<String> errors = new ArrayList<>();

    public static SaltLinkSaveResult ok(Long id, String billNo, String signCode, Integer lackCount) {
        SaltLinkSaveResult r = new SaltLinkSaveResult();
        r.success = true;
        r.id = id;
        r.billNo = billNo;
        r.signCode = signCode;
        r.lackCount = lackCount;
        return r;
    }

    public static SaltLinkSaveResult fail(List<String> errors) {
        SaltLinkSaveResult r = new SaltLinkSaveResult();
        r.success = false;
        if (errors != null) {
            r.errors.addAll(errors);
        }
        return r;
    }

    /** 回执上给人看的整句 */
    public String getMessage() {
        return success ? "操作成功" : String.join("；", errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

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

    public Integer getLackCount() {
        return lackCount;
    }

    public void setLackCount(Integer lackCount) {
        this.lackCount = lackCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
