package iitp.naman.newtrainschedulingalgorithm.util;

/**
 * Helper class for train number.
 */
public class TrainNumberHelper {

    /**
     * @param name train name.
     * @return train number.
     */
    public static String getTrainNoFromName(String name) {
        name = name.toLowerCase();
        if (name.endsWith("-slip")) {
            name = name.substring(0, name.length() - 5);
            return "9" + name.trim().replaceAll(".*-(?=.)", "");
        }
        return name.trim().replaceAll(".*-(?=.)", "");
    }
}
