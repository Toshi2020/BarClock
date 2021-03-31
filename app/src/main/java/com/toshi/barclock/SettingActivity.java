package com.toshi.barclock;
/*****************************************************************************
*
*	BarClock:SettingActivity -- セッティング用画面
*
*	V1.0.0	2021/03/31	initial revision by	toshi
*
*****************************************************************************/

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.graphics.Color;
import android.widget.TextView;
import java.util.Locale;

public class SettingActivity extends AppCompatActivity
			implements View.OnClickListener,
			 OnSeekBarChangeListener {

	private Button mBtnCancel, mBtnOk;
	private SeekBar mSeekBarR1, mSeekBarG1, mSeekBarB1;
	private SeekBar mSeekBarR2, mSeekBarG2, mSeekBarB2;
	private TextView mTextView1, mTextView2;
	private TextView mTextViewR1, mTextViewG1, mTextViewB1;
	private TextView mTextViewR2, mTextViewG2, mTextViewB2;
	private int ColR1, ColG1, ColB1, ColR2, ColG2, ColB2;
	private int TextColor, BackColor;
	private static final int REQUESTCODE_COLOR = 1;

	/*------------------------------------------------------------------------
	アクティビティ開始時の処理
	------------------------------------------------------------------------*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		// アクションバーに戻るボタンを付ける
		ActionBar actionbar = getSupportActionBar();
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		setTitle(getString(R.string.title_setting));

		// カラー設定を受け取る
		Intent intent = getIntent();
		TextColor = intent.getIntExtra("TextColor", 0xffffff);
		BackColor = intent.getIntExtra("BackColor", 0x000088);

		// リソースを得る
		mBtnCancel = (Button) findViewById(R.id.buttonCancel);
		mBtnOk = (Button) findViewById(R.id.buttonOk);
		mSeekBarR1 = (SeekBar)findViewById(R.id.seekBarR1);
		mSeekBarG1 = (SeekBar)findViewById(R.id.seekBarG1);
		mSeekBarB1 = (SeekBar)findViewById(R.id.seekBarB1);
		mSeekBarR2 = (SeekBar)findViewById(R.id.seekBarR2);
		mSeekBarG2 = (SeekBar)findViewById(R.id.seekBarG2);
		mSeekBarB2 = (SeekBar)findViewById(R.id.seekBarB2);
		mTextViewR1 = (TextView)findViewById(R.id.textViewR1);
		mTextViewG1 = (TextView)findViewById(R.id.textViewG1);
		mTextViewB1 = (TextView)findViewById(R.id.textViewB1);
		mTextViewR2 = (TextView)findViewById(R.id.textViewR2);
		mTextViewG2 = (TextView)findViewById(R.id.textViewG2);
		mTextViewB2 = (TextView)findViewById(R.id.textViewB2);
		mTextView1 = (TextView)findViewById(R.id.textView1);
		mTextView2 = (TextView)findViewById(R.id.textView2);
		// リスナの設定
		mBtnCancel.setOnClickListener(this);
		mBtnOk.setOnClickListener(this);
		mSeekBarR1.setOnSeekBarChangeListener(this);
		mSeekBarG1.setOnSeekBarChangeListener(this);
		mSeekBarB1.setOnSeekBarChangeListener(this);
		mSeekBarR2.setOnSeekBarChangeListener(this);
		mSeekBarG2.setOnSeekBarChangeListener(this);
		mSeekBarB2.setOnSeekBarChangeListener(this);

		// カラーをRGBに分解
		SetColor();
		// ビューの色を変更
		SetTextColor();

		// 初期値の設定
		mSeekBarR1.setProgress(ColR1 / 16);
		mSeekBarG1.setProgress(ColG1 / 16);
		mSeekBarB1.setProgress(ColB1 / 16);
		mSeekBarR2.setProgress(ColR2 / 16);
		mSeekBarG2.setProgress(ColG2 / 16);
		mSeekBarB2.setProgress(ColB2 / 16);
		mTextViewR1.setText(String.format(Locale.US, "%02d", ColR1	/ 16));
		mTextViewG1.setText(String.format(Locale.US, "%02d", ColG1	/ 16));
		mTextViewB1.setText(String.format(Locale.US, "%02d", ColB1	/ 16));
		mTextViewR2.setText(String.format(Locale.US, "%02d", ColR2	/ 16));
		mTextViewG2.setText(String.format(Locale.US, "%02d", ColG2	/ 16));
		mTextViewB2.setText(String.format(Locale.US, "%02d", ColB2	/ 16));
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	/*------------------------------------------------------------------------
	ボタンクリックリスナ
	------------------------------------------------------------------------*/
	@Override
	public void onClick(View v) {

		Intent intent;

		// ボタン1が押された
		if (v.equals(mBtnOk)) {
			intent = new Intent();
			intent.putExtra("TextColor", TextColor);
			intent.putExtra("BackColor", BackColor);
			setResult(RESULT_OK, intent);
		} else if (v.equals(mBtnCancel)) {
		}
		finish();
	}
	/*------------------------------------------------------------------------
	シークバーリスナ
	------------------------------------------------------------------------*/
	// つまみをドラッグしたときに呼ばれる
	public void onProgressChanged(SeekBar seekBar, int progress,
		boolean fromUser) {
		String str = String.format(Locale.US, "%02d", progress);
		int col;
		if (progress >= 15)
			col = 255;
		else if (progress <= 0)
			col = 0;
		else
			col = progress * 16 + 8;

		if (seekBar.equals(mSeekBarR1)) {
			ColR1 = progress * 16;
			mTextViewR1.setText(str);
		} else if (seekBar.equals(mSeekBarG1)) {
			ColG1 = col;
			mTextViewG1.setText(str);
		} else if (seekBar.equals(mSeekBarB1)) {
			ColB1 = col;
			mTextViewB1.setText(str);
		} else if (seekBar.equals(mSeekBarR2)) {
			ColR2 = col;
			mTextViewR2.setText(str);
		} else if (seekBar.equals(mSeekBarG2)) {
			ColG2 = col;
			mTextViewG2.setText(str);
		} else if (seekBar.equals(mSeekBarB2)) {
			ColB2 = col;
			mTextViewB2.setText(str);
		}
		// ビューの色を変更
		SetTextColor();
	}
	// つまみに触れたときに呼ばれる
	public void onStartTrackingTouch(SeekBar seekBar) {
	}
 
	// つまみを離したときに呼ばれる
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	// ビューの色を変更
	private void SetTextColor() {
		TextColor = ColR1 << 16 |  ColG1 << 8 | ColB1;
		BackColor = ColR2 << 16 |  ColG2 << 8 | ColB2;
		mTextView1.setTextColor(Color.rgb(ColR1, ColG1, ColB1));
		mTextView2.setTextColor(Color.rgb(ColR1, ColG1, ColB1));
		mTextView1.setBackgroundColor(Color.rgb(ColR2, ColG2, ColB2));
		mTextView2.setBackgroundColor(Color.rgb(ColR2, ColG2, ColB2));
	}
	// カラーをRGBに分解
	public void SetColor() {
		ColR1 = (TextColor >> 16) & 0xff;
		ColG1 = (TextColor >> 8) & 0xff;
		ColB1 = TextColor & 0xff;
		ColR2 = (BackColor >> 16) & 0xff;
		ColG2 = (BackColor >> 8) & 0xff;
		ColB2 = BackColor & 0xff;
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
