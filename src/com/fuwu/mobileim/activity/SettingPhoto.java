package com.fuwu.mobileim.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.Urlinterface;

/**
 * @作者 丁作强
 * @时间 2014-4-12 上午9:35:06
 */
public class SettingPhoto extends Activity implements Urlinterface,OnTouchListener {

	// 设置 界面
	String delUri = "";
	String photo = Environment.getExternalStorageDirectory() + "/"
			+ IMAGE_FILE_NAME;
	String photoStr = Environment.getExternalStorageDirectory() + "/1"
			+ IMAGE_FILE_NAME;
	/* 头像名称 */
	private static final String IMAGE_FILE_NAME = "faceImage.jpg";
	SharedPreferences preferences;
	private int version = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.settingphoto);
		Setwindow(0.19f);// 设置窗口化
		findViewById(R.id.lookphoto).setOnTouchListener(this);
		findViewById(R.id.paizhaoshangchuan).setOnTouchListener(this);
		findViewById(R.id.congxiangce).setOnTouchListener(this);
		String release = android.os.Build.VERSION.RELEASE; // android系统版本号
//		version = Integer.parseInt(release.substring(0, 1));
//		if (version < 4) {
//			findViewById(R.id.set_photolayout).setBackgroundResource(
//					R.drawable.photo_shape2);
//		}
		preferences = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		File file = new File(photoStr);
		if (file.exists()) {
			file.delete();
		}
		File file2 = new File(photo);
		if (file2.exists()) {
			file2.delete();
		}
	}

	public void Setwindow(float s) {
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高

		android.view.WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
//		p.height = (int) (d.getHeight() * s); // 高度设置
		p.width = (int) (d.getWidth() * 0.9); // 宽度设置
		p.alpha = 1.0f; // 设置本 身透明度
		p.dimAmount = 0.8f; // 设置黑暗度
		p.y = -150; // 设置位置
		getWindow().setAttributes(p); // 设置生效

	}
	
	/**
	 * 查看头像
	 */
	public void set_lookphoto(View v) {

		Intent intent = new Intent();
		intent.putExtra("image_path",
				Urlinterface.head_pic + preferences.getInt("user_id", -1));
		intent.setClass(SettingPhoto.this,
				ComtactZoomImageActivity.class);
		startActivity(intent);

	}
	/**
	 * 拍照上传
	 */
	public void set_paizhaoshangchuan(View v) {

		try {
			Intent intentFromCapture = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			// 判断存储卡是否可以用，可用进行存储
			if (FuXunTools.isHasSdcard()) {
				File file = new File(photo);

				if (file.exists()) {
					file.delete();
				}
				file = new File(photo);
				if (!file.exists()) {
					intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri
							.fromFile(new File(Environment
									.getExternalStorageDirectory(),
									IMAGE_FILE_NAME)));
				}
			}

			startActivityForResult(intentFromCapture, 2);
		} catch (Exception e) {

			Toast.makeText(getApplicationContext(), "调用相机失败", 0).show();
		}

	}

	/**
	 * 从相册选择
	 */
	public void set_congxiangce(View v) {

		Intent intentFromGallery = new Intent(Intent.ACTION_PICK, null);

		/**
		 * 下面这句话，与其它方式写是一样的效果，如果：
		 * intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		 * intent.setType(""image/*");设置数据类型
		 * 如果朋友们要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
		 * 这个地方小马有个疑问，希望高手解答下：就是这个数据URI与类型为什么要分两种形式来写呀？有什么区别？
		 */
		intentFromGallery.setDataAndType(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		startActivityForResult(intentFromGallery, 1);
		// Intent intentFromGallery = new Intent();
		// intentFromGallery.setType("image/*"); // 设置文件类型
		// intentFromGallery
		// .setAction(Intent.ACTION_GET_CONTENT);
		// startActivityForResult(intentFromGallery,1);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != 0) {

			switch (requestCode) {
			// 如果是直接从相册获取
			case 1:
				startPhotoZoom(data.getData());
				break;
			// 如果是调用相机拍照时
			case 2:

				if (FuXunTools.isHasSdcard()) {
					File temp = new File(photo);
					startPhotoZoom(Uri.fromFile(temp));
				} else {
					Toast.makeText(this, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG)
							.show();
				}

				break;
			// 取得裁剪后的图片
			case 3:
				/**
				 * 非空判断大家一定要验证，如果不验证的话， 在剪裁之后如果发现不满意，要重新裁剪，丢弃
				 * 当前功能时，会报NullException，小马只 在这个地方加下，大家可以根据不同情况在合适的 地方做判断处理类似情况
				 * 
				 */
				if (data != null) {
					getImageToView(data);
				}
				break;
			default:
				break;

			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 180);
		intent.putExtra("outputY", 180);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}

	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	private void getImageToView(Intent data) {

		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(photo);
			File file = new File(photoStr);
			try {
				if (file.exists()) {
					file.delete();
				}

				file.createNewFile();
				FileOutputStream stream = new FileOutputStream(file);
				ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.PNG, 90, stream);
				byte[] buf = stream1.toByteArray(); // 将图片流以字符串形式存储下来
				// byte[] buf = s.getBytes();
				stream.write(buf);

				FileInputStream fin = new FileInputStream(photoStr);
				int length = fin.available();
				byte[] buffer = new byte[length];
				fin.read(buffer);

				Intent intent2 = new Intent();
				intent2.putExtra("buf", buffer);
				intent2.putExtra("uri", photoStr);
				Log.i("linshi", "size:" + buf.length);
				// 通过调用setResult方法返回结果给前一个activity。
				SettingPhoto.this.setResult(-11, intent2);
				fin.close();
				stream.close();
				// 关闭当前activity
				SettingPhoto.this.finish();
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

	public void onResume() {
		super.onResume();

		/**
		 * 页面起始（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onResume(this);
	}

	public void onPause() {
		super.onPause();

		/**
		 * 页面结束（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onPause(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.lookphoto:
			case R.id.paizhaoshangchuan:
			case R.id.congxiangce:
				
				Button b= (Button) findViewById(v.getId());
				b.setTextColor(this.getResources().getColor(R.color.system_textColor2));
				
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.lookphoto:
			case R.id.paizhaoshangchuan:
			case R.id.congxiangce:
				Button b= (Button) findViewById(v.getId());
				b.setTextColor(this.getResources().getColor(R.color.system_textColor));
				break;
			}
		}
		return false;
	}
}
