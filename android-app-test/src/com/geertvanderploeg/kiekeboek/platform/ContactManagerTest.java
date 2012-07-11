package com.geertvanderploeg.kiekeboek.platform;

import java.util.ArrayList;
import java.util.Date;

import com.geertvanderploeg.kiekeboek.KiekeboekContentProvider;
import com.geertvanderploeg.kiekeboek.client.User;

import android.test.ProviderTestCase2;

public class ContactManagerTest extends ProviderTestCase2<KiekeboekContentProvider> {


  public ContactManagerTest() {
    super(KiekeboekContentProvider.class, KiekeboekContentProvider.class.getName());
  }

  public void testSyncContacts() {
    final ArrayList<User> users = new ArrayList<User>();
    users.add(buildUser(1));
    users.add(buildUser(2));
    users.add(buildUser(3));
    ContactManager.syncContacts(getMockContext(), "foo", users);
  }

  private User buildUser(int seed) {
    return new User("name" + seed, "firstname"+seed, "middlename"+seed, "lastername"+seed, "street", "postcode",
        "city", "0612123123", "0725123123", "0235123123", "email@email.com", new Date(), "W3", "3a", false, seed);
  }
}
