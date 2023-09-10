package xyz.pplax.kill.dto;


import xyz.pplax.kill.entity.PayOrder;
import xyz.pplax.kill.enums.PPLAXKillStateEnum;

/**
 * 封装秒杀执行后结果
 */
public class PPLAXKillExecution {

    private long killId;

    //秒杀执行结果状态
    private int state;

    //状态表示
    private String stateInfo;

    //秒杀成功对象
    private PayOrder payOrder;

    public PPLAXKillExecution(long killId, PPLAXKillStateEnum pplaxKillStateEnum, PayOrder payOrder) {
        this.killId = killId;
        this.state = pplaxKillStateEnum.getState();
        this.stateInfo = pplaxKillStateEnum.getStateInfo();
        this.payOrder = payOrder;
    }

    public PPLAXKillExecution(long killId, PPLAXKillStateEnum pplaxKillStateEnum) {
        this.killId = killId;
        this.state = pplaxKillStateEnum.getState();
        this.stateInfo = pplaxKillStateEnum.getStateInfo();
    }

    public long getKillId() {
        return killId;
    }

    public void setKillId(long killId) {
        this.killId = killId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public PayOrder getPayOrder() {
        return payOrder;
    }

    public void setPayOrder(PayOrder payOrder) {
        this.payOrder = payOrder;
    }

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "killId=" + killId +
                ", state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                ", payOrder=" + payOrder +
                '}';
    }
}
