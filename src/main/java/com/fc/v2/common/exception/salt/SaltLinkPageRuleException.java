package com.fc.v2.common.exception.salt;

import java.util.ArrayList;
import java.util.List;

/**
 * 挂接簿页不合规：页起不来/进不了库。
 * 一条异常带齐逐栏毛病，回执讲清毛病出在哪一栏。
 *
 * @author fuce
 */
public class SaltLinkPageRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> errors;

    public SaltLinkPageRuleException(List<String> errors) {
        super(String.join("；", errors == null ? new ArrayList<>() : errors));
        this.errors = errors == null ? new ArrayList<>() : errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
