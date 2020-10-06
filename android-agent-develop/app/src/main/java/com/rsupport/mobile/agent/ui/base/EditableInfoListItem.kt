package com.rsupport.mobile.agent.ui.base

import android.text.Editable
import android.text.TextWatcher

class EditableInfoListItem : InfoListItem, TextWatcher {
    var textChangedCallback: ((message: String) -> Unit?)? = null

    constructor(eventID: Int, itemTitle: String?, itemContetn: String?, type: Int) : super(eventID, itemTitle, itemContetn, type)
    constructor(eventID: Int, itemTitle: String?, itemContetn: String?) : super(eventID, itemTitle, itemContetn)

    override fun afterTextChanged(s: Editable?) {
        s?.let {
            textChangedCallback?.invoke(it.toString())
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}