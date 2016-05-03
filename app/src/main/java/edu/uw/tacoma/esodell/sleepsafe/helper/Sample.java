package edu.uw.tacoma.esodell.sleepsafe.helper;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Sample {
    public int hr_val;
    public int spo2_val;
    public int temp_val;
    public Calendar timestamp;

    public Sample(int hr, int spo2, int temp) {
        this.hr_val = hr;
        this.spo2_val = spo2;
        this.temp_val = temp;
        this.timestamp = Calendar.getInstance();
    }

    public Sample(int hr, int spo2, int temp, Calendar time) {
        this.hr_val = hr;
        this.spo2_val = spo2;
        this.temp_val = temp;
        this.timestamp = time;
    }

    public Sample(int hr, int spo2, int temp, String timeString) {
        this.hr_val = hr;
        this.spo2_val = spo2;
        this.temp_val = temp;
        this.timestamp = new GregorianCalendar();
        this.timestamp.setTime(new Date(Long.parseLong(timeString)));
    }

    @Override
    public String toString() {
        return "Sample<" + timestamp.toString()
                + ">: HR:" + hr_val + "bpm SpO2:"
                + spo2_val + " Temp:" + temp_val;
    }
}
