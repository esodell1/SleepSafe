package com.sleepsafe.iot.devices.sleepsafe.helper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Session {
    private int mSessionID = 0;

    private List<Sample> mSessionData;

    private int mHRAverage, mSpo2Average, mTempAverage;

    private Timestamp mStart, mEnd;


    public Session() {
        mSessionData = new ArrayList<>();
    }

    public Timestamp getmStart() {
        return mStart;
    }

    public Timestamp getmEnd() {
        return mEnd;
    }

    public List<Sample> getmSessionData() {
        return mSessionData;
    }

    public Session(int session, List<Sample> data) {
        mSessionData = data;
        mSessionID = session;
        computeMetaInfo();
    }

    public void addSample(Sample sample) {
        if (sample != null) mSessionData.add(sample);
    }

    public void computeMetaInfo() {
        int hr = 0, spo2 = 0, temp = 0;
        for(int i = 0; i < mSessionData.size(); i++) {
            Sample session = mSessionData.get(i);
            if (i == 0) {
                mStart = mEnd = mSessionData.get(0).timestamp;
            }
            if (session.timestamp.getTime() < mStart.getTime()) {
                mStart = session.timestamp;
            } else if (session.timestamp.getTime() > mEnd.getTime()) {
                mEnd = session.timestamp;
            }
            hr += session.hr_val;
            spo2 += session.spo2_val;
            temp += session.temp_val;
        }
        mHRAverage = (hr / mSessionData.size());
        mSpo2Average = (spo2 / mSessionData.size());
        mTempAverage = (temp / mSessionData.size());
    }
}
