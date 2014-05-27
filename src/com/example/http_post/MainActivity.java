package com.example.http_post;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	private String userName = null;
	private String videoName = null;
	
	private Button btn = null;
	private TextView tv = null;
	
	private Camera myCamera;
	private SurfaceView mySurfaceView;
	private TextView textView1;
	private Button button1;
	private boolean isRecording;
	private MediaRecorder mediaRecorder;
	private SurfaceHolder v_holder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		textView1 = (TextView) findViewById(R.id.textView1);
		textView1.setText("hello!!!");
		btn = (Button)findViewById(R.id.btn1);
		tv = (TextView)findViewById(R.id.tv1);
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 // 録画中でなければ録画を開始
				  textView1.setText("REC");
				  if (!isRecording) {
					  initializeVideoSettings(); // MediaRecorderの設定
					  mediaRecorder.start(); // 録画開始
					  isRecording = true; // 録画中のフラグを立てる
					  // 録画中であれば録画を停止
				  } else {
					  mediaRecorder.stop(); // 録画停止
					  mediaRecorder.reset(); // 無いとmediarecorder went away with unhandled
					  // events　が発生
					  mediaRecorder.release();
					  mediaRecorder = null;
					  myCamera.lock();
					  //myCamera.release(); // release the camera for other applications
					  //myCamera = null;
					  isRecording = false; // 録画中のフラグを外す
					  textView1.setText("STOP");
				  }
			}
		});
		
		btn.setOnClickListener(this);
		mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		SurfaceHolder holder = mySurfaceView.getHolder();
		holder.addCallback(mSurfaceListener);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		//myCamera = getCameraInstance();
		try {
			  myCamera = Camera.open(1); // attempt to get a Camera instance
		  }catch (Exception e){
			  Log.d("test", "------------------------");
			  Log.d("test", e.getMessage());
			  Log.d("test", "------------------------");
			  // Camera is not available (in use or does not exist)
		  }
	}
	
	private SurfaceHolder.Callback mSurfaceListener = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			try {
			myCamera.setPreviewDisplay(holder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void surfaceDestroyed(SurfaceHolder holder) {
			myCamera.setPreviewCallback(null);
			myCamera.stopPreview();
			myCamera.release();
			myCamera = null;
		}
		
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			// TODO Auto-generated method stub
			v_holder = holder; // SurfaceHolderを保存
			
			myCamera.stopPreview();
			Camera.Parameters parameters = myCamera.getParameters();
			
			List<Size> asizeSupport = parameters.getSupportedPreviewSizes();
			
			//一番小さいプレビューサイズを利用
			Size size = asizeSupport.get(asizeSupport.size() - 4);
			parameters.setPreviewSize(size.width, size.height);
			Log.d("size1", "w=" + String.valueOf(width) + "h=" + String.valueOf(height));
			Log.d("size2", "w=" + String.valueOf(size.width) + "h=" + String.valueOf(size.height));
			LayoutParams paramLayout = mySurfaceView.getLayoutParams();
			paramLayout.width = size.width;
			paramLayout.height = size.height;
			//mySurfaceView.setLayoutParams(paramLayout);
			myCamera.setDisplayOrientation(90);	// カメラを回転
			
			//List<Camera.Size> size = parameters.getSupportedPreviewSizes();
			//Log.d("カメラのサイズ", "w=" + String.valueOf(size.get(0).width) + "h=" + String.valueOf(size.get(0).height));// 
			//parameters.setPreviewSize(size.get(0).width, size.get(0).height);
			// myCamera.setParameters(parameters);
			myCamera.startPreview();
		}
	};
	
	private void initializeVideoSettings() {
		// TODO 自動生成されたメソッド・スタブ
		try {
			//myCamera = getCameraInstance();
			mediaRecorder = new MediaRecorder();
			
			myCamera.unlock();
			mediaRecorder.setCamera(myCamera);
			
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // ファイルフォーマットを指定
			
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // ビデオエンコーダを指定
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			
			mediaRecorder.setOrientationHint(270);
			mediaRecorder.setVideoFrameRate(15); // 動画のフレームレートを指定
			mediaRecorder.setVideoSize(320, 240); // 動画のサイズを指定
			
			//CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
			//mediaRecorder.setProfile(profile);
			
			if(getApplicationInfo().dataDir != null){
				videoName = RandomStringUtils.randomAlphabetic(20);
				File file = new File(getApplicationInfo().dataDir, videoName + ".mp4");
				
				mediaRecorder.setOutputFile(file.getAbsolutePath());
				Log.d("Path", file.getAbsolutePath());
				mediaRecorder.setPreviewDisplay(v_holder.getSurface());
				mediaRecorder.prepare();
			}else{
				
			}
			//mediaRecorder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		// ボタン押下時
		if(v == btn){
			exec_post();
		}
	}
	
	// POST通信を実行（AsyncTask）
	private void exec_post() {
		// 非同期タスクを定義
		HttpPostTask task = new HttpPostTask(this, "http://nuconuco.com:81/create", new HttpPostHandler(){
			
			public void onPostCompleted(String response) {
				// 受信結果をUIに表示
				tv.setText( response );
				Log.d("response", response);
			}
			
			@Override
			public void onPostFailed(String response) {
				tv.setText( response );
				Toast.makeText(getApplicationContext(), "エラーが発生しました。", Toast.LENGTH_LONG).show();
			}
		});
		
		// TODO userName を決める仕組みを作る。
		userName = "山田太郎";
				
		task.addPostParam("userName", userName);
		task.addPostParam("videoName", videoName);
				
		try{
			File file = new File(getApplicationInfo().dataDir, videoName + ".mp4");    
			task.addPostMultiParam(file);
		}catch(Exception e){
			Log.d("test", "-------------------------");
			Log.d("test", e.getMessage());
			Log.d("test", "-------------------------");
		}
				
		// タスクを開始
		task.execute();	
	}

}
