/*
 * <b>Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies
 *  and/or licensors. </b>
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *      and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *      conditions and the following disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *      endorse or promote products derived from this software without specific prior written
 *      permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *  WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.imgtec.creator.sniffles.data;

import javax.inject.Inject;

/**
 *
 */
public class Preferences {

  //account access keys
  private static final String AC_KEY = "ac_key";
  private static final String AC_SECRET = "ac_secret";
  private static final String DEFAULT_AC_KEY = "";
  private static final String DEFAULT_AC_SECRET = "";
  //user
  private static final String EMPTY = "";
  private static final String USERNAME = "username";
  private static final String DEFAULT_USERNAME = EMPTY;
  private static final String EMAIL = "email";
  private static final String DEFAULT_EMAIL = EMPTY;

  //OAuth
  private static final String OAUTH_REFRESH_TOKEN = "oauth_refresh_token";
  private static final String DEFAULT_OAUTH_REFRESH_TOKEN = EMPTY;
  private static final String KEEP_ME_LOGGED_IN = "keep_me_logged_in";
  private static final boolean DEFAULT_KEEP_ME_LOGGED_IN = false;

  private final SecurePreferences preferences;

  @Inject
  Preferences(SecurePreferences prefs) {
    this.preferences = prefs;
  }

  public synchronized UserData getUserData() {
    final String username = preferences.getString(USERNAME);
    final String email = preferences.getString(EMAIL);
    return new UserData(username == null ? DEFAULT_USERNAME : username,
                        email == null ? DEFAULT_EMAIL: email);
  }

  public synchronized void setUserData(final UserData userdata) {
    setUserData(userdata.getUsername(), userdata.getEmail());
  }

  public synchronized void resetUserData() {
    setUserData("", "");
  }

  private void setUserData(String username, String email) {
    preferences.put(USERNAME, username);
    preferences.put(EMAIL, email);
  }

  public synchronized void setRefreshToken(String refreshToken) {
    preferences.put(OAUTH_REFRESH_TOKEN, refreshToken);
  }

  public synchronized String getRefreshToken() {
    final String token = preferences.getString(OAUTH_REFRESH_TOKEN);
    return (token == null) ? DEFAULT_OAUTH_REFRESH_TOKEN :  token;
  }

  public synchronized void resetRefreshToken() {
    setRefreshToken("");
  }

  public synchronized boolean getKeepMeLoggedIn() {
    final String loggedIn = preferences.getString(KEEP_ME_LOGGED_IN);
    return (loggedIn == null) ? DEFAULT_KEEP_ME_LOGGED_IN : Boolean.parseBoolean(loggedIn);
  }

  public synchronized void setKeepMeLoggedIn(boolean checked) {
    preferences.put(KEEP_ME_LOGGED_IN, Boolean.toString(checked));
  }

  public synchronized void saveAccessKeys(String key, String secret) {
    preferences.put(AC_KEY, key);
    preferences.put(AC_SECRET, secret);
  }

  public String getKey() {
    final String key = preferences.getString(AC_KEY);
    return (key == null) ? DEFAULT_AC_KEY : key;
  }

  public String getSecret() {
    final String secret = preferences.getString(AC_SECRET);
    return (secret == null) ? DEFAULT_AC_SECRET : secret;
  }
}
