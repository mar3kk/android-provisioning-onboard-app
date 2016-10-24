package com.imgtec.creator.sniffles.presentation.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imgtec.creator.sniffles.R;
import com.imgtec.creator.sniffles.presentation.ActivityComponent;
import com.imgtec.creator.sniffles.presentation.helpers.ToolbarHelper;
import com.imgtec.creator.sniffles.presentation.views.ProgressButton;
import com.imgtec.di.HasComponent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;


public class Ci40Fragment extends BaseFragment {

  private static final String ARG_USER_NAME = "user_name";
  private static final String ARG_PASSWORD = "password";

  private String userName;
  private String password;

  @BindView(R.id.provisioning) AppCompatButton provision;
  @BindView(R.id.unprovision) AppCompatButton unprovision;

  @Inject ToolbarHelper toolbarHelper;

  public Ci40Fragment() {
    // Required empty public constructor
  }

  public static Ci40Fragment newInstance(String userName, String password) {
    Ci40Fragment fragment = new Ci40Fragment();
    Bundle args = new Bundle();
    args.putString(ARG_USER_NAME, userName);
    args.putString(ARG_PASSWORD, password);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      userName = getArguments().getString(ARG_USER_NAME);
      password = getArguments().getString(ARG_PASSWORD);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_ci40, container, false);
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  @Override
  protected void setupToolbar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    actionBar.show();
    actionBar.setTitle(R.string.ci_40);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);
    actionBar.setShowHideAnimationEnabled(true);
    setHasOptionsMenu(true);
  }

  @Override
  public void setupDrawer() {
    drawerHelper.getDrawerToggle().setDrawerIndicatorEnabled(false);
    drawerHelper.getDrawerToggle().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
    drawerHelper.getDrawerToggle().setToolbarNavigationClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getFragmentManager().popBackStack();
        drawerHelper.getDrawerToggle().syncState();
      }
    });
    drawerHelper.lockDrawer();
  }

  @OnClick(R.id.provisioning)
  void onProvisioning() {
    provision.setEnabled(false);
    toolbarHelper.showProgress();
  }

  @OnClick(R.id.unprovision)
  void onUnprovision() {
    provision.setEnabled(true);
    toolbarHelper.hideProgress();
  }
}
