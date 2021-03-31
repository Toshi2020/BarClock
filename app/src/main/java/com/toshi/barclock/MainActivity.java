package com.toshi.barclock;
/*****************************************************************************
*
*	BarClock:MainActivity -- ステータスバーに日時を表示
*
*	V1.0.0	2021/03/31	initial revision by	toshi
*
*****************************************************************************/

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Button;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.view.View;

//****************************************************************************
// メインクラス
//
public class MainActivity extends AppCompatActivity
					implements View.OnClickListener,
					CompoundButton.OnCheckedChangeListener {

	private static final String TAG = "★MainActivity";
	public static int OVERLAY_PERMISSION_REQ_CODE = 1000;
	private Switch mSwEnable;	// 起動SW
	private boolean fEnable;	// 起動SWの状態
	private Button mBtnSystemUI, mBtnAccecibility, mBtnBattery;
	private Button mBtnSetting, mBtnNotification;
	private static final int REQUESTCODE_SETTING = 1;
	private int TextColor = 0xffffff, BackColor = 0x000088;
	private boolean fRestart;	// 再起動要求フラグ

	/*------------------------------------------------------------------------
	インスタンス作成時
	------------------------------------------------------------------------*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		Log.d(TAG,"onCreate");

		// API 23 以上であれば
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

			// オーバーレイ描画のPermission chekを行う
			checkPermission();

		} else {
			// 通常のonCreate処理
			OnCreate();
		}
	}
	/*------------------------------------------------------------------------
	通常のonCreate処理
	------------------------------------------------------------------------*/
	private void OnCreate() {
		// プリファレンスから設定値を読み込む
		SharedPreferences pref = PreferenceManager.
										getDefaultSharedPreferences(this);
		// 起動SWの状態
		fEnable = pref.getBoolean("fEnable", false);
		// 表示色
		TextColor = pref.getInt("TextColor", TextColor);
		BackColor = pref.getInt("BackColor", BackColor);

		// SWのリソースを得る
		mSwEnable = findViewById(R.id.switch1);
		// SWのリスナの設定
		mSwEnable.setOnCheckedChangeListener(this);
		mSwEnable.setChecked(fEnable);	// SWの初期状態をセット

		// ボタンのリソースを得る
		mBtnSystemUI = findViewById(R.id.button_systemui);
		mBtnAccecibility = findViewById(R.id.button_ccecibility);
		mBtnBattery = findViewById(R.id.button_battery);
		mBtnSetting = findViewById(R.id.button_setting);
		mBtnNotification = findViewById(R.id.button_notification);
		// ボタンのリスナの設定
		mBtnSystemUI.setOnClickListener(this);
		mBtnAccecibility.setOnClickListener(this);
		mBtnBattery.setOnClickListener(this);
		mBtnSetting.setOnClickListener(this);
		mBtnNotification.setOnClickListener(this);

		// API 23未満なら
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			mBtnSystemUI.setEnabled(false);	// ボタンディセイブル
			mBtnBattery.setEnabled(false);
		}
	}
	/*------------------------------------------------------------------------
	アプリ開始時の処理
	------------------------------------------------------------------------*/
	@Override
	protected void onStart()
	{
		super.onStart();

		// 設定SWの状態によってサービス開始または停止
		StartStopService();
	}
	/*------------------------------------------------------------------------
	アプリ終了時の処理
	------------------------------------------------------------------------*/
	@Override
	protected void onDestroy()
	{
		if (fRestart) {	// 再起動要求あり？
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("EXTRA_DATA", 9999);
			startActivity(intent);
		}
		super.onDestroy();
	}
	/*------------------------------------------------------------------------
	オーバーレイが許可されていれば通常のOnCreate処理を行う
	許可されていなければ初回ならパーミッションチェック要求、再起動なら終了
	------------------------------------------------------------------------*/
	@TargetApi(Build.VERSION_CODES.M)
	public void checkPermission() {
		// オーバーレイが許可されていれば
		if (Settings.canDrawOverlays(this)) {
			OnCreate();		// 通常のonCreate処理
		} else {
			Intent intent = getIntent();
			// オーバレイ許可なしで再起動されたのか？
			if (intent.getIntExtra("EXTRA_DATA", 0) == 9999) {
				ToastShow(getString(R.string.permission_restart));
				finish();	// ここで終了
				return;
			}
			// セッティング画面を出す
			intent = 
				new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
						Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
		}
	}
	/*------------------------------------------------------------------------
	パーミッションチェック＆アクティビティ戻り
	------------------------------------------------------------------------*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
						Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// API 23 以上であれば
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// パーミッション許可画面から戻った時
			if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
				// オーバーレイが許可されていれば
				if (Settings.canDrawOverlays(this)) {
					OnCreate();		// 通常のonCreate処理
				} else {
					// Oreoだと許可したのにfalseになってしまうので
					// 再起動した後でもう一度判定する
					fRestart = true;	// 再起動要求
					finish();			// onDestroyで再起動
				}
			}
		}
		// 設定画面からOKで戻った時
		if (requestCode == REQUESTCODE_SETTING && resultCode == RESULT_OK) {
			TextColor = data.getIntExtra("TextColor", TextColor);
			BackColor = data.getIntExtra("BackColor", BackColor);

			// プリファレンスにSWの状態を書き込む
			SharedPreferences pref = PreferenceManager.
								getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt("TextColor", TextColor);
			editor.putInt("BackColor", BackColor);
			editor.apply();

			ClockService.SetColor(TextColor, BackColor);
		}
	}
	/*------------------------------------------------------------------------
	SW状態変化リスナ
	------------------------------------------------------------------------*/
	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {

		// 開始する？
		if (v.equals(mSwEnable)) {
			fEnable = mSwEnable.isChecked();
		}
		Log.d(TAG,"SW=" + fEnable);
		StartStopService();
		// プリファレンスに設定値を書き込む
		PrefWriteBool("fEnable", fEnable);
	}
	/*------------------------------------------------------------------------
	ボタンクリックリスナ
	------------------------------------------------------------------------*/
	@Override
	public void onClick(View v) {

		// ボタン1が押された
		if (v.equals(mBtnSystemUI)) {
			// システムUI調整ツール画面を開く
			OpenSystemUISetting();

		// ボタン2が押された
		} else if (v.equals(mBtnAccecibility)) {
			// ユーザー補助サービスの設定画面を開く
			OpenAccecibilitySetting();

		// ボタン3が押された
		} else if (v.equals(mBtnBattery)) {
			// バッテリー最適化の画面を開く
			OpenBatterySetting();

		// ボタン4が押された
		} else if (v.equals(mBtnSetting)) {
			// 表示設定アクティビティを起動
			Intent intent = new Intent(this, SettingActivity.class);
			intent.putExtra("TextColor", TextColor);
			intent.putExtra("BackColor", BackColor);
			startActivityForResult(intent, REQUESTCODE_SETTING);

		// ボタン5が押された
		} else if (v.equals(mBtnNotification)) {
			// 通知設定画面を開く
			OpenNotificationSetting();
		}
	}
	/*------------------------------------------------------------------------
	システムUI調整ツール画面を開く
	------------------------------------------------------------------------*/
	private void OpenSystemUISetting() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setClassName("com.android.systemui",
							"com.android.systemui.DemoMode");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
							Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		try {
			startActivity(intent);
		} catch (Exception e) {
			ToastShow(getString(R.string.mes_no_open));
		}
	}
	/*------------------------------------------------------------------------
	ユーザー補助サービスの設定画面を開く
	------------------------------------------------------------------------*/
	private void OpenAccecibilitySetting() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setClassName("com.android.settings",
		  "com.android.settings.Settings$AccessibilitySettingsActivity");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
							Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		try {
			startActivity(intent);
		} catch (Exception e) {
			ToastShow(getString(R.string.mes_no_open));
		}
	}
	/*------------------------------------------------------------------------
	バッテリー最適化の画面を開く
	------------------------------------------------------------------------*/
	private void OpenBatterySetting() {
		Intent intent;
		// API 23 以上であれば
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			try {
				PowerManager powerManager =
					getSystemService(PowerManager.class);
				// 現在最適化されているなら
				if (!powerManager.
					isIgnoringBatteryOptimizations(getPackageName())) {
					// バッテリー最適化を外すメニュー
					intent = new Intent(
						Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
					intent.setData(Uri.parse("package:" + getPackageName()));
				} else {
					// バッテリー最適化メニュー
					intent = new Intent(
						Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
								Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				startActivity(intent);
			} catch (Exception e) {
				ToastShow(getString(R.string.mes_no_open));
			}
		}
	}
	/*------------------------------------------------------------------------
	通知設定画面を開く
	------------------------------------------------------------------------*/
	private void OpenNotificationSetting() {
		Intent intent;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			intent = new Intent(
					Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
					Uri.parse("package:" + BuildConfig.APPLICATION_ID));
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
							Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		} else {
			intent = new Intent(
						Settings.ACTION_APP_NOTIFICATION_SETTINGS);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

			// for Android 5-7
			intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
			intent.putExtra("app_uid", this.getApplicationInfo().uid);

			// for Android O
			intent.putExtra("android.provider.extra.APP_PACKAGE",
						BuildConfig.APPLICATION_ID);
		}
		try {
			startActivity(intent);
		} catch (Exception e) {
			ToastShow(getString(R.string.mes_no_open));
		}
	}
	/*------------------------------------------------------------------------
	フラグの状態によりサービスを開始/停止
	------------------------------------------------------------------------*/
	public void StartStopService() {
		if (fEnable) {
			if (ClockService.IsServiceRunning() == false) {
				StartService();	// サービスを開始
			}
		} else 	{
			StopService();	// サービスを停止
		}
	}
	/*------------------------------------------------------------------------
	サービスを開始
	------------------------------------------------------------------------*/
	private void StartService() {
		Intent intent = 
			new Intent(getApplication(), ClockService.class);
		// Serviceの開始
		// API 26 以上
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(intent);
		} else {
			startService(intent);
		}

		Log.d(TAG,"Start Service");
	}
	/*------------------------------------------------------------------------
	サービスを停止
	------------------------------------------------------------------------*/
	private void StopService() {
		Intent intent = 
			new Intent(getApplication(), ClockService.class);
		// Serviceの停止
		stopService(intent);
		Log.d(TAG,"Stop Service");
	}
//****************************************************************************
// 設定
//
	/*------------------------------------------------------------------------
	プリファレンスにbooleanを書き込む
		引数
			String key;			キー文字列
			boolean isChecked;	false / true
	------------------------------------------------------------------------*/
	private void PrefWriteBool(String key, boolean isChecked) {
		// プリファレンスにSWの状態を書き込む
		SharedPreferences pref = PreferenceManager.
										getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, isChecked);
		editor.apply();
	}
	/*------------------------------------------------------------------------
	プリファレンスにintを書き込む
		引数
			String key;		キー文字列
			int data;		データ
	------------------------------------------------------------------------*/
	private void PrefWriteInt(String key, int data) {
		// プリファレンスにSWの状態を書き込む
		SharedPreferences pref = PreferenceManager.
										getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(key, data);
		editor.apply();
	}
//****************************************************************************
// 画面表示
//
	/*------------------------------------------------------------------------
	toast出力
		引数
			String str;	表示文字
	------------------------------------------------------------------------*/
	private void ToastShow(String str) {
		Toast tst;
		tst = Toast.makeText(this, str, Toast.LENGTH_LONG);
		// 画面の中央に表示
		tst.setGravity(Gravity.CENTER, 0, 0);
		tst.show();
	}
}
