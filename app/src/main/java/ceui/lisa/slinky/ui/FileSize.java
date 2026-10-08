package ceui.lisa.slinky.ui;

import java.text.DecimalFormat;

public class FileSize {

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0KB";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
