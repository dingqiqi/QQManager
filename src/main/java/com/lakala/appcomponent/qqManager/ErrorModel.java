package com.lakala.appcomponent.qqManager;

public class ErrorModel {

    private int code;
    private String msg;
    private String detail;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ErrorModel{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}
