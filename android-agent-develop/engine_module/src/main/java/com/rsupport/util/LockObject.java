package com.rsupport.util;

/**
 * Created by taehwan on 5/13/15.
 */
public class LockObject {

    private boolean isNotify = false;
    private boolean isLock = false;

    public void clear() {
        notifyLock();
        isNotify = false;
        isLock = false;
    }

    public synchronized void notifyLock() {
        isNotify = true;
        if (isLock) {
            try {
                isLock = false;
                notifyAll();
            } catch (Exception e) {
                //
            }
        }
    }

    public synchronized void lock(int timeOut) {
        if (!isNotify) {
            try {
                isLock = true;
                wait(timeOut);
            } catch (Exception e) {
                //
            }
        }
    }

    public synchronized void lock() {
        lock(Integer.MAX_VALUE);
    }

    public synchronized void enforceLock() {
        try {
            isLock = true;
            wait();
        } catch (Exception e) {
            //
        }
    }
}
