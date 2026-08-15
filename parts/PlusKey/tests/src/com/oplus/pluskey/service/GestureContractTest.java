package com.oplus.pluskey.service;

import static org.junit.Assert.assertEquals;

import com.oplus.pluskey.Constants;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class GestureContractTest {
    private static final long SESSION = 41;
    private static final int USER = 10;

    @Test
    public void duplicateIsRejectedAfterReceiverRecreation() {
        FakeStore store = new FakeStore();
        AtomicInteger executions = new AtomicInteger();
        GestureContract.Gesture gesture = gesture(7, USER, false);

        assertEquals(
                GestureContract.Result.EXECUTED,
                new GestureContract(store)
                        .handle(
                                gesture,
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_FLASHLIGHT,
                                executions::incrementAndGet));
        assertEquals(
                GestureContract.Result.DUPLICATE,
                new GestureContract(store)
                        .handle(
                                gesture,
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_FLASHLIGHT,
                                executions::incrementAndGet));

        assertEquals(1, executions.get());
    }

    @Test
    public void originatingUserMustMatchReceivingUser() {
        FakeStore store = new FakeStore();
        AtomicInteger executions = new AtomicInteger();
        GestureContract.Gesture gesture = gesture(8, USER, false);

        assertEquals(
                GestureContract.Result.WRONG_USER,
                new GestureContract(store)
                        .handle(
                                gesture,
                                11,
                                SESSION,
                                true,
                                Constants.ACTION_FLASHLIGHT,
                                executions::incrementAndGet));
        assertEquals(
                GestureContract.Result.EXECUTED,
                new GestureContract(store)
                        .handle(
                                gesture,
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_FLASHLIGHT,
                                executions::incrementAndGet));

        assertEquals(1, executions.get());
    }

    @Test
    public void immutableKeyguardContextBlocksUnsafeAction() {
        FakeStore store = new FakeStore();
        AtomicInteger executions = new AtomicInteger();

        assertEquals(
                GestureContract.Result.BLOCKED_BY_CONTEXT,
                new GestureContract(store)
                        .handle(
                                gesture(9, USER, true),
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_OPEN_APP,
                                executions::incrementAndGet));
        assertEquals(
                GestureContract.Result.DUPLICATE,
                new GestureContract(store)
                        .handle(
                                gesture(9, USER, false),
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_OPEN_APP,
                                executions::incrementAndGet));

        assertEquals(0, executions.get());
    }

    @Test
    public void directBootAllowsOnlyDeviceProtectedActions() {
        FakeStore store = new FakeStore();
        AtomicInteger executions = new AtomicInteger();
        GestureContract contract = new GestureContract(store);

        assertEquals(
                GestureContract.Result.BLOCKED_BY_CONTEXT,
                contract.handle(
                        gesture(10, USER, false),
                        USER,
                        SESSION,
                        false,
                        Constants.ACTION_SCREENSHOT,
                        executions::incrementAndGet));
        assertEquals(
                GestureContract.Result.EXECUTED,
                contract.handle(
                        gesture(11, USER, true),
                        USER,
                        SESSION,
                        false,
                        Constants.ACTION_FLASHLIGHT,
                        executions::incrementAndGet));

        assertEquals(1, executions.get());
    }

    @Test
    public void explicitlyDisabledActionIsConsumedWithoutDispatch() {
        AtomicInteger executions = new AtomicInteger();

        assertEquals(
                GestureContract.Result.DISABLED,
                new GestureContract(new FakeStore())
                        .handle(
                                gesture(12, USER, false),
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_NONE,
                                executions::incrementAndGet));

        assertEquals(0, executions.get());
    }

    @Test
    public void sequenceCanRestartOnlyInANewWriterSession() {
        FakeStore store = new FakeStore();
        AtomicInteger executions = new AtomicInteger();
        GestureContract contract = new GestureContract(store);

        assertEquals(
                GestureContract.Result.EXECUTED,
                contract.handle(
                        gesture(50, USER, false),
                        USER,
                        SESSION,
                        true,
                        Constants.ACTION_DND,
                        executions::incrementAndGet));
        assertEquals(
                GestureContract.Result.DUPLICATE,
                contract.handle(
                        gesture(1, USER, false),
                        USER,
                        SESSION,
                        true,
                        Constants.ACTION_DND,
                        executions::incrementAndGet));
        assertEquals(
                GestureContract.Result.EXECUTED,
                contract.handle(
                        gesture(1, USER, false),
                        USER,
                        SESSION + 1,
                        true,
                        Constants.ACTION_DND,
                        executions::incrementAndGet));

        assertEquals(2, executions.get());
    }

    @Test
    public void persistenceFailureNeverExecutesAndCanBeRetried() {
        FakeStore store = new FakeStore();
        store.failNextWrite = true;
        AtomicInteger executions = new AtomicInteger();
        GestureContract.Gesture gesture = gesture(13, USER, false);

        assertEquals(
                GestureContract.Result.PERSISTENCE_FAILED,
                new GestureContract(store)
                        .handle(
                                gesture,
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_DND,
                                executions::incrementAndGet));
        assertEquals(
                GestureContract.Result.EXECUTED,
                new GestureContract(store)
                        .handle(
                                gesture,
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_DND,
                                executions::incrementAndGet));

        assertEquals(1, executions.get());
    }

    @Test
    public void plusKeyReinstallGetsFreshPackagePrivateLedger() {
        FakeStore installedState = new FakeStore();
        new GestureContract(installedState)
                .handle(
                        gesture(14, USER, false),
                        USER,
                        SESSION,
                        true,
                        Constants.ACTION_DND,
                        () -> {});

        FakeStore reinstalledState = new FakeStore();
        assertEquals(
                GestureContract.Result.EXECUTED,
                new GestureContract(reinstalledState)
                        .handle(
                                gesture(14, USER, false),
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_DND,
                                () -> {}));
    }

    @Test
    public void missingWriterSessionIsRejectedWithoutPersistence() {
        FakeStore store = new FakeStore();

        assertEquals(
                GestureContract.Result.INVALID,
                new GestureContract(store)
                        .handle(
                                gesture(15, USER, false),
                                USER,
                                GestureContract.NO_SESSION,
                                true,
                                Constants.ACTION_DND,
                                () -> {}));
        assertEquals(0, store.writeCount);
    }

    @Test
    public void malformedSequenceIsRejectedWithoutPersistence() {
        FakeStore store = new FakeStore();

        assertEquals(
                GestureContract.Result.INVALID,
                new GestureContract(store)
                        .handle(
                                gesture(0, USER, false),
                                USER,
                                SESSION,
                                true,
                                Constants.ACTION_DND,
                                () -> {}));
        assertEquals(0, store.writeCount);
    }

    private static GestureContract.Gesture gesture(long sequence, int user, boolean locked) {
        return new GestureContract.Gesture(sequence, user, locked);
    }

    private static final class FakeStore implements GestureContract.Store {
        long session = GestureContract.NO_SESSION;
        long sequence;
        int writeCount;
        boolean failNextWrite;

        @Override
        public GestureContract.State read() {
            return new GestureContract.State(session, sequence);
        }

        @Override
        public boolean write(long newSession, long newSequence) {
            writeCount++;
            if (failNextWrite) {
                failNextWrite = false;
                return false;
            }
            session = newSession;
            sequence = newSequence;
            return true;
        }
    }
}
