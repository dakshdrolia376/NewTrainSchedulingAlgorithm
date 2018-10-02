package iitp.naman.newtrainschedulingalgorithm.util;

public class RuntimeMemoryHelper {

    /**
     * Prints current used runtime memory.
     */
    public static void getRuntimeMemory() {
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Start Used memory is megabytes: " + DataConversionHelper.bytesToMegabytes(memory));
    }

    /**
     * Clears runtime memory.
     */
    public static void clearRuntimeMemory() {
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
    }
}
