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

package com.imgtec.creator.sniffles.presentation;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.data.Preferences;
import com.imgtec.creator.sniffles.data.UserData;
import com.imgtec.creator.sniffles.presentation.fragments.AboutFragment;
import com.imgtec.creator.sniffles.presentation.fragments.ClientsFragment;
import com.imgtec.creator.sniffles.presentation.fragments.LoginFragment;
import com.imgtec.creator.sniffles.presentation.fragments.OnboardingFragment;
import com.imgtec.creator.sniffles.presentation.fragments.ProvisioningFragment;
import com.imgtec.creator.sniffles.presentation.helpers.DrawerHelper;
import com.imgtec.creator.sniffles.presentation.helpers.FragmentHelper;
import com.imgtec.creator.sniffles.presentation.helpers.PermissionHelper;
import com.imgtec.creator.sniffles.presentation.helpers.ToolbarHelper;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 *
 */
public class MainActivity extends BaseActivity implements HasComponent<ActivityComponent>,
    NavigationView.OnNavigationItemSelectedListener {

  private ActivityComponent component;

  @BindView(R.id.app_bar) AppBarLayout appBar;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.drawer_layout) DrawerLayout drawer;
  @BindView(R.id.nav_view) NavigationView navigationView;

  Fragment currentFragment;

  @Inject Logger logger;
  @Inject DrawerHelper drawerHelper;
  @Inject ToolbarHelper toolbarHelper;
  @Inject Preferences preferences;
  @Inject PermissionHelper permissionHelper;
  @Inject @Named("Main") Handler handler;

  ActionBarDrawerToggle toggle;
  Unbinder unbinder;
  boolean doubleBack = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    unbinder = ButterKnife.bind(this);

    setSupportActionBar(toolbar);
    toolbarHelper.setToolbar(toolbar);
    setupNavigationDrawer();
    updateDrawerHelper();
    if (savedInstanceState == null) {
      showFragmentWithClearBackstack(LoginFragment.newInstance());
    }
  }

  private void updateDrawerHelper() {
    UserData data = preferences.getUserData();
    if (data != null) {
      drawerHelper.updateHeader(data.getUsername(), data.getEmail());
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (intent != null) {
      handleResponseIntent(intent);
    }
  }

  private void handleResponseIntent(Intent intent) {
    Uri uri = intent.getData();
    if (uri != null) {
      String token = uri.toString().split("#")[1].split("=")[1];
      if (token != null) {
        if (currentFragment instanceof LoginFragment) {
          ((LoginFragment) currentFragment).handleRedirection(token);
        }
      }
    }
    else {
      logger.error("URI missing in authorization intent!");
    }
  }

  private void setupNavigationDrawer() {
    toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
        R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
    };
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);
    navigationView.getMenu().getItem(0).setChecked(true);
    drawerHelper.setDrawer(drawer);
    drawerHelper.setNavigationView(navigationView);
    drawerHelper.setDrawerToggle(toggle);
  }

  @Override
  protected void setComponent() {
    component = ActivityComponent.Initializer.init(this);
    component.inject(this);
  }

  @Override
  public ActivityComponent getComponent() {
    return component;
  }

  @Override
  public void onAttachFragment(Fragment fragment) {
    super.onAttachFragment(fragment);
    currentFragment = fragment;
  }

  @Override
  protected void onDestroy() {
    drawer.removeDrawerListener(toggle);
    unbinder.unbind();
    toolbarHelper = null;
    super.onDestroy();
  }


  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      final FragmentManager fm = getSupportFragmentManager();
      if (fm.getBackStackEntryCount() > 0) {
        fm.popBackStack();
      } else if (!doubleBack) {
        doubleBack = true;
        Toast.makeText(this,"Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

        handler.postDelayed(new Runnable() {

          @Override
          public void run() {
            doubleBack = false;
          }
        }, 2000);
      } else {
        super.onBackPressed();
      }
    }
    currentFragment = null;
  }

  private void showFragmentWithClearBackstack(Fragment f) {
    currentFragment = f;
    FragmentHelper.replaceFragmentAndClearBackStack(getSupportFragmentManager(), currentFragment);
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();
    item.setChecked(true);

    switch (id) {
      case R.id.clients: {
        showFragmentWithClearBackstack(ClientsFragment.newInstance());
        break;
      }
      case R.id.onboarding: {
        showFragmentWithClearBackstack(OnboardingFragment.newInstance());
        break;
      }
      case R.id.provisioning: {
        showFragmentWithClearBackstack(ProvisioningFragment.newInstance());
        break;
      }
      case R.id.about: {
        showFragmentWithClearBackstack(AboutFragment.newInstance());
        break;
      }
      case R.id.logout: {
        preferences.resetRefreshToken();
        preferences.resetUserData();
        showFragmentWithClearBackstack(LoginFragment.newInstance());
      }
      default:
        break;
    }
    drawer.closeDrawer(GravityCompat.START);
    return false;
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    toggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    toggle.onConfigurationChanged(newConfig);
  }
}
