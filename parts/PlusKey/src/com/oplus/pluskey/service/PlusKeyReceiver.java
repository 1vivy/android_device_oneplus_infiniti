package com.oplus.pluskey.service;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.oplus.pluskey.Constants;
import com.oplus.pluskey.Settings;
import com.oplus.pluskey.actions.ActionDispatcher;

import java.util.List;
import java.util.Set;

/** Receives Plus Key broadcasts from the patched PhoneWindowManager. */
public class PlusKeyReceiver extends BroadcastReceiver {

    public static final String ACTION_SHORT_PRESS = "com.oplus.pluskey.SHORT_PRESS";
    public static final String ACTION_LONG_PRESS = "com.oplus.pluskey.LONG_PRESS";
    public static final String ACTION_CAMERA_TRIGGER_DOWN = "com.oplus.pluskey.CAMERA_TRIGGER_DOWN";
    public static final String ACTION_CAMERA_TRIGGER_UP = "com.oplus.pluskey.CAMERA_TRIGGER_UP";
    public static final String EXTRA_SEQUENCE_ID = "com.oplus.pluskey.extra.SEQUENCE_ID";
    public static final String EXTRA_USER_ID = "com.oplus.pluskey.extra.USER_ID";
    public static final String EXTRA_KEYGUARD_LOCKED = "com.oplus.pluskey.extra.KEYGUARD_LOCKED";

    private static final String LEDGER_PREFS = "pluskey_gesture_ledger";
    private static final String PREF_WRITER_SESSION = "writer_session";
    private static final String PREF_LAST_SEQUENCE = "last_sequence";
    private static final int INVALID_USER_ID = -10000;

    private static boolean sCameraKeyDown;
    private static long sSuppressGesturesUntil;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        boolean longPress = ACTION_LONG_PRESS.equals(action);
        boolean shortPress = ACTION_SHORT_PRESS.equals(action);
        boolean cameraDown = ACTION_CAMERA_TRIGGER_DOWN.equals(action);
        boolean cameraUp = ACTION_CAMERA_TRIGGER_UP.equals(action);
        if (!longPress && !shortPress && !cameraDown && !cameraUp) return;

        GestureContract.Gesture gesture = readGesture(intent);
        int receivingUserId = ctx.getUserId();
        if (!GestureContract.isValidForUser(gesture, receivingUserId)) {
            Log.w(Constants.TAG, "ignoring gesture with invalid framework context");
            return;
        }

        if (cameraDown || cameraUp) {
            handleCameraTriggerEvent(ctx, cameraDown);
            return;
        }

        int actionId = Settings.getAction(ctx, longPress);
        Log.i(
                Constants.TAG,
                (longPress ? "long-press" : "short-press")
                        + " action="
                        + Constants.actionName(actionId));

