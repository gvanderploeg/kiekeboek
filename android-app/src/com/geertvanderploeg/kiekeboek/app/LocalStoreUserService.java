package com.geertvanderploeg.kiekeboek.app;

import java.util.List;

import com.geertvanderploeg.kiekeboek.client.User;
import com.geertvanderploeg.kiekeboek.syncadapter.LocalContactsStore;

import android.content.Context;

public class LocalStoreUserService implements UserService {

  @Override
  public List<User> getUsers(Context context) {
    return LocalContactsStore.getAllUsers(context);
  }

  @Override
  public User getUser(int userId, Context context) {
    return LocalContactsStore.getUser(context, userId);
  }
}
