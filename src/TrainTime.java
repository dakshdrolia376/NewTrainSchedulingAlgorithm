public class TrainTime {
    byte day;
    byte hour;
    byte minute;
    private static boolean isSingleDay = false;

    public static void updateIsSingleDay(boolean value){
        System.out.println("Setting is single day");
        TrainTime.isSingleDay = value;
    }

    public TrainTime(int day, int hour, int minute){
        setData((byte)day,(byte)hour,(byte)minute);
    }

    public TrainTime(TrainTime trainTime){
        setData(trainTime.day,trainTime.hour,trainTime.minute);
    }

    @SuppressWarnings("unused")
    public TrainTime(String label){
        String[] data = label.split(":");
        if(data.length!=3){
            throw new IllegalArgumentException("Label does not match required pattern");
        }
        setData(Byte.parseByte(data[0]), Byte.parseByte(data[1]), Byte.parseByte(data[2]));
    }

    private void setData(byte day, byte hour, byte minute){
        if(day>=7 || day<0){
            throw new IllegalArgumentException("Day info is invalid");
        }
        if(hour <0 || hour>=24){
            throw new IllegalArgumentException("Hour info is invalid");
        }
        if(minute<0 || minute>=60){
            throw new IllegalArgumentException("Minute info is invalid");
        }
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public int compareTo(TrainTime trainTime){
        int ans = this.day - trainTime.day;
        ans *=24;
        ans += this.hour - trainTime.hour;
        ans *=60;
        ans += this.minute - trainTime.minute;
        return ans;
    }

    public boolean equals(TrainTime trainTime){
        return this.day==trainTime.day && this.hour == trainTime.hour && this.minute == trainTime.minute;
    }

    public void addDay(int day){
        if(!isSingleDay) {
            day += this.day;
            this.day = (byte) Math.floorMod(day, 7);
        }
    }

    public void addHours(int hours){
        hours += this.hour;
        addDay(hours/24);
        this.hour = (byte)Math.floorMod(hours, 24);
    }

    public void addMinutes(int minutes){
        minutes += this.minute;
        addHours(minutes/60);
        this.minute =(byte)Math.floorMod(minutes, 60);
    }

    public void subDay(int day){
        if(!isSingleDay) {
            day = Math.floorMod(day, 7);
            this.day -= day;
            this.day = (byte) Math.floorMod(this.day, 7);
        }
    }

    public void subHours(int hours){
        hours = Math.floorMod(hours, 168); //24*7
        hours = this.hour - hours;
        if(hours>=0){
            this.hour = (byte)hours;
            return;
        }
        int daysToSub = (-hours)/24 + 1;
        subDay(daysToSub);
        this.hour = (byte)Math.floorMod(hours, 24);
    }

    public void subMinutes(int minutes){
        minutes = Math.floorMod(minutes, 10080); //60*24*7
        minutes = this.minute - minutes;
        if(minutes>=0){
            this.minute = (byte)minutes;
            return;
        }
        int hrsToSub = (-minutes)/60 + 1;
        subHours(hrsToSub);
        this.minute = (byte)Math.floorMod(minutes, 60);
    }

    public int getValue(){
        return (this.day*24 + this.hour)*60 + this.minute;
    }

    @Override
    public String toString(){
        return this.day + ":" + this.hour + ":" + this.minute;
    }

    public String getTimeString(){
        return this.hour + ":" + this.minute;
    }
}
