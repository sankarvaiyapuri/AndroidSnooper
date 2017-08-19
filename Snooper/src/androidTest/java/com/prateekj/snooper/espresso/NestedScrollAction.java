package com.prateekj.snooper.espresso;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import org.hamcrest.Matcher;

import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.allOf;

public class NestedScrollAction implements ViewAction {


  @Override
  public Matcher<View> getConstraints() {
    return allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE), isDescendantOfA(anyOf(
      isAssignableFrom(ScrollView.class), isAssignableFrom(HorizontalScrollView.class), isAssignableFrom(NestedScrollView.class))));
  }

  @Override
  public String getDescription() {
    return "scroll to";
  }

  @Override
  public void perform(UiController uiController, View view) {
    scrollTo().perform(uiController, view);
  }


}
