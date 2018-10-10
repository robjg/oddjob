package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MultiBlockGateTest {

    @Test
    public void testBlockAndRelease() throws InterruptedException {

        MultiBlockGate gate = new MultiBlockGate();

        gate.await();

        gate.block();
        gate.block();

        assertThat(gate.getBlockers(), is(2));

        new Thread(() -> {
            while(gate.getBlocked() == 0) {
                Thread.yield();
            }
            assertThat(gate.getBlocked(), is(1));
            gate.unblock();
            gate.unblock();
        } ).start();

        gate.await();

        gate.block();

        assertThat(gate.getBlockers(), is(1));

        new Thread(() -> {
            while(gate.getBlocked() == 0) {
                Thread.yield();
            }
            assertThat(gate.getBlocked(), is(1));
            gate.unblock();
        } ).start();


        gate.await();
        gate.await();
        gate.await();
        gate.await();

        assertThat(gate.getBlockers(), is(0));
        assertThat(gate.getBlocked(), is(0));
    }

    @Test
    public void testUnBlockBlocksButIsThisAProblem() throws InterruptedException {

        MultiBlockGate gate = new MultiBlockGate();

        gate.await();

        gate.unblock();

        assertThat(gate.getBlockers(), is(-1));

        new Thread(() -> {
            while(gate.getBlocked() == 0) {
                Thread.yield();
            }
            assertThat(gate.getBlocked(), is(1));
            gate.block();
            gate.block();
            gate.unblock();
        } ).start();

        gate.await();

        gate.await();

        assertThat(gate.getBlockers(), is(0));
        assertThat(gate.getBlocked(), is(0));
    }

    @Test(expected = TimeoutException.class)
    public void testTimeout() throws InterruptedException, TimeoutException {

        MultiBlockGate test = new MultiBlockGate();

        test.block();

        test.await(1);
    }
}
