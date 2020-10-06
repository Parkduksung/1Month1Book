package com.rsupport.mobile.agent.modules.function;

import android.content.ClipboardManager;
import android.content.Context;


public class Clipboard {

    private static Clipboard clipboard;
    private ClipboardManager clipManager;


    public static Clipboard getInstance(Context context) {
        if (clipboard == null) {
            clipboard = new Clipboard(context);
        }
        return clipboard;
    }

    private Clipboard(Context context) {
        try {
            clipManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }

    public void setText(String text) {
        if (clipManager == null) return;
        clipManager.setText(text);
    }

    public String getText() {
        if (clipManager == null) return "";
        CharSequence charSequence = clipManager.getText();
        if (charSequence == null) return null;
        return charSequence.toString();
    }

    public boolean isHasText() {
        if (clipManager == null) return false;
        return clipManager.hasText();
    }

}
