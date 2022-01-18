package devesh.ephrine;

import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Codes {
    //todo: COMMON_RESOLUTIONS
    static final ArrayList<Size> COMMON_RESOLUTIONS = new ArrayList<Size>(Arrays.asList(
        // 0, Unknown resolution
        new Size(160, 120), // 1, QQVGA
        new Size(240, 160), // 2, HQVGA
        new Size(320, 240), // 3, QVGA
        new Size(400, 240), // 4, WQVGA
        new Size(480, 320), // 5, HVGA
        new Size(640, 360), // 6, nHD
        new Size(640, 480), // 7, VGA
        new Size(768, 480), // 8, WVGA
        new Size(854, 480), // 9, FWVGA
        new Size(800, 600), // 10, SVGA
        new Size(960, 540), // 11, qHD
        new Size(960, 640), // 12, DVGA
        new Size(1024, 576), // 13, WSVGA
        new Size(1024, 600), // 14, WVSGA
        new Size(1280, 720), // 15, HD
        new Size(1280, 1024), // 16, SXGA
        new Size(1920, 1080), // 17, Full HD
        new Size(1920, 1440), // 18, Full HD 4:3
        new Size(2560, 1440), // 19, QHD
        new Size(3840, 2160) // 20, UHD
        ));
}
