package xyz.pplax.kill.dto;

import java.io.Serializable;

public class PPLAXKillMsgBody implements Serializable {
    private static final long serialVersionUID = -4206751408398568444L;
    private long killId;
    private long userPhone;

    public long getKillId() {
        return killId;
    }

    public void setKillId(long killId) {
        this.killId = killId;
    }

    public long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(long userPhone) {
        this.userPhone = userPhone;
    }

    @Override
    public String toString() {
        return "PPLAXKillMsgBody{" +
                "killId=" + killId +
                ", userPhone=" + userPhone +
                '}';
    }
}
