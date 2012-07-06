package com.geertvanderploeg.kiekeboek.platform;

import java.util.ArrayList;

import com.geertvanderploeg.kiekeboek.KiekeboekContentProvider;
import com.geertvanderploeg.kiekeboek.client.User;

import android.test.ProviderTestCase2;

public class ContactManagerTest extends ProviderTestCase2<Contacts> {


  public ContactManagerTest() {
    super(KiekeboekContentProvider.class, "foo");
  }

  public void testSyncContacts() {
    ContactManager.syncContacts(getMockContext(), "foo", new ArrayList<User>());
  }
}
