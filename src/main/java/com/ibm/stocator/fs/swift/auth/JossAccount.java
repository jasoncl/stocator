package com.ibm.stocator.fs.swift.auth;

import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Access;
import org.javaswift.joss.model.Account;

/**
 *
 * The account model in Joss has session that contains token.
 * When token expire, token re-created automatically.
 * In certain flows we need to get token value and use it in the direct
 * calls to the Swift API object stores.
 * In this case we need to cache the token and re-authenticate it only when 401 happens.
 *
 */
public class JossAccount {
  /*
   * Joss account object
   */
  private Account mAccount;
  /*
   * Joss configuration
   */
  private AccountConfig mAccountConfig;
  /*
   * Keystone region
   */
  private String mRegion;
  /*
   * use public or internal URL for Swift API object store
   */
  boolean mUsePublicURL;
  /*
   * Cached Access object. Will be renewed when token expire
   */
  private Access mAccess;

  /**
   * Constructor
   *
   * @param config Joss configuration
   * @param region Keystone region
   * @param usePublicURL use public or internal url
   */
  public JossAccount(AccountConfig config, String region, boolean usePublicURL) {
    mAccountConfig = config;
    mRegion = region;
    mUsePublicURL = usePublicURL;
    mAccess = null;
  }

  /**
   * Creates account model
   */
  public void createAccount() {
    mAccount = new AccountFactory(mAccountConfig).createAccount();
  }

  /**
   * Creates virtual account. Used for public containers
   */
  public void createDummyAccount() {
    mAccount = new DummyAccountFactory(mAccountConfig).createAccount();
  }

  /**
   * Authenticates and renew the token
   */
  public void authenticate() {
    if (mAccount == null) {
      createAccount();
    }
    mAccess = mAccount.authenticate();
    if (mRegion != null) {
      mAccess.setPreferredRegion(mRegion);
    }
  }

  /**
   * Return current token
   *
   * @return cached token
   */
  public String getAuthToken() {
    return mAccess.getToken();
  }

  /**
   * Get authenticated URL
   *
   * @return access URL, public or internal
   */
  public String getAccessURL() {
    if (mUsePublicURL) {
      return mAccess.getPublicURL();
    }
    return mAccess.getInternalURL();
  }

  /**
   * Get account
   *
   * @return Account
   */
  public Account getAccount() {
    if (mAccount == null) {
      createAccount();
    }
    return mAccount;
  }
}
