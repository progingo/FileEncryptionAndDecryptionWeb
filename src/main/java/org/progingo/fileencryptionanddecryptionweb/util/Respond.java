package org.progingo.fileencryptionanddecryptionweb.util;

import java.io.Serializable;

public class Respond implements Serializable {
    private int state;
    private Object data;
    private String errorMsg;

    public Respond(int state) {
        this.state = state;
    }

    public Respond(int state, Object data) {
        this.state = state;
        this.data = data;
    }

    public Respond(int state, Object data, String errorMsg) {
        this.state = state;
        this.data = data;
        this.errorMsg = errorMsg;
    }

    public static Respond ok(){
        return new Respond(200);
    }
    public static Respond ok(Object data){
        return new Respond(200,data);
    }
    public static Respond fail(String errorMsg){
        return new Respond(500,null,errorMsg);
    }
    public static Respond fail(int state, String errorMsg){
        return new Respond(state,null,errorMsg);
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
