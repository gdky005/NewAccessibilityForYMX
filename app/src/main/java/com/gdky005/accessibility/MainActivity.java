package com.gdky005.accessibility;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private String SERVICE_NAME = WQAccessibilityService.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preInit();
        initListener();
        initData();
    }

    private void preInit() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SERVICE_NAME = getPackageName() + "/" + SERVICE_NAME;
    }

    private void initListener() {
        findViewById(R.id.fab).setOnClickListener(this);
        findViewById(R.id.as_btn).setOnClickListener(this);
        findViewById(R.id.test_sj_btn).setOnClickListener(this);
        findViewById(R.id.test_start_ymx_btn).setOnClickListener(this);
        findViewById(R.id.test_sj_open_btn).setOnClickListener(this);
        findViewById(R.id.test_sj_open_btn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(MainActivity.this, "触摸事件！！！", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void initData() {
        if (!checkStealFeature1(SERVICE_NAME)) {
            openAS();
        } else {
            Toast.makeText(this, "辅助功能已经开启", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAS() {
        Toast.makeText(this, "请优先开启 " + getString(R.string.accessibilityTab) + " 的权限", Toast.LENGTH_SHORT).show();
        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessibilitySettingsActivity");

        Intent intent = new Intent();
        intent.setComponent(componentName);
        startActivity(intent);
    }

    /**
     * 参考：https://blog.csdn.net/dd864140130/article/details/51794318
     */
    private boolean checkStealFeature1(String service) {
        try {
            int ok = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
            TextUtils.SimpleStringSplitter ms = new TextUtils.SimpleStringSplitter(':');
            if (ok == 1) {
                String settingValue = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (settingValue != null) {
                    ms.setString(settingValue);
                    while (ms.hasNext()) {
                        String accessibilityService = ms.next();
                        if (accessibilityService.equalsIgnoreCase(service)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "checkStealFeature1: ", e);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.as_btn:
                openAS();
                break;
            case R.id.test_sj_btn:
                Toast.makeText(MainActivity.this, "测试按钮！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab:
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case R.id.test_sj_open_btn:
                Toast.makeText(this, "当时辅助功能开始的状态是：" + checkStealFeature1(SERVICE_NAME), Toast.LENGTH_SHORT).show();
                break;
            case R.id.test_start_ymx_btn:
                ComponentName componentName = new ComponentName("cn.amazon.mShop.android", "com.amazon.mShop.home.HomeActivity");

                Intent intent = new Intent();
                intent.setComponent(componentName);
                startActivity(intent);
                break;
        }
    }

}
