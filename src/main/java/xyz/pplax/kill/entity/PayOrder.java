package xyz.pplax.kill.entity;

import java.util.Date;

/**
 * 订单
 */
public class PayOrder {

    private long killId;

    private long userPhone;

    private int state;

    private Date createTime;

    // 多对一
    private PPLAXKill pplaxKill;


    public long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(long userPhone) {
        this.userPhone = userPhone;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getKillId() {
        return killId;
    }

    public void setKillId(long killId) {
        this.killId = killId;
    }

    public PPLAXKill getPplaxKill() {
        return pplaxKill;
    }

    public void setPplaxKill(PPLAXKill pplaxKill) {
        this.pplaxKill = pplaxKill;
    }

    @Override
    public String toString() {
        return "PayOrder{" +
                "killId=" + killId +
                ", userPhone=" + userPhone +
                ", state=" + state +
                ", createTime=" + createTime +
                ", pplaxKill=" + pplaxKill +
                '}';
    }
}
