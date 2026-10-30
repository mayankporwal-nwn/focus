package com.focuslock.app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Date keys follow the device's local date. User can change clock, so not tamper-proof. */
public final class DayKeys {
    private DayKeys() {}
    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
    public static String forEpoch(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(millis));
    }
    public static boolean sameDay(String a, String b) { return a != null && a.equals(b); }
}
