package com.rsupport.mobile.agent.modules.push.service;

import java.util.ArrayList;

import com.rsupport.mobile.agent.modules.push.command.IPushCommand;
import com.rsupport.util.log.RLog;

public class CommandExecutor implements Runnable {
    private ArrayList<IPushCommand> targetCommand = new ArrayList<IPushCommand>();
    private final int RETRY_COUNT = 30;
    private final int SLEEP_TIME = 3000;
    private IPushCommand command = null;
    private Thread t = null;

    @Override
    public void run() {
        try {
            while (true) {
                if (command == null) {
                    synchronized (this) {
                        if (targetCommand.size() > 0) {
                            command = targetCommand.remove(0);
                        } else {
                            t = null;
                            break;
                        }
                    }
                }

                RLog.i("retry command count(" + command.getCurrentRetryCount() + "), type(" + command.getType() + ")");
                if (command.execute() == true || command.getCurrentRetryCount() > RETRY_COUNT) {
                    command = null;
                    synchronized (targetCommand) {
                        if (targetCommand.size() == 0) {
                            t = null;
                            break;
                        }
                    }
                }

                RLog.w("retry command count(" + command.getCurrentRetryCount() + "), type(" + command.getType() + ")");
                Thread.sleep(SLEEP_TIME);
            }
        } catch (Exception e) {
            RLog.e(e);
        }
    }

    public synchronized void execute(IPushCommand command) {
        if (targetCommand != null && checkAddedCommand(command) == false) {
            targetCommand.add(command);
        }
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    /**
     * 같은 타입의 command 가 이미 실행중인지...
     *
     * @param command
     * @return
     */
    private boolean checkAddedCommand(IPushCommand command) {
        if (this.command != null && this.command.getType() == command.getType()) {
            RLog.i("already added type : " + command.getType());
            return true;
        }

        for (IPushCommand cmd : targetCommand) {
            if (cmd.getType() == command.getType()) {
                RLog.i("already added type : " + command.getType());
                return true;
            }
        }
        return false;
    }
}
