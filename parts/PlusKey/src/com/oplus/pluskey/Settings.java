package com.oplus.pluskey;

import android.content.Context;
import android.provider.Settings.Secure;

import java.util.LinkedHashSet;
import java.util.Set;

/** Typed persistence wrapper for the per-user Plus Key settings. */
public final class Settings {
    private Settings() {}

    /** Returns ACTION_UNSET until the user explicitly chooses an action. */
    public static int getAction(Context ctx) {
        return getAction(ctx, true);
    }

    public static int getAction(Context ctx, boolean longPress) {
        String value = Secure.getString(ctx.getContentResolver(),
                longPress ? Constants.KEY_PLUSKEY_ACTION : Constants.KEY_PLUSKEY_SHORT_ACTION);
        return Constants.actionFromName(value);
    }

    public static void setAction(Context ctx, int action) {
        setAction(ctx, true, action);
    }

    public static void setAction(Context ctx, boolean longPress, int action) {
        String value = Constants.actionName(action);
        if (value == null) {
            throw new IllegalArgumentException("Unknown Plus Key action " + action);
        }
        Secure.putString(ctx.getContentResolver(),
                longPress ? Constants.KEY_PLUSKEY_ACTION : Constants.KEY_PLUSKEY_SHORT_ACTION,
                value);
    }

    public static int getCameraMode(Context ctx) {
        return Secure.getInt(ctx.getContentResolver(),
                Constants.KEY_PLUSKEY_CAMERA_MODE, Constants.CAM_MODE_PHOTO);
    }

    public static void setCameraMode(Context ctx, int mode) {
        Secure.putInt(ctx.getContentResolver(), Constants.KEY_PLUSKEY_CAMERA_MODE, mode);
    }

    public static boolean isShortPressScreenOnOnly(Context ctx) {
        return Secure.getInt(ctx.getContentResolver(),
                Constants.KEY_PLUSKEY_SHORT_SCREEN_ON_ONLY, 0) == 1;
    }

    public static void setShortPressScreenOnOnly(Context ctx, boolean enabled) {
        Secure.putInt(ctx.getContentResolver(),
                Constants.KEY_PLUSKEY_SHORT_SCREEN_ON_ONLY, enabled ? 1 : 0);
    }

    public static boolean isCameraTriggerEnabled(Context ctx) {
        return Secure.getInt(ctx.getContentResolver(),
                Constants.KEY_PLUSKEY_CAMERA_TRIGGER, 0) == 1;
    }

    public static void setCameraTriggerEnabled(Context ctx, boolean enabled) {
        Secure.putInt(ctx.getContentResolver(),
                Constants.KEY_PLUSKEY_CAMERA_TRIGGER, enabled ? 1 : 0);
    }

    public static Set<String> getCameraTriggerPkgs(Context ctx) {
        String raw = Secure.getString(ctx.getContentResolver(),
                Constants.KEY_PLUSKEY_CAMERA_TRIGGER_PKGS);
        Set<String> out = new LinkedHashSet<>();
        if (raw == null || raw.isEmpty()) return out;
        for (String pkg : raw.split(",")) {
            if (!pkg.isEmpty()) out.add(pkg);
        }
        return out;
    }

    public static void setCameraTriggerPkgs(Context ctx, Set<String> pkgs) {
        StringBuilder sb = new StringBuilder();
        for (String pkg : pkgs) {
            if (pkg == null || pkg.isEmpty()) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append(pkg);
        }
        Secure.putString(ctx.getContentResolver(),
                Constants.KEY_PLUSKEY_CAMERA_TRIGGER_PKGS, sb.toString());
    }

    public static String getOpenAppPkg(Context ctx) {
        return Secure.getString(ctx.getContentResolver(), Constants.KEY_PLUSKEY_OPEN_APP_PKG);
    }

    public static void setOpenAppPkg(Context ctx, String pkg) {
        Secure.putString(ctx.getContentResolver(), Constants.KEY_PLUSKEY_OPEN_APP_PKG, pkg);
    }
}
