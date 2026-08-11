package com.oplus.pluskey.actions;

import static com.google.common.truth.Truth.assertThat;

import com.oplus.pluskey.Constants;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ActionDispatcherTest {
    private static final int[] ACTIONS = {
            Constants.ACTION_NONE,
            Constants.ACTION_SOUND_VIB,
            Constants.ACTION_DND,
            Constants.ACTION_CAMERA,
            Constants.ACTION_FLASHLIGHT,
            Constants.ACTION_SCREENSHOT,
            Constants.ACTION_RECORDER,
            Constants.ACTION_TRANSLATE,
            Constants.ACTION_OPEN_APP,
    };

    @Test
    public void registryIsCompleteAndRoundTripsPersistedNames() {
        for (int action : ACTIONS) {
            String name = Constants.actionName(action);
            assertThat(name).isNotNull();
            assertThat(Constants.actionFromName(name)).isEqualTo(action);
            assertThat(ActionDispatcher.forId(action)).isNotNull();
        }
        assertThat(Constants.actionFromName("unknown")).isEqualTo(Constants.ACTION_UNSET);
        assertThat(ActionDispatcher.forId(99)).isNull();
    }

    @Test
    public void dispatchInvokesExactlyOneFakeBackendAction() {
        AtomicInteger backendLookups = new AtomicInteger();
        AtomicInteger invocations = new AtomicInteger();
        ActionDispatcher dispatcher = new ActionDispatcher(null, actionId -> {
            backendLookups.incrementAndGet();
            assertThat(actionId).isEqualTo(Constants.ACTION_FLASHLIGHT);
            return context -> invocations.incrementAndGet();
        });

        dispatcher.dispatch(Constants.ACTION_FLASHLIGHT);

        assertThat(backendLookups.get()).isEqualTo(1);
        assertThat(invocations.get()).isEqualTo(1);
    }
}
