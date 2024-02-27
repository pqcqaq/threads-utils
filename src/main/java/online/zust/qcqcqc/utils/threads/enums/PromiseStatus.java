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

    /**
     * 状态码
     */
    private final int code;
    /**
     * 描述
     */
    private final String desc;

    /**
     * 构造方法
     * @param i 状态码
     * @param rejected 描述
     */
    PromiseStatus(int i, String rejected) {
        this.code = i;
        this.desc = rejected;
    }

    /**
     * 获取状态码
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取描述
     * @return 描述
     */
    public String getDesc() {
        return desc;
    }
}
