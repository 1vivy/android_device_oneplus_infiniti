package com.oplus.pluskey.service;

import com.oplus.pluskey.Constants;

/**
 * Validates the immutable framework gesture context and claims terminal actions exactly once.
 *
 * <p>The persisted high-water mark is scoped to one framework writer session. It is advanced before
 * any action runs, so receiver or app-process recreation cannot replay an action. A failed durable
 * write never reaches the action backend.
 */
public final class GestureContract {
    public static final long NO_SESSION = Long.MIN_VALUE;

    public enum Result {
        EXECUTED,
        DISABLED,
        BLOCKED_BY_CONTEXT,
        DUPLICATE,
        WRONG_USER,
        INVALID,
        PERSISTENCE_FAILED,
    }

    /** Context captured by the framework on the hardware key's initial down event. */
    public static final class Gesture {
        public final long sequenceId;
        public final int userId;
        public final boolean keyguardLocked;

        public Gesture(long sequenceId, int userId, boolean keyguardLocked) {
            this.sequenceId = sequenceId;
            this.userId = userId;
            this.keyguardLocked = keyguardLocked;
        }
    }

    /** Immutable persisted ledger state. */
    public static final class State {
        public final long writerSession;
        public final long lastSequenceId;

        public State(long writerSession, long lastSequenceId) {
            this.writerSession = writerSession;
            this.lastSequenceId = lastSequenceId;
        }
    }

    /** Small persistence boundary implemented by device-protected SharedPreferences in the app. */
    public interface Store {
        State read();

        boolean write(long writerSession, long sequenceId);
    }

    private final Store mStore;

    public GestureContract(Store store) {
        mStore = store;
    }

    public Result handle(
            Gesture gesture,
            int receivingUserId,
            long writerSession,
            boolean userUnlocked,
            int actionId,
            Runnable action) {
        if (!isValidForUser(gesture, receivingUserId) || writerSession == NO_SESSION) {
            return gesture != null && gesture.userId != receivingUserId
                    ? Result.WRONG_USER
                    : Result.INVALID;
        }
        if (!isKnownAction(actionId)) {
            return Result.INVALID;
        }

        State state = mStore.read();
        if (state.writerSession == writerSession && gesture.sequenceId <= state.lastSequenceId) {
            return Result.DUPLICATE;
        }
        if (!mStore.write(writerSession, gesture.sequenceId)) {
            return Result.PERSISTENCE_FAILED;
        }

        if (actionId == Constants.ACTION_NONE) {
            return Result.DISABLED;
        }
        if ((!userUnlocked || gesture.keyguardLocked) && !isDirectBootSafe(actionId)) {
            return Result.BLOCKED_BY_CONTEXT;
        }

        action.run();
        return Result.EXECUTED;
    }

    public static boolean isValidForUser(Gesture gesture, int receivingUserId) {
        return gesture != null && gesture.sequenceId > 0 && gesture.userId == receivingUserId;
    }

    private static boolean isKnownAction(int actionId) {
        return actionId == Constants.ACTION_UNSET || Constants.actionName(actionId) != null;
    }

    private static boolean isDirectBootSafe(int actionId) {
        switch (actionId) {
            case Constants.ACTION_NONE:
            case Constants.ACTION_SOUND_VIB:
            case Constants.ACTION_DND:
            case Constants.ACTION_FLASHLIGHT:
                return true;
            default:
                return false;
        }
    }
}
