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
    desc.setText("\n开启无障碍服务后生效：\n\n· 底部上滑 → 返回主页\n· 底部上滑停住 → 最近任务\n· 左右边缘向内滑 → 返回\n\n当前由 adb 直接启用，无需手动设置。\n若手势失效，检查无障碍列表里\n「车机手势」是否已开启。");
    desc.setTextSize(16);
    layout.addView(desc);

    Button open = new Button(this);
    open.setText("打开系统无障碍设置");
    open.setOnClickListener(v -> {
      try {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
      } catch (Exception e) {
        Toast.makeText(this, "系统无此设置页，请用 adb 启用", Toast.LENGTH_LONG).show();
      }
    });
    layout.addView(open);

    setContentView(layout);
  }
}