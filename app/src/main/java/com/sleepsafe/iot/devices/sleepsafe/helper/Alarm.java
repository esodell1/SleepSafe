package com.sleepsafe.iot.devices.sleepsafe.helper;


public class Alarm {
    private int mValue;
    private boolean mLessThan;

    public Alarm(int value, boolean lessThan) {
        mValue = value;
        mLessThan = lessThan;
    }
}
