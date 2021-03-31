package com.toshi.barclock;
/*****************************************************************************
*
*	BarClock:ClockReceiver -- ステータスバーに日時を表示するサービス
*
*	V1.0.0	2021/03/31	initial revision by	toshi
*
*****************************************************************************/

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import java.util.Timer;
import java.util.TimerTask;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import android.widget.Toast;
import android.os.Build;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


//****************************************************************************
// インスタンス作成時
//
public class ClockService extends Service {
	private static final String TAG = "■ClockService ";
	private View view;
	private WindowManager windowManager;
	private float dpScale , spScale;
	private Timer mTimer = null;
	Handler mHandler = new Handler();
	private FrameLayout mFrameLayout;
	private LinearLayout mLinearLayout;
	private TextView mText, mText1, mText2;
	private static boolean fServiceRunning;	// サービス起動中フラグ
	private int statusBarHeight = 24;	// 取得できなかった場合のデフォルト
	private int fontHeight;
	private SimpleDateFormat sCalForm = new SimpleDateFormat(
											"yyyy MM/dd(E) HH:mm");
	private BroadcastReceiver mReceiver = null;
	public static int TextColor = 0xffffff, BackColor = 0x000088;

	/*------------------------------------------------------------------------
	インスタンス作成時
	------------------------------------------------------------------------*/
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG,"onCreate");

		// テーマを設定
		setTheme(R.style.AppTheme);

		// dpを掛けるとピクセル数pxになる。px=dpScale*dp
		dpScale = getResources().getDisplayMetrics().density;
		spScale = getResources().getDisplayMetrics().scaledDensity;
		if (dpScale == 0.0) dpScale = 1.0f;

		// ステータスバーの高さ(px)
		int resourceId = getResources().getIdentifier(
							"status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			statusBarHeight =
				getResources().getDimensionPixelSize(resourceId);
		}
		Log.d(TAG, "通知バー高さ = " + statusBarHeight);
		// フォントの高さ(dp)
		fontHeight = (int)((float)statusBarHeight / spScale);
		Log.d(TAG, "フォント高さ = " + fontHeight);

		// SCREEN_ONを受けるためのレシーバを用意
		IntentFilter filter = new IntentFilter();
		// 画面の電源が入れられた
		filter.addAction(Intent.ACTION_SCREEN_ON);
		// 端末が目覚めた
		filter.addAction(Intent.ACTION_USER_PRESENT);
		// ヘッドセットが抜き差しされた
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		// レシーバー登録
		mReceiver = new MyReceiver();
		registerReceiver(mReceiver, filter);
	}

	/*------------------------------------------------------------------------
	インスタンス破壊時
	------------------------------------------------------------------------*/
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"onDestroy");

		// Viewを削除
		windowManager.removeView(view);

		// タイマー停止
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		// レシーバ削除
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}

		fServiceRunning = false;	// サービス非作動
	}
	/*------------------------------------------------------------------------
	bind要求時(使われない)
	------------------------------------------------------------------------*/
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG,"onBind");
		return null;
	}
	/*------------------------------------------------------------------------
	サービス開始要求時
	------------------------------------------------------------------------*/
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d(TAG,"fServiceRunning="+fServiceRunning);

		// サービス作動中なら
		if (fServiceRunning == true) {
			Log.d(TAG,"ReStartCommand skip..");
			if (BuildConfig.DEBUG) {
				Toast.makeText(this, "2回目始動要求",
					Toast.LENGTH_LONG).show();
			}
			return START_STICKY;
		}
		Log.d(TAG,"StartCommand");
		if (BuildConfig.DEBUG) {
			Toast.makeText(this, "サービス始動", Toast.LENGTH_LONG).show();
		}
		fServiceRunning = true;	// サービス作動中

		// API 26 以上
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// startForegroundService() -----
			Context context = getApplicationContext();
			String channelId = "default";
			String title = context.getString(R.string.app_name);

			// メインActivityを起動させるためのインテント
			Intent intentm = new Intent();
			intentm.setAction(Intent.ACTION_VIEW);
			intentm.setClassName("com.toshi.barclock", 
								"com.toshi.barclock.MainActivity");

			PendingIntent pendingIntent =
					PendingIntent.getActivity(context, 0, intentm,
							PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationManager notificationManager =
					(NotificationManager)context.getSystemService(
										Context.NOTIFICATION_SERVICE);

			// Notification　Channel 設定
			NotificationChannel channel = new NotificationChannel(
					channelId, title ,
					NotificationManager.IMPORTANCE_DEFAULT);

			if(notificationManager != null){
				notificationManager.createNotificationChannel(channel);

				Notification notification = 
					new Notification.Builder(context, channelId)
						.setContentTitle(title)
						// android標準アイコンから時計っぽいのを選択
						.setSmallIcon(
								android.R.drawable.ic_menu_recent_history)
						.setContentText(getString(R.string.info_service))
						.setAutoCancel(true)
						.setContentIntent(pendingIntent)
						.setWhen(System.currentTimeMillis())
						.build();

				// startForeground
				startForeground(1, notification);
			}
			// ----- startForegroundService()
		}

		// プリファレンスから設定値を読み込む
		SharedPreferences pref = PreferenceManager.
										getDefaultSharedPreferences(this);
		TextColor = pref.getInt("TextColor", TextColor);
		BackColor = pref.getInt("BackColor", BackColor);

		// inflaterの生成
		LayoutInflater layoutInflater = LayoutInflater.from(this);

		int typeLayer;
		// API 26 以上
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			typeLayer = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		}else {
			typeLayer = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		}

		windowManager = (WindowManager)getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams (
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				typeLayer,
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
						WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
						WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);

		// 上中央に配置
		params.gravity=  Gravity.TOP | Gravity.CENTER;
		params.x = 0;
		params.y = -statusBarHeight;	// px

		// レイアウトファイルからInfalteするViewを作成
		final ViewGroup nullParent = null;
		view = layoutInflater.inflate(R.layout.service_layer, nullParent);

		// リソースの取得
		mLinearLayout = view.findViewById(R.id.layout1);
		mText1 = view.findViewById(R.id.textView1);
		mText2 = view.findViewById(R.id.textView2);
		mText = view.findViewById(R.id.textView);

		// 文字のサイズを設定
		mText1.setHeight(statusBarHeight / 2);
		mText2.setHeight(statusBarHeight / 2);
		mText.setHeight(statusBarHeight);
		mText.setTextSize((int)((float)fontHeight * 0.7f));
		mText1.setTextSize((int)((float)(fontHeight / 2) * 0.6f));
		mText2.setTextSize((int)((float)(fontHeight / 2) * 0.6f));

		// 文字と背景の色をセット
		SetTextViewColor();
		// 日時をTextViewにセット
		CalSet();

		// Viewを画面上に追加
		windowManager.addView(view, params);

		// タイマーの設定 1秒毎にループ
		mTimer = new Timer(true);
		mTimer.schedule( new TimerTask(){
			@Override
			public void run(){
				mHandler.post( new Runnable(){
					public void run(){
						// 文字と背景の色をセット
						SetTextViewColor();
						// 日時をTextViewにセット
						CalSet();
					}
				});
			}
		}, 1000, 1000);

		return super.onStartCommand(intent, flags, startId);
	}
	/*------------------------------------------------------------------------
	日時をTextViewにセット
	------------------------------------------------------------------------*/
	private void CalSet() {
		// カレンダー取得
		Calendar now = Calendar.getInstance();
       	String scal = sCalForm.format(now.getTime());
       	String ss[] = scal.split(" ", 0);
		String s1, s2, s3;
		s1 = scal.substring(0, 4);
		s2 = scal.substring(5, 14);
		s3 = scal.substring(14);
		mText1.setText(ss[0]);			// 年
		mText2.setText(ss[1] + " ");	// 月日(曜日)
		mText.setText(ss[2]);			// 時刻
	}
	/*------------------------------------------------------------------------
	サービス起動中かどうかを返す
	------------------------------------------------------------------------*/
	public static boolean IsServiceRunning() {
		return fServiceRunning;
	}
	/*------------------------------------------------------------------------
	文字と背景の色を変更
	------------------------------------------------------------------------*/
	private void SetTextViewColor() {
		int col;
		col = (0xff << 24) | TextColor;
		mText1.setTextColor(col);
		mText2.setTextColor(col);
		mText.setTextColor(col);

		// mLinearLayout.setBackgroundColor(col);だと角の丸みが失われてしまう
		col = (0xff << 24) | BackColor;
		GradientDrawable shape = new GradientDrawable();
		shape.setShape(GradientDrawable.RECTANGLE);
		shape.setColor(col);		// 色を設定
		shape.setCornerRadius(6/*dp*/ * dpScale);	// 角を丸める(ピクセル)
		mLinearLayout.setBackground(shape);
	}
	/*------------------------------------------------------------------------
	文字と背景の色をセット
	------------------------------------------------------------------------*/
	public static void SetColor(int textcolor, int backcolor) {
		TextColor = textcolor;
		BackColor = backcolor;
	}
}
