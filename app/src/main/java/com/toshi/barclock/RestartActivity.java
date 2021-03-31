package com.toshi.barclock;
/*****************************************************************************
*
*	BarClock:RestartActivity -- サービス再起動用
*
*	V1.0.0	2021/03/31	initial revision by	toshi
*
*****************************************************************************/

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;
import android.os.Build;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

public class RestartActivity extends AppCompatActivity {

	private static final String TAG = "⦿Restart ";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restart);

		Log.d(TAG, "onCreate");
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, TAG+"onCreate",
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
		finish();
	}
}
