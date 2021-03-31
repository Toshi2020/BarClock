package com.toshi.barclock;
/*****************************************************************************
*
*	BarClock:MyAccessibilityService -- ユーザー補助サービス(起動検出用)
*
*	V1.0.0	2021/03/31	initial revision by	toshi
*
*****************************************************************************/

import android.content.Intent;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import android.util.Log;
import android.os.Build;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

public class MyAccessibilityService extends AccessibilityService {

	private static final String TAG = "💛MyAccessibility ";

	// サービス開始時
	@Override
	public void onServiceConnected() {
		Log.d(TAG, "onServiceConnected");
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, TAG+"onServiceConnected",
					Toast.LENGTH_LONG).show();
		}

		// 設定値を読む
		SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(this);

		// 起動SWオンかつサービスが起動していない？
		if (sharedPreferences.getBoolean("fEnable", false) &&
				ClockService.IsServiceRunning() == false) {

			// サービス起動
			Intent i = new Intent(this, ClockService.class);
			// API 26 以上
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				startForegroundService(i);
			} else {
				startService(i);
			}
		}
	}

	// システムで起こったイベントを受け取る
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Log.d(TAG, "onAccessibilityEvent");
	}

	// サービスが中断されたときの処理
	@Override
	public void onInterrupt() {
		Log.d(TAG, "onInterrupt");
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, TAG+"onInterrupt",Toast.LENGTH_LONG).show();
		}
	}

	// サービスが切断されたときの処理
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, TAG+"onUnbind",Toast.LENGTH_LONG).show();
		}
		return false;
	}
}
