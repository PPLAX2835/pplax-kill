package xyz.pplax.kill.dto;

import java.io.Serializable;

/**
 * 所有ajax请求放回类型,封装json结果
 * @param <T>
 */
public class PPLAXKillResult<T> implements Serializable {

    private static final long serialVersionUID = -7301291894175524606L;
    private boolean success;

    private T data;

    private String error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "PPLAXKillResult{" +
                "success=" + success +
                ", data=" + data +
                ", error='" + error + '\'' +
                '}';
    }
}
