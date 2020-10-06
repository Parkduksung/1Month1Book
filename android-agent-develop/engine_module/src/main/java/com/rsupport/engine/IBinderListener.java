package com.rsupport.engine;

public interface IBinderListener {

    public void onBindService(boolean ret);

    public void onUnBindService(boolean ret);

    public void onServiceConnected();

    public void onServiceDisconnected();

}
