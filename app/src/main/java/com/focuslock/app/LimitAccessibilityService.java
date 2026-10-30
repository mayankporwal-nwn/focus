package com.focuslock.app;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/** Redirects exhausted selected apps to HOME (best-effort; never claims device-owner enforcement). */
public class LimitAccessibilityService extends AccessibilityService {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private String activePackage;
    private long lastCheckElapsed=0;
    private long lastBlockedToast=0;
    private final Runnable watchdog=new Runnable() {
        @Override public void run() {
            checkActive(false);
            handler.postDelayed(this,1200L);
        }
    };
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        handler.removeCallbacks(watchdog);
        handler.post(watchdog);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event==null || event.getPackageName()==null) return;
        if(event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        String pkg=event.getPackageName().toString();
        // Brief system overlays (notification shade, keyboard, permission dialogs)
        // shouldn't erase the last monitored foreground package.
        if(pkg.equals("com.android.systemui") || pkg.equals("com.google.android.inputmethod.latin")
            || pkg.equals("com.android.inputmethod.latin") || pkg.equals("com.android.permissioncontroller")) return;
        activePackage=pkg;
        checkActive(true);
    }
    private void checkActive(boolean immediate) {
        String pkg=activePackage;
        if(pkg==null || !SafetyPolicy.canBlock(pkg) || pkg.equals(getPackageName())) return;
        long elapsed=SystemClock.elapsedRealtime();
        if(!immediate && elapsed-lastCheckElapsed<1000L) return;
        lastCheckElapsed=elapsed;
        int limit=LimitStore.get(this,pkg);
        if(limit==0 || !Permissions.usage(this)) return;
        if(UsageClock.usedMillis(this,pkg)>=limit*60_000L) {
            if(performGlobalAction(GLOBAL_ACTION_HOME)) {
                activePackage=null;
                if(elapsed-lastBlockedToast>3500L) {
                    lastBlockedToast=elapsed;
                    Toast.makeText(this,"Daily limit reached. Choose something that matters to you.",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { handler.removeCallbacks(watchdog); super.onDestroy(); }
}
