package com.cargestures;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * 车机全面屏手势：底部上滑=主页，上滑停住=多任务，左右边缘内滑=返回。
 * 触发区用 TYPE_ACCESSIBILITY_OVERLAY + gravity 定位（旋转无关）。
 */
public class GestureService extends AccessibilityService {
  private static final String TAG = "CarGesture";
  private WindowManager wm;
  private View bottomZone, leftZone, rightZone;
  private int screenW, screenH;

  @Override
  protected void onServiceConnected() {
    super.onServiceConnected();
    Log.i(TAG, "service connected");
    wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    android.graphics.Point p = new android.graphics.Point();
    wm.getDefaultDisplay().getRealSize(p);
    screenW = p.x;
    screenH = p.y;
    Log.i(TAG, "screen=" + screenW + "x" + screenH);
    try {
      addBottomZone();
      addSideZone(true);
      addSideZone(false);
      Log.i(TAG, "zones added");
    } catch (Exception e) {
      Log.e(TAG, "addZone error", e);
    }
  }

  private WindowManager.LayoutParams lp(int w, int h, int gravity) {
    WindowManager.LayoutParams l = new WindowManager.LayoutParams(
        w, h,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT);
    l.gravity = gravity;
    return l;
  }

  private void addBottomZone() {
    bottomZone = new View(this);
    bottomZone.setBackgroundColor(0x00FFFFFF);
    bottomZone.setOnTouchListener(new View.OnTouchListener() {
      private float downY;
      private long downTime;
      private boolean moved;

      @Override
      public boolean onTouch(View v, MotionEvent ev) {
        switch (ev.getActionMasked()) {
          case MotionEvent.ACTION_DOWN:
            downY = ev.getY();
            downTime = System.currentTimeMillis();
            moved = false;
            return true;
          case MotionEvent.ACTION_MOVE:
            if (downY - ev.getY() > 25) moved = true;
            return true;
          case MotionEvent.ACTION_UP:
            float dy = downY - ev.getY();
            long held = System.currentTimeMillis() - downTime;
            if (moved && held > 400 && dy > screenH * 0.15f) {
              goRecents();
            } else if (dy > screenH * 0.10f) {
              goHome();
            }
            return true;
        }
        return false;
      }
    });
    wm.addView(bottomZone, lp(screenW, screenH / 6, Gravity.BOTTOM));
  }

  private void addSideZone(final boolean left) {
    View zone = new View(this);
    zone.setBackgroundColor(0x00FFFFFF);
    final float[] downX = new float[1];
    zone.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent ev) {
        switch (ev.getActionMasked()) {
          case MotionEvent.ACTION_DOWN:
            downX[0] = ev.getX();
            return true;
          case MotionEvent.ACTION_UP:
            float travel = left ? ev.getX() - downX[0] : downX[0] - ev.getX();
            if (travel > screenH * 0.06f) goBack();
            return true;
        }
        return false;
      }
    });
    wm.addView(zone, lp(40, screenH / 2, left ? Gravity.LEFT | Gravity.CENTER_VERTICAL
        : Gravity.RIGHT | Gravity.CENTER_VERTICAL));
  }

  private void removeZones() {
    try {
      if (bottomZone != null) wm.removeView(bottomZone);
      if (leftZone != null) wm.removeView(leftZone);
      if (rightZone != null) wm.removeView(rightZone);
    } catch (Exception ignored) {
    }
    bottomZone = leftZone = rightZone = null;
  }

  private void goHome() {
    performGlobalAction(GLOBAL_ACTION_HOME);
  }

  private void goBack() {
    performGlobalAction(GLOBAL_ACTION_BACK);
  }

  private void goRecents() {
    performGlobalAction(GLOBAL_ACTION_RECENTS);
  }

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
  }

  @Override
  public void onInterrupt() {
  }

  @Override
  public boolean onUnbind(Intent intent) {
    removeZones();
    return super.onUnbind(intent);
  }
}
