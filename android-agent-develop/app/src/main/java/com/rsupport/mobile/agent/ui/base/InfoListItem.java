package com.rsupport.mobile.agent.ui.base;


public class InfoListItem {

    public static final int NO_BUTTON_EVENT = 0;
    public static final int RIGHT_BUTTON = 1;
    public static final int DOWN_BUTTON = 2;
    public static final int UP_BUTTON = 3;
    public static final int TOGGLE_BUTTON_ON = 4;
    public static final int TOGGLE_BUTTON_OFF = 5;
    public static final int EDIT_BOX = 6;
    public static final int CHECK_BOX = 7;
    public static final int COPY_BUTTON = 8;
    public static final int INCLUDE_LAYOUT = 9;
    public static final int KILL_ITEM = 10;
    public static final int REMOVE_ITEM = 11;
    public static final int EDIT_PASS_BOX = 12;
    public static final int EDIT_RADIO_BOX = 13;

    /**
     * Basic Box Style
     **/
    private String mItemTitle = null;
    private String mItemContent = null;

    private int mEventID = 0;
    private int mChildID = 0;

    /**
     * on/off 모드 (on-true / off-false)
     **/
    private boolean mModeOn = false;
    /**
     * child item
     **/
    public boolean isChild = false;

    /**
     * 일반적인 경우 나타나는 오른쪽 방향 버튼
     **/
    public boolean isRightButton = false;

    /**
     * 아래로 chile 뷰를 열 수 있는 아래쪽 방향 버튼
     **/
    public boolean isDownButton = false;
    public boolean isUpButton = false;

    /**
     * 설정의 Yes/No 를 판단할 수 있는 토클버튼
     **/
    public boolean isToggleOn = false;
    public boolean isToggleOff = false;

    /**
     * EditBox 를 가진 형태
     **/
    public boolean isEditBox = false;

    /**
     * CheckBox 를 가진 형태
     **/
    public boolean isCheckImage = false;

    /**
     * 다른 레이아웃을 가진 형태
     **/
    public boolean isIncludeLayout = false;
    public int mIncludeLayoutResID = -1;

    /**
     * 선택된 항목 아이템 kill
     **/
    public boolean isKillItem = false;

    /**
     * 선택된 항목 아이템remove
     **/
    public boolean isRemoveItem = false;

    /**
     * 이벤트 여부 (버튼이 없을 경우)
     **/
    public boolean isEvent = false;

    /**
     * Copy button
     **/
    public boolean isCopyImage = false;

    /**
     * Item 하단 가로줄 존재 여부
     **/
    public boolean isDivider = true;

    /**
     * 공지사항 New Icon 여부
     **/
    public boolean isNewIcon = false;

    /**
     * 패스워드 여부
     **/
    public boolean isPassword = false;

    public boolean isRadiobutton = false;

    public InfoListItem(int eventID, String itemTitle, String itemContetn) {
        init(eventID, itemTitle, itemContetn);
    }

    /**
     * Param - evnetID, itemTitle, itemContent, type
     **/
    public InfoListItem(int eventID, String itemTitle, String itemContetn, int type) {
        switch (type) {
            case RIGHT_BUTTON:
                this.isRightButton = true;
                this.isEvent = true;
                break;
            case DOWN_BUTTON:
                this.isDownButton = true;
                this.isEvent = true;
                break;
            case UP_BUTTON:
                this.isUpButton = true;
                this.isEvent = true;
                break;
            case TOGGLE_BUTTON_ON:
                this.isToggleOn = true;
                this.isEvent = false;    // 토글버튼의 경우 버튼 자체에 클릭 이벤트를 준다
                break;
            case TOGGLE_BUTTON_OFF:
                this.isToggleOff = true;
                this.isEvent = false;
                break;
            case EDIT_BOX:
                this.isEditBox = true;
                this.isEvent = false;
                this.isPassword = false;
                break;
            case CHECK_BOX:
                this.isCheckImage = true;
                this.isEvent = true;
                break;
            case COPY_BUTTON:
                this.isCopyImage = true;
                this.isEvent = true;
                break;
            case INCLUDE_LAYOUT:
                this.isIncludeLayout = true;
                this.isEvent = false;
                break;
            case KILL_ITEM:
                this.isKillItem = true;
                this.isEvent = true;
                break;
            case NO_BUTTON_EVENT:
                this.isEvent = true;
                break;
            case REMOVE_ITEM:
                this.isRemoveItem = true;
                break;
            case EDIT_PASS_BOX:
                this.isEditBox = true;
                this.isEvent = false;
                this.isPassword = true;
                break;
            case EDIT_RADIO_BOX:
                this.isEditBox = true;
                this.isEvent = false;
                this.isPassword = false;
                this.isRadiobutton = true;
                break;

            default:
                this.isEvent = false;
                break;
        }

        init(eventID, itemTitle, itemContetn);
    }


    private void init(int eventID, String itemTitle, String itemContetn) {
        // No button. Simple ListItem.
        this.mItemTitle = itemTitle;
        this.mItemContent = itemContetn;
        this.mEventID = eventID;
    }

    public boolean getModeOn() {
        return mModeOn;
    }

    public void setModeOn(boolean modeOn) {
        this.mModeOn = modeOn;
    }

    public String getItemTitle() {
        return mItemTitle;
    }

    public void setItemTitle(String mItemTitle) {
        this.mItemTitle = mItemTitle;
    }

    public String getItemContent() {
        return mItemContent;
    }

    public void setItemContent(String mItemContent) {
        this.mItemContent = mItemContent;
    }

    public int getmEventID() {
        return mEventID;
    }

    public void setChildID(int childID) {
        this.mChildID = childID;
    }

    public int getChildID() {
        return mChildID;
    }

    public int getIncludeLayoutResID() {
        return mIncludeLayoutResID;
    }

    public void setIncludeLayoutResID(int includeLayoutResID) {
        this.mIncludeLayoutResID = includeLayoutResID;
    }
}
