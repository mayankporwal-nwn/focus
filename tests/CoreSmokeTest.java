import com.focuslock.app.DayKeys;
import com.focuslock.app.SafetyPolicy;
import com.focuslock.app.FocusContent;
import com.focuslock.app.IntervalCounter;

/** Plain Java smoke test, runnable without an Android SDK. */
public final class CoreSmokeTest {
    private static void ok(boolean cond,String why) {
        if(!cond) throw new AssertionError(why);
    }
    public static void main(String[] args) {
        ok(DayKeys.today().matches("\\d{4}-\\d{2}-\\d{2}"),"local day format");
        ok(DayKeys.sameDay("2026-10-30","2026-10-30"),"equal days");
        ok(!DayKeys.sameDay("2026-10-29","2026-10-30"),"different days");
        for(String pkg:new String[]{"com.focuslock.app","com.android.settings","com.android.systemui","com.google.android.dialer","com.miui.home"})
            ok(!SafetyPolicy.canBlock(pkg),"protected package was blockable: "+pkg);
        for(String pkg:new String[]{"com.instagram.android","com.google.android.youtube","com.whatsapp"})
            ok(SafetyPolicy.canBlock(pkg),"social package not blockable: "+pkg);
        ok(FocusContent.IDEAS.length>=15,"replacement alternatives");
        ok(FocusContent.TIPS.length>=10,"focus tips");
        ok(FocusContent.ROUTINE_NAMES.length==FocusContent.ROUTINE_HINTS.length,"routine hints");
        IntervalCounter acrossMidnight=new IntervalCounter(1000L);
        acrossMidnight.resume(800,"A");
        acrossMidnight.pause(1800,"A");
        ok(acrossMidnight.totalUntil(2000)==800,"cross-midnight interval clips at start");
        IntervalCounter sameAppNav=new IntervalCounter(0L);
        sameAppNav.resume(100,"Old");
        sameAppNav.resume(150,"New");
        sameAppNav.pause(175,"Old");
        sameAppNav.pause(250,"New");
        ok(sameAppNav.totalUntil(300)==150,"in-app navigation stale pause");
        IntervalCounter interrupted=new IntervalCounter(0L);
        interrupted.resume(50,"A");interrupted.stop(200);
        ok(interrupted.totalUntil(300)==150,"screen off interrupts usage");
        System.out.println("PASS: date keys, core safety policy, offline content catalog");
    }
}
