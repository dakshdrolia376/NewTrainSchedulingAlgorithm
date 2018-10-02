package iitp.naman.newtrainschedulingalgorithm.util;

import java.util.Map;

/**
 * Helper class for station id.
 */
public class StationIdHelper {

    @SuppressWarnings("SpellCheckingInspection")
    private static Map<String, String> uniqueStationIdMap = Map.ofEntries(Map.entry("-k", "unique-k"),
            Map.entry("-a", "unique-a"),
            Map.entry("-t", "unique-t"), Map.entry("-b", "unique-b"), Map.entry("-x", "unique-x"),
            Map.entry("haldibari-chilahati-zero-point-0ph-c", "unique-phc"),
            Map.entry("radhikapur-birol-zero-point-0pr-b", "unique-prb"),
            Map.entry("0-point-of-mssn-latu-line-0pm-l", "unique-pml"),
            Map.entry("petrapole-benapole-zero-point-0pp-b", "unique-ppb"),
            Map.entry("gede-darshana-zero-point-0pg-d", "unique-pgd"),
            Map.entry("singabad-rohanpur-zero-point-0ps-r", "unique-psr"),
            Map.entry("-am", "unique-am"), Map.entry("-er", "unique-er"), Map.entry("-bq", "unique-bq"),
            Map.entry("-cr", "unique-cr"), Map.entry("-yd", "unique-yd"), Map.entry("-cy", "unique-cy"),
            Map.entry("-ka", "unique-ka"), Map.entry("-ne", "unique-ne"),
            Map.entry("lalmonirhat-junction-br-lmh", "br-lmh"),
            Map.entry("rohri-junction-pr-roh", "pr-roh"),
            Map.entry("mominpur-b-mmpr", "b-mmpr"));

    /**
     * @param name station name.
     * @return station id.
     */
    public static String getStationIdFromName(String name) {
        name = name.toLowerCase();
        if (uniqueStationIdMap.containsKey(name)) {
            return uniqueStationIdMap.get(name);
        }
        if (name.contains("-sl-")) {
            return name.trim().replaceAll(".*-(?=-sl-.)", "");
        } else if (name.contains("-xx-")) {
            return name.trim().replaceAll(".*-(?=-xx-.)", "");
        } else if (name.contains("-yy-")) {
            return name.trim().replaceAll(".*-(?=-yy-.)", "");
        } else if (name.endsWith("-dls")) {
            return name.trim().replaceAll(".*-(?=.+?-dls)", "");
        } else if (name.endsWith("-els")) {
            return name.trim().replaceAll(".*-(?=.+?-els)", "");
        } else if (name.endsWith("-")) {
            return name.trim().replaceAll(".*-(?=.+?-.)", "");
        }
        return name.trim().replaceAll(".*-", "");
    }
}
