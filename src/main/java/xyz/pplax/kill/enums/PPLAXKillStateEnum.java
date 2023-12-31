package xyz.pplax.kill.enums;

/**
 * 使用枚举表述常量数据字段
 */
public enum PPLAXKillStateEnum {
    ENQUEUE_PRE_PPLAXKILL(6, "排队中..."),

    /**
     * 释放分布式锁失败，秒杀被淘汰
     */
    DISTLOCK_RELEASE_FAILED(5, "没抢到"),

    /**
     * 获取分布式锁失败，秒杀被淘汰
     */
    DISTLOCK_ACQUIRE_FAILED(4, "没抢到"),

    /**
     * Redis秒杀没抢到
     */
    REDIS_ERROR(3, "没抢到"),
    SOLD_OUT(2, "已售罄"),
    SUCCESS(1, "秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "重复秒杀"),
    /**
     * 运行时才能检测到的所有异常-系统异常
     */
    INNER_ERROR(-2, "没抢到"),
    /**
     * md5错误的数据篡改
     */
    DATA_REWRITE(-3, "数据篡改"),

    DB_CONCURRENCY_ERROR(-4, "没抢到"),
    /**
     * 被AccessLimitService限流了
     */
    ACCESS_LIMIT(-5, "没抢到");


    private int state;
    private String stateInfo;

    PPLAXKillStateEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public static PPLAXKillStateEnum stateOf(int index) {
        for (PPLAXKillStateEnum state : values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }
}
