package com.github.canbabel.canio.dbc;

public class MessageComment extends Comment {

    private long id;
    private boolean extended;

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
