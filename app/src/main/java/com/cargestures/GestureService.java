package com.cargestures;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * 底部上滑=主页 / 上滑停住=多任务；左右边缘内滑=返回。
 * 触发区用 TYPE_ACCESSIBILITY_OVERLAY 悬浮窗实现（无障碍服务可直接创建，无需悬浮窗权限）。
 */
public class GestureService extends AccessibilityService {
  private WindowManager wm;
  private Handler handler = new Handler(Looper.getMainLooper());
  private int navBarHeight;

  private View bottomZone, leftZone, rightZone;

  // 底部区手势状态
  private float downY;
  private long downTime;
  private boolean moved = false;

  @Override
  protected void onServiceConnected() {
    super.onServiceConnected();
    wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    int[] size = new int[2];
    getDefaultDisplay(size);
    int w = size[0], h = size[1];
    navBarHeight = h / 10; // 底部 10% 高度区域

    addBottomZone(w, h);
    addSideZone(true, w, h);
    addSideZone(false, w, h);
  }

  private void getDefaultDisplay(int[] out) {
    android.view.Display d = wm.getDefaultDisplay();
    android.graphics.Point p = new android.graphics.Point();
    d.getRealSize(p);
    out[0] = p.x;
    out[1] = p.y;
  }

  private WindowManager.LayoutParams zoneParams(int x, int y, int w, int h) {
    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
        w, h,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT);
    lp.gravity = Gravity.TOP | Gravity.START;
    lp.x = x;
    lp.y = y;
    return lp;
  }

  private void addBottomZone(int w, int h) {
    bottomZone = new View(this);
    bottomZone.setBackgroundColor(0x00000000);
    bottomZone.setOnTouchListener((v, ev) -> {
      switch (ev.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
          downY = ev.getY();
          downTime = System.currentTimeMillis();
          moved = false;
          return true;
        case MotionEvent.ACTION_MOVE:
          if (downY - ev.getY() > 30) moved = true;
          return true;
        case MotionEvent.ACTION_UP:
          float dy = downY - ev.getY();
          long held = System.currentTimeMillis() - downTime;
          if (moved && held > 450 && dy > h * 0.18f) {
            goRecents();
          } else if (dy > h * 0.12f) {
            goHome();
          }
          return true;
      }
      return false;
    });
    int bh = h / 8;
    wm.addView(bottomZone, zoneParams(0, h - bh, w, bh));
  }

  private void addSideZone(boolean left, int w, int h) {
    View zone = new View(this);
    zone.setBackgroundColor(0x00000000);
    final float downX[] = new float[1];
    zone.setOnTouchListener((v, ev) -> {
      switch (ev.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
          downX[0] = ev.getX();
          return true;
        case MotionEvent.ACTION_UP:
          float travel = left ? ev.getX() - downX[0] : downX[0] - ev.getX();
          if (travel > h * 0.08f) goBack();
          return true;
      }
      return false;
    });
    int zw = 30;
    wm.addView(zone, zoneParams(left ? 0 : w - zw, h / 4, zw, h / 2));
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
