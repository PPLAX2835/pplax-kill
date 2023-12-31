package xyz.pplax.kill.dto;

import java.io.Serializable;

/**
 * 暴露秒杀地址DTO
 */
public class Exposer implements Serializable {

    private static final long serialVersionUID = 3945522124434282652L;
    //是否开启秒杀
    private boolean exposed;

    //一种加密措施
    private String md5;

    //id
    private long killId;

    //系统当前时间(毫秒)
    private long now;

    //开启时间
    private long start;

    //结束时间
    private long end;

    private Exposer(){}

    public Exposer(boolean exposed, String md5, long killId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.killId = killId;
    }

    public Exposer(boolean exposed, long killId, long now, long start, long end) {
        this.exposed = exposed;
        this.killId = killId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public Exposer(boolean exposed, long killId) {
        this.exposed = exposed;
        this.killId = killId;
    }

    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getKillId() {
        return killId;
    }

    public void setKillId(long killId) {
        this.killId = killId;
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Exposer{" +
                "exposed=" + exposed +
                ", md5='" + md5 + '\'' +
                ", killId=" + killId +
                ", now=" + now +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
