package online.zust.qcqcqc.utils.threads.enums;

/**
 * @author qcqcqc
 */

public enum PromiseStatus {
    /**
     * 未完成
     */
    PENDING(0, "PENDING"),
    /**
     * 已完成
     */
    FULFILLED(1, "FULFILLED"),
    /**
     * 已拒绝
     */
    REJECTED(2, "REJECTED"),
    /**
     * 已取消
     */
    CANCELED(3, "CANCELED");

    private final int code;
    private final String desc;

    PromiseStatus(int i, String rejected) {
        this.code = i;
        this.desc = rejected;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
