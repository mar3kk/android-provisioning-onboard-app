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

package com.imgtec.creator.sniffles.presentation.helpers;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.widget.TextView;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.presentation.MainActivity;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

/**
 *
 */
public class DrawerHelper {
  private final MainActivity activity;
  private WeakReference<DrawerLayout> drawer;
  private WeakReference<ActionBarDrawerToggle> drawerToggle;
  private WeakReference<NavigationView> navigationView;

  @Inject
  public DrawerHelper(MainActivity activity) {
    this.activity = activity;
  }

  public DrawerLayout getDrawer() {
    if (drawer == null || drawer.get() == null) {
      throw new IllegalArgumentException("Set Drawer first.");
    }
    return drawer.get();
  }

  public void setDrawer(DrawerLayout drawer) {
    this.drawer = new WeakReference<>(drawer);
  }

  public void setNavigationView(NavigationView navigationView) {
    this.navigationView = new WeakReference<>(navigationView);
  }

  public ActionBarDrawerToggle getDrawerToggle() {
    return this.drawerToggle.get();
  }

  public void setDrawerToggle(ActionBarDrawerToggle drawerToggle) {
    this.drawerToggle = new WeakReference<>(drawerToggle);
  }

  public void unlockDrawer() {
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
      });
    }
  }

  public void lockDrawer() {
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
      });
    }
  }

  public void updateHeader(final String user, final String email) {
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (navigationView.get() != null) {
            ((TextView) navigationView.get().getHeaderView(0).findViewById(R.id.user)).setText(user);
          }

          if (navigationView.get() != null) {
            ((TextView)( navigationView.get()).getHeaderView(0).findViewById(R.id.email)).setText(email);
          }
        }
      });
    }
  }

  public void hideSelector() {
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Menu menu = ((NavigationView) drawer.get().findViewById(R.id.nav_view)).getMenu();
          if (menu != null) {
            for (int i = 0; i < menu.size(); menu.getItem(i).setChecked(false), i++) ;
          }
        }
      });
    }
  }

  public void setSelector(final int index) {
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Menu menu = ((NavigationView) drawer.get().findViewById(R.id.nav_view)).getMenu();
          if (menu != null) {
            menu.getItem(index).setChecked(true);
          }
        }
      });
    }
  }
}
