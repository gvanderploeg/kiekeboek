package com.geertvanderploeg.kiekeboek.syncadapter;

import java.util.Date;
import java.util.List;

import android.accounts.Account;
import android.content.Context;

import com.geertvanderploeg.kiekeboek.client.User;

public interface UserConnector {

    /**
     * 
     * @param context
     * @param account
     * @param authtoken
     * @param mLastUpdated
     * @return
     */
    List<User> fetchUpdates(Context context, Account account, String authtoken, Date mLastUpdated);

}
