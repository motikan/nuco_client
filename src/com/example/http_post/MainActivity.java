package com.example.http_post;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	private Button btn = null;
	private TextView tv = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btn = (Button)findViewById(R.id.btn1);
		tv = (TextView)findViewById(R.id.tv1);
		
		btn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// ボタン押下時
		if(v == btn){
			exec_post();
		}
	}
	
	// POST通信を実行（AsyncTask）
	@SuppressWarnings("deprecation")
	private void exec_post() {
		
		// 非同期タスクを定義
		HttpPostTask task = new HttpPostTask(this, "http://oncetec.sub.jp/php/011/index2.php", new HttpPostHandler(){
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
		
		task.addPostParam( "post_1", "ユーザID" );
		task.addPostParam( "post_2", "パスワード" );
		
		try{
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			File file = new File(getMount_sd(), "test.mp4");    
		    FileBody fileBody = new FileBody(file, "image/png"); 
			multipartEntity.addPart("upfile", fileBody);
			task.addPostMultiParam(multipartEntity);
		}catch(Exception e){
			Log.d("test", "-------------------------");
			Log.d("test", e.getMessage());
			Log.d("test", "-------------------------");
		}
		
		// タスクを開始
		task.execute();
	
	}
	
	private String getMount_sd() {
		   List<String> mountList = new ArrayList<String>();
		   String mount_sdcard = null;

		   Scanner scanner = null;
		   try {
		      // システム設定ファイルにアクセス
		      File vold_fstab = new File("/system/etc/vold.fstab");
		      scanner = new Scanner(new FileInputStream(vold_fstab));
		      // 一行ずつ読み込む
		      while (scanner.hasNextLine()) {
		         String line = scanner.nextLine();
		         // dev_mountまたはfuse_mountで始まる行の
		         if (line.startsWith("dev_mount") || line.startsWith("fuse_mount")) {	            	
		            // 半角スペースではなくタブで区切られている機種もあるらしいので修正して
		            // 半角スペース区切り３つめ（path）を取得
		            String path = line.replaceAll("\t", " ").split(" ")[2];
		            // 取得したpathを重複しないようにリストに登録
		            if (!mountList.contains(path)){
		               mountList.add(path);
		            }
		         }
		      }
		   } catch (FileNotFoundException e) {
		      throw new RuntimeException(e);
		   } finally {
		      if (scanner != null) {
		         scanner.close();
		      }
		   }

		   // Environment.isExternalStorageRemovable()はGINGERBREAD以降しか使えない
		   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){	    	
		      // getExternalStorageDirectory()が罠であれば、そのpathをリストから除外
		      if (!Environment.isExternalStorageRemovable()) {   // 注1
		         mountList.remove(Environment.getExternalStorageDirectory().getPath());
		      }
		   }

		   // マウントされていないpathは除外
		   for (int i = 0; i < mountList.size(); i++) {
		      if (!isMounted(mountList.get(i))){
		         mountList.remove(i--);
		      }
		   }

		   // 除外されずに残ったものがSDカードのマウント先
		   if(mountList.size() > 0){
		      mount_sdcard = mountList.get(0);
		   }
			    
		   // マウント先をreturn（全て除外された場合はnullをreturn）
		   return mount_sdcard;
		}

		// 引数に渡したpathがマウントされているかどうかチェックするメソッド
		public boolean isMounted(String path) {
		   boolean isMounted = false;

		   Scanner scanner = null;
		   try {
		      // マウントポイントを取得する
		      File mounts = new File("/proc/mounts");   // 注2
		      scanner = new Scanner(new FileInputStream(mounts));
		      // マウントポイントに該当するパスがあるかチェックする
		      while (scanner.hasNextLine()) {
		         if (scanner.nextLine().contains(path)) {
		            // 該当するパスがあればマウントされているってこと
		            isMounted = true;
		            break;
		         }
		      }
		   } catch (FileNotFoundException e) {
		      throw new RuntimeException(e);
		   } finally {
		      if (scanner != null) {
		      scanner.close();
		      }
		   }

		   // マウント状態をreturn
		   return isMounted;
		}

}
