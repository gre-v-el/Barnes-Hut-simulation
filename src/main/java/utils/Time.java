package utils;

public class Time {
    public static long timeStarted = System.currentTimeMillis();

    public static float getTime(){
        return (float)(System.currentTimeMillis()-timeStarted)*0.001f;
    }
}
