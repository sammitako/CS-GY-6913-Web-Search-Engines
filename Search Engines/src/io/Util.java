package io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    // DocumentParser -> temp*.txt -> SortedFileMerger
    private static final Pattern FILENAME_PATTERN = Pattern.compile("temp(\\d+)\\.txt");

    // Formats time in milliseconds to HH:mm:ss format
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%06d", h, m, s);
    }

    public static String calculateAndFormatDuration(long startTime, long endTime) {
        long durationInMillis = endTime - startTime;

        return formatTime(durationInMillis);
    }

    // Formats file size in bytes to a human-readable format
    public static String formatSize(long bytes) {
        final long KILOBYTE = 1024;
        final long MEGABYTE = KILOBYTE * 1024;
        final long GIGABYTE = MEGABYTE * 1024;

        double size;
        String unit;

        if (bytes >= GIGABYTE) {
            size = (double) bytes / GIGABYTE;
            unit = "GB";
        } else if (bytes >= MEGABYTE) {
            size = (double) bytes / MEGABYTE;
            unit = "MB";
        } else if (bytes >= KILOBYTE) {
            size = (double) bytes / KILOBYTE;
            unit = "KB";
        } else {
            size = (double) bytes;
            unit = "bytes";
        }

        return String.format("%.2f %s", size, unit);
    }


    /**
     * Extracts the numeric index from a filename.
     * @param filename The name of the file.
     * @return The extracted index or -1 if no index is found.
     */
    public static int getIndexFromFilename(String filename) {
        Matcher matcher = FILENAME_PATTERN.matcher(filename);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

}
