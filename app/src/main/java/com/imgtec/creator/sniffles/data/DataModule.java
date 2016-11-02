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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.imgtec.creator.sniffles.app.App;
import com.imgtec.di.PerApp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class DataModule {

  static final String PREFS = "data";

  @Provides @PerApp
  SharedPreferences provideSharedPreferences(App application) {
    return application.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
  }

  @Provides @PerApp @Named("SecureKey")
  String provideSecureKey(App app) {
    return Settings.Secure.getString(app.getContentResolver(), Settings.Secure.ANDROID_ID);
  }

  @Provides @PerApp
  SecurePreferences provideSecurePreferences(final App app, final SharedPreferences prefs,
                                             @Named("SecureKey") String secureKey) {
    return new SecurePreferences(app, prefs, secureKey, false);
  }

  @Provides @PerApp
  Preferences providesPreferences(@NonNull final SecurePreferences prefs) {
    return new Preferences(prefs);
  }

  @Provides @PerApp
  ScheduledExecutorService provideScheduleExecutorService() {
    return Executors.newScheduledThreadPool(4);
  }

  @Provides @PerApp
  NsdService provideNsdService(Context appContext, ScheduledExecutorService executorService,
                               @Named("Main") Handler mainHandler) {
    return new NsdServiceImpl(appContext, executorService, mainHandler);
  }
}
