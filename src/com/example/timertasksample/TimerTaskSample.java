package com.example.timertasksample;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class TimerTaskSample extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// 描画クラスのインスタンスを生成
		MySurfaceView mSurfaceView = new MySurfaceView(this);
		setContentView(mSurfaceView);
	}
}

class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	/** 太鼓画像データを保持する。 */
	private Bitmap taikoOffImage;
	private Bitmap taikoOnImage;
	/** 太鼓画像の原点（左上）のx座標を保持する。 */
	private int TAIKO_X = 100;
	private int taikoX = TAIKO_X;
	/** 太鼓画像の原点（左上）のy座標を保持する。 */
	private int TAIKO_Y = 50;
	private int taikoY = TAIKO_Y;
	/** サウンド再生データを保持する。 */
	private MediaPlayer mp;
	/** 太鼓押下フラグ */
	private boolean isTaiko = false;
	/** 　太鼓を上下に動かすためのスレッド */
	private MyTimerTask task;
	private Timer timer;

	public MySurfaceView(Context context) {
		super(context);
		// イベント取得できるようにFocusを有効にする
		setFocusable(true);
		// Resourceインスタンスの生成
		Resources res = this.getContext().getResources();
		// 画像の読み込み（/res/drawable-hdpi/taiko.png）
		taikoOffImage = BitmapFactory.decodeResource(res, R.drawable.taiko);
		taikoOnImage = BitmapFactory.decodeResource(res, R.drawable.taicoclick);
		// サウンドデータを読み込む(/res/raw/pon.mp3)
		mp = MediaPlayer.create(context, R.raw.pon);

		// Callbackを登録する
		getHolder().addCallback(this);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// 描画の開始
		timer = new Timer();
		task = new MyTimerTask(height);
		timer.schedule(task, 50, 50);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// バックグラウンドの処理が動いている場合は、終了させなければいけない
		if (task != null) {
			task.cancel();
			task = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	/**
	 * タッチイベント
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// 指がタッチされたx,y座標の取得
			int touchX = (int) event.getX();
			int touchY = (int) event.getY();

			// 太鼓の中心座標を計算
			int centerX = taikoX + taikoOffImage.getWidth() / 2;
			int centerY = taikoY + taikoOffImage.getHeight() / 2;

			// 太鼓の中心座標と指の距離を計算
			double distance = Math.sqrt(Math.pow((centerX - touchX), 2)
					+ Math.pow((centerY - touchY), 2));

			// 太鼓画像の半径
			int taikoR = taikoOffImage.getWidth() / 2;

			// あたり判定
			if (distance < taikoR) {
				// サウンド再生
				mp.start();
				isTaiko = true;
				drawTaiko();
			}

		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			isTaiko = false;
			drawTaiko();
		}
		return true;
	}

	/**
	 * 太鼓の画像を描画するメソッド
	 */
	private void drawTaiko() {
		try {
			// Canvasを取得する
			Canvas canvas = getHolder().lockCanvas();
	
			// 背景色を設定する
			canvas.drawColor(Color.BLACK);
	
			// Bitmapイメージの描画
			Paint mPaint = new Paint();
			if (!isTaiko) {
				canvas.drawBitmap(taikoOffImage, taikoX, taikoY, mPaint);
			} else {
				canvas.drawBitmap(taikoOnImage, taikoX, taikoY, mPaint);
			}
	
			// 画面に描画をする
			getHolder().unlockCanvasAndPost(canvas);
		} catch (Exception e) {}
	
	}

	// 定期処理を行うクラス
	private class MyTimerTask extends TimerTask {
		/**
		 * 増分値
		 */
		private int move = 50;
		/**
		 * 縦のサイズ
		 */
		private int height = 0;

		MyTimerTask(int height) {
			this.height = height;
		}

		@Override
		public void run() {
			//太鼓がはみ出しそうになったら、増やす値の向きを変える
			if ((taikoY + taikoOffImage.getHeight()) > height) {
				move = -50;
			}
			if (taikoY < 0) {
				move = 50;
			}
			taikoY += move;
			drawTaiko();
		}
	}

}