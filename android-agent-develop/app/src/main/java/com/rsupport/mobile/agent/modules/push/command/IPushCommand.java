package com.rsupport.mobile.agent.modules.push.command;

public interface IPushCommand {
    public int getType();

    public int getCurrentRetryCount();

    public boolean execute();
}
