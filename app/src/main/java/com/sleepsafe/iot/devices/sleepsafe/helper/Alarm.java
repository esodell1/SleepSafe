package com.sleepsafe.iot.devices.sleepsafe.helper;

public class Alarm {
    private int mValue;

    public int getmValue() {
        return mValue;
    }

    public void setmValue(int mValue) {
        this.mValue = mValue;
    }

    public boolean ismLessThan() {
        return mLessThan;
    }

    public void setmLessThan(boolean mLessThan) {
        this.mLessThan = mLessThan;
    }

    private boolean mLessThan;

    public Alarm(int value, boolean lessThan) {
        mValue = value;
        mLessThan = lessThan;
    }
}
