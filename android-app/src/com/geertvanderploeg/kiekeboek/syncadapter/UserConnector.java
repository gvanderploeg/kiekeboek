package com.geertvanderploeg.kiekeboek.syncadapter;

import java.util.Date;
import java.util.List;

import com.geertvanderploeg.kiekeboek.client.User;

import android.accounts.Account;
import android.content.Context;

public interface UserConnector {

  /**
   * @param context
   * @param account
   * @param authtoken
   * @param lastUpdated
   * @return
   */
  List<User> fetchUpdates(Context context, Account account, String authtoken, Date lastUpdated);

  Date getLastSyncDate(Context context);
  void saveLastSyncDate(Context context, Date date);
}
