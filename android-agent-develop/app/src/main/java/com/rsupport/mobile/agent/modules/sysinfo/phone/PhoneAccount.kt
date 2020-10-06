package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

class PhoneAccount(private val context: Context) {

    fun getAccount(): Array<Account> {
        return AccountManager.get(context).accounts
    }
}