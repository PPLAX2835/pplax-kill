package xyz.pplax.kill.exception;


import xyz.pplax.kill.enums.PPLAXKillStateEnum;

/**
 * 秒杀相关业务异常
 */
public class PPLAXKillException extends RuntimeException {

    private PPLAXKillStateEnum pplaxKillStateEnum;

    public PPLAXKillException(PPLAXKillStateEnum seckillStateEnum) {
        this.pplaxKillStateEnum = seckillStateEnum;
    }

    public PPLAXKillException(String message) {
        super(message);
    }

    public PPLAXKillException(String message, Throwable cause) {
        super(message, cause);
    }

    public PPLAXKillStateEnum getSeckillStateEnum() {
        return pplaxKillStateEnum;
    }

    public void setSeckillStateEnum(PPLAXKillStateEnum seckillStateEnum) {
        this.pplaxKillStateEnum = seckillStateEnum;
    }
}
