package com.ftp.upload;

import java.io.File;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	static final String FTP_HOST= "xxx.xxx.xxx.xxx"; // input FTP Host
	
	static String FTP_USER;
	
	static String FTP_PASS;
	
	Button mLoginBtn, mSelectBtn, mUploadBtn;
	String TAG = "connect";
	EditText mAccountEditText, mPasswordEditText;
	FTPClient mFtpClient;
	File mFile;
	String getRealPath;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(this);
        mSelectBtn = (Button) findViewById(R.id.select_btn);
        mSelectBtn.setOnClickListener(this);
        mSelectBtn.setVisibility(View.GONE);
        mUploadBtn = (Button) findViewById(R.id.upload_btn);
        mUploadBtn.setOnClickListener(this);
        mUploadBtn.setVisibility(View.GONE);
        mAccountEditText = (EditText) findViewById(R.id.account_editText);
        mPasswordEditText = (EditText) findViewById(R.id.password_editText);
    }
    
    @SuppressLint({ "SdCardPath", "NewApi" }) 
    public void onClick(View v) {
		switch (v.getId()){
		case R.id.login_btn:
			Log.i(TAG, "editText1=" + mAccountEditText.getText().toString());
			Log.i(TAG, "editText2=" + mPasswordEditText.getText().toString());
			FTP_USER = mAccountEditText.getText().toString();
			FTP_PASS = mPasswordEditText.getText().toString();
			Log.i(TAG, "FTP_USER & FTP_PASS set");
			connect();
			break;
		case R.id.select_btn:
			Log.i(TAG, "press select file button");
			Intent intent = new Intent(Intent.ACTION_PICK);
			Log.i(TAG, "Intent.ACTION_PICK");
			intent.setType("image/*");
			Intent showResult = Intent.createChooser(intent, "Please select one file");
			startActivityForResult(showResult, 0);
			break;
		case R.id.upload_btn:
			uploadFile(mFile);
			break;
		}
	}
    
    public void connect(){
    	Log.i(TAG, "into connect");
    	mFtpClient = new FTPClient();
    	try {
    		mFtpClient.connect(FTP_HOST, 21);
    		mFtpClient.login(FTP_USER, FTP_PASS);
			Log.i(TAG, "connected");
			Toast.makeText(getBaseContext(),"Connect success", Toast.LENGTH_SHORT).show();
			mSelectBtn.setVisibility(View.VISIBLE);			
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "Account wrong: " + e.toString());
			Toast.makeText(getBaseContext(),"Wrong account...", Toast.LENGTH_LONG).show();
			try {
				mFtpClient.disconnect(true);	
				Log.i(TAG, "client disconnect");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// 有選擇檔案
		if (resultCode == RESULT_OK) {
			// 取得檔案的 Uri
			Uri uri = data.getData();
			Log.i(TAG, "uri=" + uri.toString());
			getRealPath = getRealPathFromURI(MainActivity.this, uri);
			Log.i(TAG, "getRealPath=" + getRealPath);
			mFile = new File(getRealPath);
			mUploadBtn.setVisibility(View.VISIBLE);
		} else {
			Toast.makeText(getBaseContext(), "no select", Toast.LENGTH_SHORT).show();
		}
	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void uploadFile(File fileName){
    	try {
			mFtpClient.setType(FTPClient.TYPE_BINARY);
			mFtpClient.upload(fileName, new MyTransferListener());
			Log.i(TAG, "upload File");
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, e.toString());
			try {
				mFtpClient.disconnect(true);	
				Log.i(TAG, "client disconnect");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
    
    public class MyTransferListener implements FTPDataTransferListener {

    	public void started() {
    		mSelectBtn.setVisibility(View.GONE);
    		// Transfer started
    		Toast.makeText(getBaseContext(), "Upload Started...", Toast.LENGTH_SHORT).show();
    	}

    	public void transferred(int length) {
    		// Yet other length bytes has been transferred since the last time this
    		// method was called
    		Toast.makeText(getBaseContext(), "transferring...", Toast.LENGTH_SHORT).show();
    	}

    	public void completed() {
    		mSelectBtn.setVisibility(View.VISIBLE);
    		// Transfer completed
    		Toast.makeText(getBaseContext(), "Completed!", Toast.LENGTH_SHORT).show();
    		mUploadBtn.setVisibility(View.GONE);
    	}

    	public void aborted() {
    		mSelectBtn.setVisibility(View.VISIBLE);
    		// Transfer aborted
    		Toast.makeText(getBaseContext(),"Transfer aborted, please try again.", Toast.LENGTH_SHORT).show();
    	}

    	public void failed() {
    		mSelectBtn.setVisibility(View.VISIBLE);
    		// Transfer failed
    		Toast.makeText(getBaseContext(),"Failed!", Toast.LENGTH_SHORT).show();
    	}
    }
}
