package com.rsupport.mobile.agent.modules.push.command;

abstract public class AbstractPushCommand implements IPushCommand {
    public static final int TYPE_SET_SERVER_INFO = 0;
    public static final int TYPE_REGISTER = 1;
    public static final int TYPE_UN_REGISTER = 2;

    private int tryCount = 0;

    protected abstract boolean run();

    @Override
    public boolean execute() {
        tryCount++;
        return run();
    }

    @Override
    public int getCurrentRetryCount() {
        return tryCount;
    }
}
