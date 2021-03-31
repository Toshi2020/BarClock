package com.toshi.barclock;
/*****************************************************************************
*
*	BarClock:BootReceiver -- ブロードキャストレシーバー(BOOT_COMPLETE受信用)
*
*	V1.0.0	2021/03/31	initial revision by	toshi
*
*****************************************************************************/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "●BootReceiver ";

	@Override
	public void onReceive(final Context context, Intent intent) {

		String action = intent.getAction();

		Log.d(TAG, action);
		if (BuildConfig.DEBUG) {
			Toast.makeText(context,TAG + action, Toast.LENGTH_LONG).show();
		}

		// 設定値を読む
		SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(context);

		// 起動SWオンかつサービスが起動していない？
		if (sharedPreferences.getBoolean("fEnable", false) &&
				ClockService.IsServiceRunning() == false) {

			// サービス起動
			Intent i = new Intent(context, ClockService.class);
			// API 26 以上
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(i);
			} else {
				context.startService(i);
			}
		}
	}
}
