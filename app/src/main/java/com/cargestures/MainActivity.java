package com.cargestures;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    int pad = (int) (getResources().getDisplayMetrics().density * 24);
    layout.setPadding(pad, pad * 2, pad, pad);

    TextView title = new TextView(this);
    title.setText("车机全面屏手势");
    title.setTextSize(24);
    layout.addView(title);

    TextView desc = new TextView(this);
    desc.setText("\n开启无障碍服务后生效：\n\n· 底部上滑 → 返回主页\n· 底部上滑停住 → 最近任务\n· 左右边缘向内滑 → 返回\n\n(先隐藏导航栏再开启手势)");
    desc.setTextSize(16);
    layout.addView(desc);

    Button open = new Button(this);
    open.setText("1. 打开无障碍设置");
    open.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
    layout.addView(open);

    Button hideNav = new Button(this);
    hideNav.setText("2. 隐藏导航栏");
    hideNav.setOnClickListener(v -> {
      try {
        Process p = Runtime.getRuntime().exec(new String[]{"settings", "put", "global", "policy_control", "immersive.navigation=*"});
        Toast.makeText(this, "需要 adb 授权，若无效请用 adb 执行", Toast.LENGTH_LONG).show();
      } catch (Exception e) {
        Toast.makeText(this, "请用 adb: settings put global policy_control 'immersive.navigation=*'", Toast.LENGTH_LONG).show();
      }
    });
    layout.addView(hideNav);

    Button showNav = new Button(this);
    showNav.setText("恢复导航栏");
    showNav.setOnClickListener(v -> {
      try {
        Process p = Runtime.getRuntime().exec(new String[]{"settings", "put", "global", "policy_control", "null"});
        Toast.makeText(this, "已请求恢复", Toast.LENGTH_SHORT).show();
      } catch (Exception e) {
        Toast.makeText(this, "请用 adb: settings put global policy_control null", Toast.LENGTH_LONG).show();
      }
    });
    layout.addView(showNav);

    setContentView(layout);
  }
}
