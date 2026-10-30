package com.focuslock.app;

import android.content.Context;
import android.content.SharedPreferences;

/** Only records checkmarks, journal, daily actions and an optional intention, locally. */
public final class DailyStore {
    private static final String PREF = "focus_habits";
    private DailyStore() {}
    private static SharedPreferences p(Context ctx) { return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE); }
    private static String key(String name) { return DayKeys.today() + "." + name; }
    public static boolean done(Context ctx, int i) { return p(ctx).getBoolean(key("routine." + i), false); }
    public static void done(Context ctx, int i, boolean value) { p(ctx).edit().putBoolean(key("routine." + i), value).apply(); }
    public static int routineCount(Context ctx) { int n=0; for(int i=0;i<FocusContent.ROUTINE_NAMES.length;i++) if(done(ctx,i)) n++; return n; }
    public static int alternatives(Context ctx) { return p(ctx).getInt(key("alternatives"), 0); }
    public static void completedAlternative(Context ctx) { p(ctx).edit().putInt(key("alternatives"), alternatives(ctx) + 1).apply(); }
    public static int focusSessions(Context ctx) { return p(ctx).getInt(key("focus_sessions"), 0); }
    public static void completedFocus(Context ctx) { p(ctx).edit().putInt(key("focus_sessions"), focusSessions(ctx) + 1).apply(); }
    public static String journal(Context ctx) { return p(ctx).getString(key("journal"), ""); }
    public static void journal(Context ctx, String value) { p(ctx).edit().putString(key("journal"), value).apply(); }
    public static String intention(Context ctx) { return p(ctx).getString("intention", ""); }
    public static void intention(Context ctx, String value) { p(ctx).edit().putString("intention", value).apply(); }
    public static String ifThen(Context ctx) { return p(ctx).getString("ifthen", "If I reach for social media out of habit, then I will take a 2-minute break."); }
    public static void ifThen(Context ctx, String value) { p(ctx).edit().putString("ifthen", value).apply(); }
}
