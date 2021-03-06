package com.example.booksmanager.support;

import org.springframework.stereotype.Component;

@Component
public class Message{

    private String success = null;
    private String error = null;
    private String info = null;

    public Message() {
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void reset(){
        this.error=null;
        this.success=null;
        this.info=null;
    }
}
