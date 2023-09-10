package xyz.pplax.kill.exception;


import xyz.pplax.kill.enums.PPLAXKillStateEnum;

/**
 * 秒杀相关业务异常
 */
public class PPLAXKillException extends RuntimeException {

    private PPLAXKillStateEnum pplaxKillStateEnum;

    public PPLAXKillException(PPLAXKillStateEnum pplaxKillStateEnum) {
        this.pplaxKillStateEnum = pplaxKillStateEnum;
    }

    public PPLAXKillException(String message) {
        super(message);
    }

    public PPLAXKillException(String message, Throwable cause) {
        super(message, cause);
    }

    public PPLAXKillStateEnum getPPLAXKillStateEnum() {
        return pplaxKillStateEnum;
    }

    public void setPPPLAXKillStateEnum(PPLAXKillStateEnum pplaxKillStateEnum) {
        this.pplaxKillStateEnum = pplaxKillStateEnum;
    }
}
