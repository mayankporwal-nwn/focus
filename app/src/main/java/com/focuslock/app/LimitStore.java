package com.focuslock.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
import java.util.HashMap;

/** Local limits, with optional next-day-only changes to discourage impulse overrides. */
public final class LimitStore {
    private static final String PREFS = "daily_limits";
    private static final String OPTIONS = "focus_options";
    private LimitStore() {}
    private static SharedPreferences p(Context c) { return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }
    private static SharedPreferences o(Context c) { return c.getSharedPreferences(OPTIONS, Context.MODE_PRIVATE); }
    public static void applyDue(Context c) {
        SharedPreferences opt = o(c);
        String today = DayKeys.today();
        String pending = opt.getString("changes_for_day", "");
        // Future dates have no effect. Edits are applied once the date actually changes.
        if (!pending.isEmpty() && today.compareTo(pending) >= 0) {
            SharedPreferences.Editor dest = p(c).edit();
            for (Map.Entry<String, ?> e : opt.getAll().entrySet()) {
                if (e.getKey().startsWith("next_") && e.getValue() instanceof Integer) {
                    int value = (Integer) e.getValue(); String pkg = e.getKey().substring(5);
                    if (SafetyPolicy.canBlock(pkg)) {
                        if (value <= 0) dest.remove(pkg); else dest.putInt(pkg,value);
                    }
                }
            }
            dest.commit();
            SharedPreferences.Editor clear = opt.edit();
            for (String key : opt.getAll().keySet()) if (key.startsWith("next_")) clear.remove(key);
            clear.remove("changes_for_day").apply();
        }
    }
    public static int get(Context c, String pkg) { applyDue(c); return p(c).getInt(pkg, 0); }
    public static Map<String,Integer> all(Context c) {
        applyDue(c);
        Map<String,Integer> map = new HashMap<>();
        for (Map.Entry<String,?> e : p(c).getAll().entrySet()) {
            if (e.getValue() instanceof Integer && (Integer)e.getValue()>0 && SafetyPolicy.canBlock(e.getKey()))
                map.put(e.getKey(),(Integer)e.getValue());
        }
        return map;
    }
    public static boolean strict(Context c) { return o(c).getBoolean("strict", false); }
    public static void enableStrict(Context c) { o(c).edit().putBoolean("strict",true).putString("strict_from_day",DayKeys.today()).apply(); }
    public static boolean strictLockedToday(Context c) {
        return strict(c) && DayKeys.sameDay(o(c).getString("strict_from_day", ""), DayKeys.today());
    }
    public static boolean disableStrict(Context c) {
        if (strictLockedToday(c)) return false;
        o(c).edit().putBoolean("strict",false).apply(); return true;
    }
    /** Saving in strict mode defers changes to next local day. */
    public static boolean set(Context c, String pkg, int minutes) {
        if (!SafetyPolicy.canBlock(pkg) || minutes < 0 || minutes > 1440) throw new IllegalArgumentException("Invalid app or limit");
        applyDue(c);
        if (strict(c)) {
            // create a pending change once, for next day; checked by applyDue.
            java.util.Calendar cal=java.util.Calendar.getInstance(); cal.add(java.util.Calendar.DAY_OF_YEAR,1);
            String tomorrow = DayKeys.forEpoch(cal.getTimeInMillis());
            o(c).edit().putString("changes_for_day",tomorrow).putInt("next_"+pkg,minutes).apply();
            return false;
        }
        SharedPreferences.Editor e=p(c).edit(); if(minutes==0) e.remove(pkg); else e.putInt(pkg,minutes); e.apply();
        return true;
    }
    public static Integer pending(Context c, String pkg) {
        if (!o(c).contains("next_"+pkg)) return null;
        return o(c).getInt("next_"+pkg,0);
    }
}
