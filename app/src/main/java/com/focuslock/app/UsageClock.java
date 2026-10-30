package com.focuslock.app;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import java.util.Calendar;

/** Estimates daily foreground time from UsageEvents; Android may omit events. */
public final class UsageClock {
    private UsageClock() {}
    public static long usedMillis(Context ctx, String pkg) {
        if (!Permissions.usage(ctx) || pkg == null) return 0L;
        UsageStatsManager manager=(UsageStatsManager)ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        if(manager==null)return 0L;
        Calendar day=Calendar.getInstance();
        day.set(Calendar.HOUR_OF_DAY,0);day.set(Calendar.MINUTE,0);day.set(Calendar.SECOND,0);day.set(Calendar.MILLISECOND,0);
        long start=day.getTimeInMillis(),now=System.currentTimeMillis();
        UsageEvents events;
        // Query the preceding day to account for app sessions spanning midnight.
        // Usage events are only kept by Android for a limited time.
        try {events=manager.queryEvents(start-86_400_000L,now);}catch(RuntimeException ex){return 0L;}
        if(events==null)return 0L;
        IntervalCounter counter=new IntervalCounter(start);
        UsageEvents.Event event=new UsageEvents.Event();
        while(events.hasNextEvent()) {
            events.getNextEvent(event);
            int type=event.getEventType();
            if(type==UsageEvents.Event.SCREEN_NON_INTERACTIVE || type==UsageEvents.Event.DEVICE_SHUTDOWN) {
                counter.stop(event.getTimeStamp());continue;
            }
            if(type==UsageEvents.Event.ACTIVITY_RESUMED) {
                if(pkg.equals(event.getPackageName())) counter.resume(event.getTimeStamp(),event.getClassName());
                else counter.stop(event.getTimeStamp());
            } else if(pkg.equals(event.getPackageName()) &&
                (type==UsageEvents.Event.ACTIVITY_PAUSED || type==UsageEvents.Event.ACTIVITY_STOPPED)) {
                counter.pause(event.getTimeStamp(),event.getClassName());
            }
        }
        return counter.totalUntil(now);
    }
}
