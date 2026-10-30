package com.focuslock.app;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Never offer a lock for core OS, dialer, settings, or our own app. */
public final class SafetyPolicy {
    private SafetyPolicy() {}
    private static final Set<String> NEVER_BLOCK = new HashSet<>(Arrays.asList(
        "com.focuslock.app", "com.android.settings", "com.android.systemui",
        "com.android.phone", "com.android.dialer", "com.google.android.dialer",
        "com.android.contacts", "com.google.android.contacts",
        "com.android.launcher3", "com.google.android.apps.nexuslauncher",
        "com.miui.home", "com.sec.android.app.launcher", "com.oppo.launcher",
        "com.vivo.launcher", "com.huawei.android.launcher", "com.android.incallui",
        "com.google.android.apps.safetyhub", "com.android.emergency",
        "com.google.android.apps.turbo", "com.android.permissioncontroller",
        "com.google.android.permissioncontroller", "com.android.packageinstaller",
        "com.google.android.packageinstaller"
    ));
    public static boolean canBlock(String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) return false;
        if (NEVER_BLOCK.contains(pkg)) return false;
        return !pkg.startsWith("com.android.settings")
            && !pkg.startsWith("com.android.launcher")
            && !pkg.startsWith("com.android.emergency")
            && !pkg.startsWith("com.google.android.permissioncontroller");
    }
}
