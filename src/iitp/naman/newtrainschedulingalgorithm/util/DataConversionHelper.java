package iitp.naman.newtrainschedulingalgorithm.util;

public class DataConversionHelper {
    private static final long MEGABYTE = 1024L * 1024L;

    /**
     * @param bytes bytes
     * @return converted to megabytes
     */
    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }
}
