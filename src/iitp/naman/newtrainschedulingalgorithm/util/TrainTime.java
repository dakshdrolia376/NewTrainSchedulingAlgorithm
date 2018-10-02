package iitp.naman.newtrainschedulingalgorithm.util;

import static java.util.Objects.requireNonNull;

/**
 * A helper class to store the time info of a train schedule at a station.
 */
public class TrainTime {
    private byte day;
    private byte hour;
    private byte minute;

    /**
     * Creates a new instance with time as given in param.
     *
     * @param day    day info.
     * @param hour   hour info.
     * @param minute minute info.
     */
    public TrainTime(int day, int hour, int minute) {
        setData((byte) day, (byte) hour, (byte) minute);
    }

    /**
     * Creates a new instance by coping from existing TrainTime.
     *
     * @param trainTime existing trainTime instance.
     */
    public TrainTime(TrainTime trainTime) {
        requireNonNull(trainTime, "TrainTime cannot be null");
        setData(trainTime.day, trainTime.hour, trainTime.minute);
    }

    /**
     * Creates a new instance by setting time as given in param.
     *
     * @param timeInt number of minutes passed from the start of a week (sunday at 00:00).
     */
    public TrainTime(int timeInt) {
        setData((byte) ((timeInt / 1440) % 7), (byte) ((timeInt / 60) % 24), (byte) (timeInt % 60));
    }

    /**
     * Creates a new instance by parsing time info from param.
     *
     * @param label Time label in format D:HH:MM.
     */
    public TrainTime(String label) {
        String[] data = label.split(":");
        if (data.length != 3) {
            throw new IllegalArgumentException("Label does not match required pattern");
        }
        try {
            setData(Byte.parseByte(data[0]), Byte.parseByte(data[1]), Byte.parseByte(data[2]));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Train time label is invalid " + label);
        }
    }

    /**
     * Sets the data.
     *
     * @param day    day info.
     * @param hour   hour info.
     * @param minute minute info.
     */
    private void setData(byte day, byte hour, byte minute) {
        if (day >= 7 || day < 0) {
            throw new IllegalArgumentException("Day info is invalid");
        }
        if (hour < 0 || hour >= 24) {
            throw new IllegalArgumentException("Hour info is invalid");
        }
        if (minute < 0 || minute > 60) {
            throw new IllegalArgumentException("Minute info is invalid");
        }
        this.day = day;
        this.hour = hour;
        if (minute == 60) {
            this.minute = 0;
            addHours(1);
        } else {
            this.minute = minute;
        }
    }

    /**
     * @return day info.
     */
    public byte getDay() {
        return this.day;
    }

    /**
     * @return byte info.
     */
    public byte getHour() {
        return this.hour;
    }

    /**
     * @return minute info.
     */
    public byte getMinute() {
        return this.minute;
    }

    /**
     * Compares two trainTime and returns the difference.
     *
     * @param trainTime trainTime instance to be compared to.
     * @return difference in minutes between two times.
     */
    public int compareTo(TrainTime trainTime) {
        requireNonNull(trainTime, "TrainTime is null");
        int ans = this.day - trainTime.day;
        ans *= 24;
        ans += this.hour - trainTime.hour;
        ans *= 60;
        ans += this.minute - trainTime.minute;
        return ans;
    }

    /**
     * Compares teo trainTime for equality.
     *
     * @param trainTime trainTime instance to be compared to.
     * @return true if equal otherwise false.
     */
    public boolean equals(TrainTime trainTime) {
        requireNonNull(trainTime, "util.TrainTime is null");
        return this.day == trainTime.day && this.hour == trainTime.hour && this.minute == trainTime.minute;
    }

    /**
     * Add days in current trainTime.
     *
     * @param day number of days to add.
     */
    public void addDay(int day) {
        day += this.day;
        this.day = (byte) Math.floorMod(day, 7);
    }

    /**
     * Add hours in current trainTime.
     *
     * @param hours number of hours to add.
     */
    public void addHours(int hours) {
        hours += this.hour;
        addDay(hours / 24);
        this.hour = (byte) Math.floorMod(hours, 24);
    }

    /**
     * Add minutes in current trainTime.
     *
     * @param minutes number of minutes to add.
     */
    public void addMinutes(int minutes) {
        minutes += this.minute;
        addHours(minutes / 60);
        this.minute = (byte) Math.floorMod(minutes, 60);
    }

    /**
     * Subtract days in current trainTime.
     *
     * @param day number of days to subtract.
     */
    public void subDay(int day) {
        day = Math.floorMod(day, 7);
        this.day -= day;
        this.day = (byte) Math.floorMod(this.day, 7);
    }

    /**
     * Subtract hours in current trainTime.
     *
     * @param hours number of hours to subtract.
     */
    public void subHours(int hours) {
        hours = Math.floorMod(hours, 168); //24*7
        hours = this.hour - hours;
        if (hours >= 0) {
            this.hour = (byte) hours;
            return;
        }
        int daysToSub = (-hours) / 24 + 1;
        subDay(daysToSub);
        this.hour = (byte) Math.floorMod(hours, 24);
    }

    /**
     * Subtract minutes in current trainTime.
     *
     * @param minutes number of minutes to subtract.
     */
    public void subMinutes(int minutes) {
        minutes = Math.floorMod(minutes, 10080); //60*24*7
        minutes = this.minute - minutes;
        if (minutes >= 0) {
            this.minute = (byte) minutes;
            return;
        }
        int hrsToSub = (-minutes) / 60 + 1;
        subHours(hrsToSub);
        this.minute = (byte) Math.floorMod(minutes, 60);
    }

    /**
     * @return the trainTime in number of minutes passed from the start of a week (sunday at 00:00).
     */
    public int getValue() {
        return (this.day * 24 + this.hour) * 60 + this.minute;
    }

    /**
     * @return String label for time in format D:HH:MM. May not append 0 in start e.g 2:2:20 is possible.
     */
    @Override
    public String toString() {
        return this.day + ":" + this.hour + ":" + this.minute;
    }

    /**
     * @return String label for time in format HH:MM.
     */
    public String getTimeString() {
        return String.format("%02d", this.hour) + ":" + String.format("%02d", this.minute);
    }

    /**
     * @return String label for time in format D:HH:MM. Must append 0 in start e.g 2:02:20 is possible.
     */
    public String getFullString() {
        return String.format("%01d", this.day) + ":" + String.format("%02d", this.hour) + ":" + String.format("%02d", this.minute);
    }
}
