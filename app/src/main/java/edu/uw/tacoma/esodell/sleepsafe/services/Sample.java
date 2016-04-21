package edu.uw.tacoma.esodell.sleepsafe.services;

import java.sql.Time;
import java.util.Calendar;

public class Sample {
    int hr_val;
    int spo2_val;
    int temp_val;
    Time timestamp;

    public Sample(int hr, int spo2, int temp) {
        this.hr_val = hr;
        this.spo2_val = spo2;
        this.temp_val = temp;
        this.timestamp = new Time(Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public String toString() {
        return "Sample<" + timestamp.toString()
                + ">: HR:" + hr_val + "bpm SpO2:"
                + spo2_val + " Temp:" + temp_val;
    }
}
