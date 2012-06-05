/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.geertvanderploeg.kiekeboek.syncadapter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.geertvanderploeg.kiekeboek.Constants;
import com.geertvanderploeg.kiekeboek.app.Notifications;
import com.geertvanderploeg.kiekeboek.client.User;
import com.geertvanderploeg.kiekeboek.platform.ContactManager;

import org.apache.http.ParseException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    private final AccountManager mAccountManager;
    private final Context context;

    private Date mLastUpdated;

    private UserConnector connector = new IntranetUserConnector();
//    private UserConnector connector = new JSONResourceUserConnector();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        List<User> users;
        String authtoken = null;
        try {
            // use the account manager to request the credentials
            try {
                authtoken = mAccountManager.blockingGetAuthToken(account,
                        Constants.AUTHTOKEN_TYPE, true /* notifyAuthFailure */);
            } catch (OperationCanceledException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (AuthenticatorException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            // fetch updates
            users = connector.fetchUpdates(context, account, authtoken, mLastUpdated);


            // update the last synced date.
            mLastUpdated = new Date();

            // Update local store
            LocalContactsStore.syncContacts(context, users);

            // update platform contacts.
            Log.d(TAG, "Calling contactManager's sync contacts");
            ContactManager.syncContacts(context, account.name, users);


        } catch (final ParseException e) {
          syncResult.stats.numParseExceptions++;
          Log.e(TAG, "ParseException", e);
          Notifications.addNotification(context, "ParseException", e.getMessage());
        }
    }
}
