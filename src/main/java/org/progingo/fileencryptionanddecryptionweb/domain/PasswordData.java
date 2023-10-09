package org.progingo.fileencryptionanddecryptionweb.domain;

import java.io.Serializable;

public class PasswordData implements Serializable {
    //private static final long serialVersionUID = 766474;

    private int state;

    private String from;
    private String zh;
    private String password;
    private String bz;

    private String nextName;
    private String nextUUID;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getZh() {
        return zh;
    }

    public void setZh(String zh) {
        this.zh = zh;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBz() {
        return bz;
    }

    public void setBz(String bz) {
        this.bz = bz;
    }

    public String getNextName() {
        return nextName;
    }

    public void setNextName(String nextName) {
        this.nextName = nextName;
    }

    public String getNextUUID() {
        return nextUUID;
    }

    public void setNextUUID(String nextUUID) {
        this.nextUUID = nextUUID;
    }

    @Override
    public String toString() {
        return "PasswordData{" +
                "from='" + from + '\'' +
                ", zh='" + zh + '\'' +
                ", password='" + password + '\'' +
                ", bz='" + bz + '\'' +
                ", nextName='" + nextName + '\'' +
                ", nextUUID='" + nextUUID + '\'' +
                '}';
    }
}