        UserManager userManager = ctx.getSystemService(UserManager.class);
        boolean userUnlocked = userManager == null || userManager.isUserUnlocked();
        int bootCount =
                Global.getInt(ctx.getContentResolver(), Global.BOOT_COUNT, Integer.MIN_VALUE);
        long writerSession =
                bootCount == Integer.MIN_VALUE ? GestureContract.NO_SESSION : bootCount;
        GestureContract.Result result =
                new GestureContract(new PreferenceStore(ctx))
                        .handle(
                                gesture,
                                receivingUserId,
                                writerSession,
                                userUnlocked,
                                actionId,
                                () -> executeAction(ctx, actionId, longPress));
        Log.i(Constants.TAG, "gesture sequence=" + gesture.sequenceId + " result=" + result);
    }

    private GestureContract.Gesture readGesture(Intent intent) {
        if (!intent.hasExtra(EXTRA_SEQUENCE_ID)
                || !intent.hasExtra(EXTRA_USER_ID)
                || !intent.hasExtra(EXTRA_KEYGUARD_LOCKED)) {
            return null;
        }
        return new GestureContract.Gesture(
                intent.getLongExtra(EXTRA_SEQUENCE_ID, 0),
                intent.getIntExtra(EXTRA_USER_ID, INVALID_USER_ID),
                intent.getBooleanExtra(EXTRA_KEYGUARD_LOCKED, false));
    }

    private void executeAction(Context ctx, int actionId, boolean longPress) {
        if (sCameraKeyDown || SystemClock.uptimeMillis() < sSuppressGesturesUntil) {
            Log.i(Constants.TAG, "gesture suppressed by camera trigger");
            return;
        }
        if (!longPress && Settings.isShortPressScreenOnOnly(ctx) && !isScreenOn(ctx)) {
            Log.i(Constants.TAG, "short-press ignored while screen is off");
            return;
        }

        AssistantRoleClearer.clearOnce(ctx);
        if (actionId == Constants.ACTION_UNSET) {
            // First-run is an explicit unlocked-only action. Reinstall clears the package-private
            // sequence ledger; the per-user action selection remains in Settings.Secure.
            Intent settings =
                    new Intent("com.oplus.pluskey.SETTINGS")
                            .setPackage(ctx.getPackageName())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(settings);
            return;
        }
        new ActionDispatcher(ctx).dispatch(actionId);
    }

    private void handleCameraTriggerEvent(Context ctx, boolean down) {
        if (down) {
            if (sCameraKeyDown) return;
            if (!Settings.isCameraTriggerEnabled(ctx) || !isForegroundCameraTriggerApp(ctx)) {
                return;
            }
            if (injectCameraKey(ctx, KeyEvent.ACTION_DOWN)) {
                sCameraKeyDown = true;
                Log.i(Constants.TAG, "camera trigger down");
            }
            return;
        }

        if (!sCameraKeyDown) return;
        injectCameraKey(ctx, KeyEvent.ACTION_UP);
        sCameraKeyDown = false;
        sSuppressGesturesUntil = SystemClock.uptimeMillis() + 1000;
        Log.i(Constants.TAG, "camera trigger up");
    }

    private boolean isScreenOn(Context ctx) {
        PowerManager pm = ctx.getSystemService(PowerManager.class);
        return pm == null || pm.isInteractive();
    }

    private boolean isForegroundCameraTriggerApp(Context ctx) {
        ActivityManager am = ctx.getSystemService(ActivityManager.class);
        if (am == null) return false;

        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) return false;

        ComponentName top = tasks.get(0).topActivity;
        if (top == null) return false;

        String pkg = top.getPackageName();
        if (ctx.getPackageName().equals(pkg)) return false;

        Set<String> selected = Settings.getCameraTriggerPkgs(ctx);
        return selected.contains(pkg) && packageStillHasCameraPermission(ctx, pkg);
    }

    private boolean packageStillHasCameraPermission(Context ctx, String pkg) {
        try {
            return hasCameraPermission(ctx.getPackageManager(), pkg);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(Constants.TAG, "Selected camera trigger package missing " + pkg, e);
            return false;
        }
    }

    private boolean hasCameraPermission(PackageManager pm, String pkg)
            throws PackageManager.NameNotFoundException {
        PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);
        String[] permissions = info.requestedPermissions;
        if (permissions == null) return false;
        for (String permission : permissions) {
            if (Manifest.permission.CAMERA.equals(permission)) return true;
        }
        return false;
    }

    private boolean injectCameraKey(Context ctx, int action) {
        InputManager im = ctx.getSystemService(InputManager.class);
        if (im == null) return false;

        long now = SystemClock.uptimeMillis();
        return im.injectInputEvent(
                cameraKey(now, action), InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    private KeyEvent cameraKey(long eventTime, int action) {
        return new KeyEvent(
                eventTime,
                eventTime,
                action,
                KeyEvent.KEYCODE_CAMERA,
                0,
                0,
                KeyCharacterMap.VIRTUAL_KEYBOARD,
                0,
                KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                InputDevice.SOURCE_KEYBOARD);
    }

    private static final class PreferenceStore implements GestureContract.Store {
        private final SharedPreferences mPreferences;

        PreferenceStore(Context context) {
            mPreferences = context.getSharedPreferences(LEDGER_PREFS, Context.MODE_PRIVATE);
        }

        @Override
        public GestureContract.State read() {
            return new GestureContract.State(
                    mPreferences.getLong(PREF_WRITER_SESSION, GestureContract.NO_SESSION),
                    mPreferences.getLong(PREF_LAST_SEQUENCE, 0));
        }

        @Override
        public boolean write(long writerSession, long sequenceId) {
            return mPreferences
                    .edit()
                    .putLong(PREF_WRITER_SESSION, writerSession)
                    .putLong(PREF_LAST_SEQUENCE, sequenceId)
                    .commit();
        }
    }
}
