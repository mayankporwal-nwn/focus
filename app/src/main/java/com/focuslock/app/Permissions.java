package com.focuslock.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Process;
import android.view.accessibility.AccessibilityManager;
import java.util.List;

public final class Permissions {
    private Permissions() {}
    public static boolean usage(Context c) {
        AppOpsManager ops=(AppOpsManager)c.getSystemService(Context.APP_OPS_SERVICE);
        return ops!=null && ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),c.getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }
    public static boolean blocking(Context c) {
        AccessibilityManager am=(AccessibilityManager)c.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if(am == null) return false;
        List<AccessibilityServiceInfo> services=am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for(AccessibilityServiceInfo s:services) {
            if(s.getResolveInfo()!=null && s.getResolveInfo().serviceInfo!=null
                && c.getPackageName().equals(s.getResolveInfo().serviceInfo.packageName)
                && LimitAccessibilityService.class.getName().equals(s.getResolveInfo().serviceInfo.name)) return true;
        }
        return false;
    }
}
