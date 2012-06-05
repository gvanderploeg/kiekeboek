package com.geertvanderploeg.kiekeboek.app;

import java.util.List;

import com.geertvanderploeg.kiekeboek.client.User;

import android.content.Context;

public interface UserService {
  List<User> getUsers(Context context);

  User getUser(int userId, Context context);
}
