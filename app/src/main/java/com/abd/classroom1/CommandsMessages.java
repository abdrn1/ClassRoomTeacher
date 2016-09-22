package com.abd.classroom1;

/**
 * Created by Abd on 8/29/2016.
 */
public class CommandsMessages {

    /* command type
    0 : zoom
    3: scrool down
    4:scrol up
    */
    double zoomFactor =1;
    int commnadType =0;


    public CommandsMessages(){

    }

    public CommandsMessages(int commnadType) {
        this.commnadType = commnadType;
    }

    public CommandsMessages(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }


    public int getCommnadType() {
        return commnadType;
    }

    public void setCommnadType(int commnadType) {
        this.commnadType = commnadType;
    }
}
