package com.oplus.pluskey.actions;

import android.content.Context;
import android.util.Log;

import com.oplus.pluskey.Constants;

/** Resolves a persisted typed action and invokes exactly one backend. */
public final class ActionDispatcher {
    public interface Backend {
        Action forId(int actionId);
    }

    private final Context mCtx;
    private final Backend mBackend;

    public ActionDispatcher(Context ctx) {
        this(ctx.getApplicationContext(), ActionDispatcher::createAction);
    }

    /** Test seam: a fake backend can observe dispatch without device side effects. */
    public ActionDispatcher(Context ctx, Backend backend) {
        mCtx = ctx;
        mBackend = backend;
    }

    public void dispatch(int actionId) {
        Action action = mBackend.forId(actionId);
        if (action == null) {
            Log.w(Constants.TAG, "no handler for action=" + Constants.actionName(actionId));
            return;
        }
        Log.i(Constants.TAG, "dispatch action=" + Constants.actionName(actionId));
        try {
            action.run(mCtx);
        } catch (RuntimeException e) {
            Log.e(Constants.TAG, "action failed: " + Constants.actionName(actionId), e);
        }
    }

    public static Action forId(int id) {
        return createAction(id);
    }

    private static Action createAction(int id) {
        switch (id) {
            case Constants.ACTION_NONE: return new NoOpAction();
            case Constants.ACTION_SOUND_VIB: return new SoundVibrationAction();
            case Constants.ACTION_DND: return new DndAction();
            case Constants.ACTION_CAMERA: return new CameraAction();
            case Constants.ACTION_FLASHLIGHT: return new FlashlightAction();
            case Constants.ACTION_SCREENSHOT: return new ScreenshotAction();
            case Constants.ACTION_RECORDER: return new RecorderAction();
            case Constants.ACTION_TRANSLATE: return new TranslateAction();
            case Constants.ACTION_OPEN_APP: return new OpenAppAction();
            default: return null;
        }
    }
}
