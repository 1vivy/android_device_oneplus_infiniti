package com.oplus.pluskey;

import android.view.KeyEvent;

public final class Constants {
    private Constants() {}

    public static final String TAG = "PlusKey";

    /** The hardware key the OnePlus 15 "Plus Key" reports (BTN_TRIGGER_HAPPY32). */
    public static final int PLUS_KEY_KEYCODE = KeyEvent.KEYCODE_ASSIST;

    /** Long-press threshold. Below this is treated as a short-press. */
    public static final long LONG_PRESS_MS = 400L;

    // ===== action ids — persisted as int in Settings.System =====
    public static final int ACTION_NONE          = 0;
    public static final int ACTION_SOUND_VIB     = 1;
    public static final int ACTION_DND           = 2;
    public static final int ACTION_CAMERA        = 3;
    public static final int ACTION_FLASHLIGHT    = 4;
    public static final int ACTION_SCREENSHOT    = 5;
    public static final int ACTION_RECORDER      = 6;
    public static final int ACTION_TRANSLATE     = 7;
    public static final int ACTION_OPEN_APP      = 8;

    /** Sentinel returned by Settings.getAction() when the user has never
     *  picked an action. Distinct from ACTION_NONE (an explicit "do nothing"
     *  choice). On long-press in this state the receiver opens the picker
     *  Activity instead of dispatching anything. */
    public static final int ACTION_UNSET = -1;

    /** Initial value shown on the chip row when the user has never picked
     *  anything. Not persisted until they tap "Set". */
    public static final int DEFAULT_DISPLAY_ACTION = ACTION_SOUND_VIB;

    // ===== Settings.Secure keys =====
    public static final String KEY_PLUSKEY_SHORT_ACTION = "plus_key_short_action";
    public static final String KEY_PLUSKEY_ACTION       = "plus_key_long_action";
    public static final String KEY_PLUSKEY_CAMERA_MODE  = "pluskey_camera_mode";
    public static final String KEY_PLUSKEY_SHORT_SCREEN_ON_ONLY =
            "pluskey_short_screen_on_only";
    public static final String KEY_PLUSKEY_CAMERA_TRIGGER =
            "pluskey_camera_trigger";
    public static final String KEY_PLUSKEY_CAMERA_TRIGGER_PKGS =
            "pluskey_camera_trigger_pkgs";
    /** Package name of the user-chosen "Open app" target (string). */
    public static final String KEY_PLUSKEY_OPEN_APP_PKG = "pluskey_open_app_pkg";

    // ===== camera mode IDs (passed to camera intent extras) =====
    public static final int CAM_MODE_PHOTO     = 0;
    public static final int CAM_MODE_VIDEO     = 1;
    public static final int CAM_MODE_SELFIE    = 2;
    public static final int CAM_MODE_PORTRAIT  = 3;
    public static final int CAM_MODE_MACRO     = 4;
    public static final int CAM_MODE_SLO_MO    = 5;

    public static String actionName(int action) {
        switch (action) {
            case ACTION_NONE: return "none";
            case ACTION_SOUND_VIB: return "sound_vibrate";
            case ACTION_DND: return "dnd";
            case ACTION_CAMERA: return "camera";
            case ACTION_FLASHLIGHT: return "flashlight";
            case ACTION_SCREENSHOT: return "screenshot";
            case ACTION_RECORDER: return "recorder";
            case ACTION_TRANSLATE: return "translate";
            case ACTION_OPEN_APP: return "open_app";
            default: return null;
        }
    }

    public static int actionFromName(String value) {
        if (value == null) return ACTION_UNSET;
        switch (value) {
            case "none": return ACTION_NONE;
            case "sound_vibrate": return ACTION_SOUND_VIB;
            case "dnd": return ACTION_DND;
            case "camera": return ACTION_CAMERA;
            case "flashlight": return ACTION_FLASHLIGHT;
            case "screenshot": return ACTION_SCREENSHOT;
            case "recorder": return ACTION_RECORDER;
            case "translate": return ACTION_TRANSLATE;
            case "open_app": return ACTION_OPEN_APP;
            default: return ACTION_UNSET;
        }
    }
}
