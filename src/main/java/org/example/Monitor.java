package org.example;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor {
    /*
     * ------------
     * Data members
     * ------------
     */

    private enum Status {THINKING, HUNGRY, EATING}

    private int N; // this is for the number of the philosophors
    private boolean mutex_talk;
    private Status state[];
    private Lock lock = new ReentrantLock(); // this is for solving the starvation
    private Condition self[];  // this is the condition variable
    private Object talkLock = new Object();

    /**
     * Constructor
     */
    public Monitor(int piNumberOfPhilosophers) {
        // TODO:
        this.N = piNumberOfPhilosophers;
        state = new Status[N];
        self = new Condition[N];
        this.mutex_talk = true;
        //initialization code
        for (int i = 0; i < piNumberOfPhilosophers; i++) {
            state[i] = Status.THINKING;
            this.self[i] = lock.newCondition();
        }
    }


    /*
     * -------------------------------
     * User-defined monitor procedures
     * -------------------------------
     */


    /*
     *
     *if the Philosopher is hungry
     *and his left hand side philosopher is not eating
     *and right hand side philosopher is not eating as well
     *then he can eat
     */

    public void checkForRequest(int i) {
        if ((state[((i - 1 + N) % N)] != Status.EATING) && (state[i] == Status.HUNGRY) && (state[((i + 1) % N)] != Status.EATING)) {
            state[i] = Status.EATING;
            self[i].signal();
        }
    }

    /**
     * Grants request (returns) to eat when both chopsticks/forks are available.
     * Else forces the philosopher to wait()
     */
    public void pickUp(int piTID) {
        piTID = piTID - 1;
        // ...
        try{
            lock.lock();
            state[piTID] = Status.HUNGRY;
            checkForRequest(piTID);
            if (state[piTID] != Status.EATING) {
                try {
                    self[piTID].await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * When a given philosopher's done eating, they put the chopstiks/forks down
     * and let others know they are available.
     */
    public void putDown(int piTID) {
        lock.lock();
        piTID = piTID - 1;
        // ...
        state[piTID] = Status.THINKING;
        checkForRequest((piTID - 1 + N) % N);
        checkForRequest((piTID + 1) % N);
        lock.unlock();
    }


    /**
     * Only one philopher at a time is allowed to philosophy
     * (while she is not eating).
     */
    public void requestTalk() {
        // ...

        synchronized (talkLock) {
            while (mutex_talk == false) {
                try {
                    talkLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        this.mutex_talk = false;
    }

    /**
     * When one philosopher is done talking stuff, others
     * can feel free to start talking.
     */
    public void endTalk() {
        // ...
        synchronized (talkLock) {
            this.mutex_talk = true;
            talkLock.notify();
        }
    }

}

// EOF

