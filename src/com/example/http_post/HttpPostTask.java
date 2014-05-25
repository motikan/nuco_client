package com.example.http_post;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpPostTask extends AsyncTask<Void, Void, Void> {
	
	// �ݒ莖��
	private String request_encoding = "UTF-8";
	private String response_encoding = "UTF-8";
	
	// ����������
	private Activity parent_activity = null;
	private String post_url = null;
	private Handler ui_handler = null;
	private List<NameValuePair> post_params = null;
	
	// �������Ɏg�������o
	private ResponseHandler<Void> response_handler = null;
	private String http_err_msg = null;
	private String http_ret_msg = null;
	private ProgressDialog dialog = null;
	
	private MultipartEntity uploadFile = null;
	
	
	
	// ������
	public HttpPostTask( Activity parent_activity, String post_url, Handler ui_handler ){
		// ������
		this.parent_activity = parent_activity;
		this.post_url = post_url;
		this.ui_handler = ui_handler;
		
		// ���M�p�����[�^�͏����������Cnew���set������
		post_params = new ArrayList<NameValuePair>();
	}
	
	/* --------------------- POST�p�����[�^ --------------------- */
	
	// �ǉ�
	public void addPostParam( String post_name, String post_value ){
		post_params.add(new BasicNameValuePair( post_name, post_value ));
	}
	
	public void addPostMultiParam(MultipartEntity uploadfile_tmp){
		this.uploadFile = uploadfile_tmp;
	}
	
	/* --------------------- �����{�� --------------------- */
	
	// �^�X�N�J�n��
	protected void onPreExecute() {
		// �_�C�A���O��\��
		dialog = new ProgressDialog( parent_activity );
		dialog.setMessage("�ʐM���E�E�E");
		dialog.show();
		
		// ���X�|���X�n���h���𐶐�
		response_handler = new ResponseHandler<Void>() {
			
			// HTTP���X�|���X����C��M��������G���R�[�h���ĕ�����Ƃ��ĕԂ�
			@Override
			public Void handleResponse(HttpResponse response) throws IOException{
				Log.d("posttest", "���X�|���X�R�[�h�F" + response.getStatusLine().getStatusCode());
				
				// ����Ɏ�M�ł����ꍇ��200
				switch (response.getStatusLine().getStatusCode()) {
				case HttpStatus.SC_OK:
					Log.d("posttest", "���X�|���X�擾�ɐ���");
					
					// ���X�|���X�f�[�^���G���R�[�h�ς݂̕�����Ƃ��Ď擾����
					// ��IOException�̉\������
					HttpPostTask.this.http_ret_msg = EntityUtils.toString(response.getEntity(), HttpPostTask.this.response_encoding);
					break;
					
				case HttpStatus.SC_NOT_FOUND:
					// 404
					Log.d("posttest", "���݂��Ȃ�");
					HttpPostTask.this.http_err_msg = "404 Not Found";
					break;
					
				default:
					Log.d("posttest", "�ʐM�G���[");
					HttpPostTask.this.http_err_msg = "�ʐM�G���[������";
				}
				
				return null;
			}
		};
	}
	
	// ���C������
	protected Void doInBackground(Void... unused) {
		
		Log.d("posttest", "post���܂�");
		
		// URL
		URI url = null;
		try {
			url = new URI( post_url );
			Log.d("posttest", "URL��OK");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			http_err_msg = "�s����URL";
			return null;
		}
		
		// POST�p�����[�^�t����POST���N�G�X�g���\�z
		HttpPost request = new HttpPost( url );
		try {
			// ���M�p�����[�^�̃G���R�[�h���w��
			request.setEntity(new UrlEncodedFormEntity(post_params, request_encoding));
			if(uploadFile != null){
				request.setEntity(uploadFile);
			}else{
				Log.d("TEST", "HOGEHOGE");
			}
		}catch (UnsupportedEncodingException e1){
			e1.printStackTrace();
			http_err_msg = "�s���ȕ����R�[�h";
			return null;
		}
		
		// POST���N�G�X�g�����s
		HttpClient httpClient = new DefaultHttpClient();
		Log.d("posttest", "POST�J�n");
		try {
			httpClient.execute(request, response_handler);
		}catch (ClientProtocolException e) {
			e.printStackTrace();
			http_err_msg = "�v���g�R���̃G���[";
		}catch (IOException e){
			e.printStackTrace();
			Log.d("io_exception", e.getMessage());
			http_err_msg = "IO�G���[";
		}
		
		// shutdown����ƒʐM�ł��Ȃ��Ȃ�
		httpClient.getConnectionManager().shutdown();
		
		return null;
	}
	
	// �^�X�N�I����
	protected void onPostExecute(Void unused) {
		// �_�C�A���O������
		dialog.dismiss();
		
		// ��M���ʂ�UI�ɓn�����߂ɂ܂Ƃ߂�
		Message message = new Message();
		Bundle bundle = new Bundle();
		if (http_err_msg != null) {
			// �G���[������
			bundle.putBoolean("http_post_success", false);
			bundle.putString("http_response", http_err_msg);
		} else {
			// �ʐM������
			bundle.putBoolean("http_post_success", true);
			bundle.putString("http_response", http_ret_msg);
		}
		message.setData(bundle);
		
		// ��M���ʂɊ�Â���UI���삳����
		ui_handler.sendMessage(message);
	}
}