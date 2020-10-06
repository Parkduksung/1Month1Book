package com.rsupport.mobile.agent.api.model;

public class GroupInfo {

    public String key = "";
    public String grpid;
    public String pgrpid;
    public String grpname;
    public String grpCount;
    public boolean isSelect = false;

    public String getGroupName() {
        return grpname;
    }

    public void setGroupName(String grpname) {
        this.grpname = grpname;
    }
}
