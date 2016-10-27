package com.imgtec.creator.sniffles.presentation.helpers;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class UiHelper {

  private UiHelper() {

  }

  public static void hideSoftKeyboard(FragmentActivity activity, View view) {

    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

  }
}
