package org.progingo.fileencryptionanddecryptionweb.domain;

public class PasswordDataSafe {

    private int state;

    private String from = "";
    private String zh = "";
    private String bz = "";

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

    public String getBz() {
        return bz;
    }

    public void setBz(String bz) {
        this.bz = bz;
    }

    @Override
    public String toString() {
        return "PasswordDataSafe{" +
                "state=" + state +
                ", from='" + from + '\'' +
                ", zh='" + zh + '\'' +
                ", bz='" + bz + '\'' +
                '}';
    }
}
