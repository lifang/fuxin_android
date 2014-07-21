package com.fuwu.mobileim.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.view.CharacterParser;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class FuXunTools {
	private static CharacterParser characterParser = CharacterParser
			.getInstance();
	private static String[] arr = { "教育培训", "医疗健康", "法律咨询", "金融财经", "生活百科",
			"公益慈善" };
	private static int[] arr_bg = { R.drawable.education_and_training,
			R.drawable.health, R.drawable.legal_consultation,
			R.drawable.financial_finance, R.drawable.encyclopedia_of_life,
			R.drawable.charity };
	private static int[] arr_item = { R.drawable.education_and_training1,
			R.drawable.health1, R.drawable.legal_consultation1,
			R.drawable.financial_finance1, R.drawable.encyclopedia_of_life1,
			R.drawable.charity1 };
	public static int[] image_id = { R.id.info_face0, R.id.info_face1,
			R.id.info_face2, R.id.info_face3, R.id.info_face4, R.id.info_face5 };
	private static Bitmap bm = null;
	protected static ImageLoader imageLoader = ImageLoader.getInstance();
	static DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.moren)
			.showImageForEmptyUri(R.drawable.moren)
			.showImageOnFail(R.drawable.moren).cacheInMemory(true)
			.cacheOnDisk(true).considerExifParams(true)
			.displayer(new RoundedBitmapDisplayer(20)).build();
	private static ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	// 判断应用前台还是后台
	public static boolean isApplicationBroughtToBackground(final Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;

	}

	// 判断手机号合法性
	public static boolean isMobileNO(String mobiles) {
		// ^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$

		Pattern p = Pattern.compile("^[1][34578][0-9]{9}$");
		Matcher m = p.matcher(mobiles);
		Log.i("Max", m.matches() + "");
		return m.matches();
	}

	/**
	 * 16以内的十进制数转换成 四位二进制
	 */
	public static String toNumber(int number) {
		String str = Integer.toBinaryString(number);
		if (number < 2) {
			return "000" + str;
		} else if (number < 4) {
			return "00" + str;
		} else if (number < 8) {
			return "0" + str;
		} else {
			return str;
		}
	}

	/**
	 * 判断 字符串,如："1010"，，对应区间内是否存在"1"
	 */
	public static Boolean isExist(String str, int index1, int index2) {
		Boolean boo = false;
		for (int i = index1; i <= index2; i++) {
			if ((str.charAt(i) + "").equals("1")) {
				boo = true;
			}
		}
		return boo;
	}

	public static void set_bk(final int contactId, final String url,
			final ImageView imageView) {

		final Handler mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case 0:
					Drawable drawable = (Drawable) msg.obj;
					imageView.setImageDrawable(drawable);
					break;
				default:
					break;
				}
			}
		};

		Thread thread = new Thread() {
			public void run() {
				Drawable face_drawable;
				try {
					Log.i("linshi------------", url);
					URL myurl = new URL(url);
					// 获得连接
					HttpURLConnection conn = (HttpURLConnection) myurl
							.openConnection();
					conn.setConnectTimeout(6000);// 设置超时
					conn.setDoInput(true);
					conn.setUseCaches(false);// 不缓存
					conn.connect();
					InputStream is = conn.getInputStream();// 获得图片的数据流
					// bm =decodeSampledBitmapFromStream(is,150,150);

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = false;
					// options.outWidth = 159;
					// options.outHeight = 159;
					options.inSampleSize = 2;
					bm = BitmapFactory.decodeStream(is, null, options);
					Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
					is.close();
					if (bm != null) {
						Log.i("linshi",
								bm.getWidth() + "---2---" + bm.getHeight());
						File f = new File(Urlinterface.head_pic, contactId + "");

						if (f.exists()) {
							f.delete();
						}
						if (!f.getParentFile().exists()) {
							f.getParentFile().mkdirs();
						}
						Log.i("linshi", "----1");
						FileOutputStream out = new FileOutputStream(f);
						Log.i("linshi", "----6");
						bm.compress(Bitmap.CompressFormat.PNG, 90, out);
						out.flush();
						out.close();
						Log.i("linshi", "已经保存");
						Log.i("linshi", "----6");
						Log.i("linshi", "已经保存2");

						face_drawable = new BitmapDrawable(bm);
						Message msg = new Message();// 创建Message 对象
						msg.what = 0;
						msg.obj = face_drawable;
						mHandler.sendMessage(msg);

					}

				} catch (Exception e) {
					Log.i("linshi", "发生异常");
					// Log.i("linshi", url);
				}

			}
		};

		thread.start();

	}

	public static void set_bk_createRoundConerImage(final int contactId,
			final String url, final ImageView imageView) {

		final Handler mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case 0:
					Bitmap bitmap = (Bitmap) msg.obj;
					imageView.setImageDrawable(new BitmapDrawable(
							createRoundConerImage(bitmap)));
					break;
				default:
					break;
				}
			}
		};

		Thread thread = new Thread() {
			public void run() {
				try {
					Log.i("linshi------------", url);
					URL myurl = new URL(url);
					// 获得连接
					HttpURLConnection conn = (HttpURLConnection) myurl
							.openConnection();
					conn.setConnectTimeout(6000);// 设置超时
					conn.setDoInput(true);
					conn.setUseCaches(false);// 不缓存
					conn.connect();
					InputStream is = conn.getInputStream();// 获得图片的数据流
					// bm =decodeSampledBitmapFromStream(is,150,150);

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = false;
					// options.outWidth = 159;
					// options.outHeight = 159;
					options.inSampleSize = 2;
					bm = BitmapFactory.decodeStream(is, null, options);
					Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
					is.close();
					if (bm != null) {
						Log.i("linshi",
								bm.getWidth() + "---2---" + bm.getHeight());
						File f = new File(Urlinterface.head_pic, contactId + "");

						if (f.exists()) {
							f.delete();
						}
						if (!f.getParentFile().exists()) {
							f.getParentFile().mkdirs();
						}
						Log.i("linshi", "----1");
						FileOutputStream out = new FileOutputStream(f);
						Log.i("linshi", "----6");
						bm.compress(Bitmap.CompressFormat.PNG, 90, out);
						out.flush();
						out.close();
						Log.i("linshi", "已经保存");
						Log.i("linshi", "----6");
						Log.i("linshi", "已经保存2");

						Message msg = new Message();// 创建Message 对象
						msg.what = 0;
						msg.obj = bm;
						mHandler.sendMessage(msg);

					}

				} catch (Exception e) {
					Log.i("linshi", "发生异常");
					// Log.i("linshi", url);
				}

			}
		};

		thread.start();

	}

	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			left = 0;
			top = 0;
			right = width;
			bottom = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		paint.setAntiAlias(true);// 设置画笔无锯齿

		canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
		paint.setColor(color);

		// 以下有两种方法画圆,drawRounRect和drawCircle
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);//
		// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
		canvas.drawCircle(roundPx, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
		canvas.drawBitmap(bitmap, src, dst, paint); // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

		return output;
	}

	public static void setBackground(final String url, final ImageView imageView) {
		imageLoader.displayImage(url, imageView, options, animateFirstListener);
	}

	/*
	 * 获得头像并以个人的id 作为文件名，保存到 /fuXun/head_pic/ 中
	 */
	public static void getBitmap(final List<ShortContactPojo> contactsList) {

		Thread thread = new Thread() {
			public void run() {
				try {
					for (int i = 0; i < contactsList.size(); i++) {

						URL myurl = new URL(contactsList.get(i)
								.getUserface_url());
						// 获得连接
						HttpURLConnection conn = (HttpURLConnection) myurl
								.openConnection();
						conn.setConnectTimeout(6000);// 设置超时
						conn.setDoInput(true);
						conn.setUseCaches(false);// 不缓存
						conn.connect();
						InputStream is = conn.getInputStream();// 获得图片的数据流
						// bm =decodeSampledBitmapFromStream(is,150,150);

						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = false;
						options.inSampleSize = 1;
						bm = BitmapFactory.decodeStream(is, null, options);
						Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
						is.close();
						if (bm != null) {
							Log.i("linshi",
									bm.getWidth() + "---2---" + bm.getHeight());
							File f = new File(Urlinterface.head_pic,
									contactsList.get(i).getContactId() + "");

							if (f.exists()) {
								f.delete();
							}
							if (!f.getParentFile().exists()) {
								f.getParentFile().mkdirs();
							}
							Log.i("linshi", "----1");
							FileOutputStream out = new FileOutputStream(f);
							Log.i("linshi", "----6");
							bm.compress(Bitmap.CompressFormat.PNG, 90, out);
							out.flush();
							out.close();

							Log.i("linshi", "已经保存");
						}
					}
					ImageCacheUtil.IMAGE_CACHE.clear();
				} catch (Exception e) {
					Log.i("linshi", "发生异常");
					// Log.i("linshi", url);
				}
			}
		};
		thread.start();
	}

	// 判断sd卡是否可用
	public static boolean isHasSdcard() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	//
	public static void set_img(int id, ImageView iv) {

		File f = new File(Urlinterface.head_pic, id + "");
		if (f.exists()) {
			Log.i("linshi------------", "加载本地图片");
			Drawable dra = new BitmapDrawable(
					BitmapFactory.decodeFile(Urlinterface.head_pic + id));
			iv.setImageDrawable(dra);
		} else {
			iv.setImageResource(R.drawable.moren);
		}

	}

	// 判断网络
	public static boolean isConnect(Context context) {

		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {

				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();

				if (info != null && info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}

	public static String getSortKey(String customName, String name) {

		String sortKey = null;
		if (customName != null && customName.length() > 0) {
			sortKey = findSortKey(customName);
		} else {
			sortKey = findSortKey(name);
		}
		return sortKey;
	}

	/**
	 * 获得首字母
	 */
	public static String findSortKey(String str) {
		if (str.length() > 0) {
			String pinyin = characterParser.getSelling(str);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				return sortString.toUpperCase();
			} else {
				return "#";
			}
		} else {
			return "#";
		}
	}

	public static void getBitmap_url(final String url, final int id) {

		Thread thread = new Thread() {
			public void run() {
				try {

					URL myurl = new URL(url);
					// 获得连接
					HttpURLConnection conn = (HttpURLConnection) myurl
							.openConnection();
					conn.setConnectTimeout(6000);// 设置超时
					conn.setDoInput(true);
					conn.setUseCaches(false);// 不缓存
					conn.connect();
					InputStream is = conn.getInputStream();// 获得图片的数据流
					// bm =decodeSampledBitmapFromStream(is,150,150);

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = false;
					options.inSampleSize = 1;
					bm = BitmapFactory.decodeStream(is, null, options);
					Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
					is.close();
					if (bm != null) {
						Log.i("linshi",
								bm.getWidth() + "---2---" + bm.getHeight());
						File f = new File(Urlinterface.head_pic, id + "");

						if (f.exists()) {
							f.delete();
						}
						if (!f.getParentFile().exists()) {
							f.getParentFile().mkdirs();
						}
						Log.i("linshi", "----1");
						FileOutputStream out = new FileOutputStream(f);
						Log.i("linshi", "----6");
						bm.compress(Bitmap.CompressFormat.PNG, 90, out);
						out.flush();
						out.close();

						Log.i("linshi", "已经保存");
					}
				} catch (Exception e) {
					Log.i("linshi", "发生异常");
					// Log.i("linshi", url);
				}
			}
		};
		thread.start();
	}

	/**
	 * 根据原图添加圆角
	 * 
	 * @param source
	 * @return
	 */
	public static Bitmap createRoundConerImage(Bitmap source) {
		int width = source.getWidth();
		int height = source.getHeight();
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		Bitmap target = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(target);
		RectF rect = new RectF(0, 0, width, height);
		canvas.drawRoundRect(rect, 10f, 10f, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(source, 0, 0, paint);
		return target;
	}

	/**
	 * Get root content view.
	 * 
	 * @param act
	 * @return
	 */
	public static ViewGroup getContentView(Activity act) {
		ViewGroup systemContent = (ViewGroup) act.getWindow().getDecorView()
				.findViewById(android.R.id.content);
		ViewGroup content = null;
		if (systemContent.getChildCount() > 0
				&& systemContent.getChildAt(0) instanceof ViewGroup) {
			content = (ViewGroup) systemContent.getChildAt(0);
		}
		return content;
	}

	// /**
	// * 适用于包含多个组件的 view
	// * */
	// public static void changeFonts(ViewGroup root, Activity act) {
	//
	// Typeface tf = Typeface.createFromAsset(act.getAssets(),
	// "fonts/FZLTHJW.TTF");
	//
	// for (int i = 0; i < root.getChildCount(); i++) {
	// View v = root.getChildAt(i);
	// if (v instanceof TextView) {
	// ((TextView) v).setTypeface(tf);
	// } else if (v instanceof Button) {
	// ((Button) v).setTypeface(tf);
	// } else if (v instanceof EditText) {
	// ((EditText) v).setTypeface(tf);
	// } else if (v instanceof ViewGroup) {
	// changeFonts((ViewGroup) v, act);
	// }
	// }
	// }
	// /**
	// * 适用于包含多个组件的 view
	// * */
	// public static void changeFonts(ViewGroup root, Context act) {
	//
	// Typeface tf = Typeface.createFromAsset(act.getAssets(),
	// "fonts/FZLTHJW.TTF");
	//
	// for (int i = 0; i < root.getChildCount(); i++) {
	// View v = root.getChildAt(i);
	// if (v instanceof TextView) {
	// ((TextView) v).setTypeface(tf);
	// } else if (v instanceof Button) {
	// ((Button) v).setTypeface(tf);
	// } else if (v instanceof EditText) {
	// ((EditText) v).setTypeface(tf);
	// } else if (v instanceof ViewGroup) {
	// changeFonts((ViewGroup) v, act);
	// }
	// }
	// }
	// /**
	// * 适用于单个组件
	// * */
	// public static void changeFonts_one(View v, Activity act) {
	//
	// Typeface tf = Typeface.createFromAsset(act.getAssets(),
	// "fonts/FZLTHJW.TTF");
	//
	// if (v instanceof TextView) {
	// ((TextView) v).setTypeface(tf);
	// } else if (v instanceof Button) {
	// ((Button) v).setTypeface(tf);
	// } else if (v instanceof EditText) {
	// ((EditText) v).setTypeface(tf);
	// } else if (v instanceof ViewGroup) {
	// changeFonts_one((ViewGroup) v, act);
	// }
	// }
	// /**
	// * 适用于单个组件
	// * */
	// public static void changeFonts_one(View v, Context act) {
	//
	// Typeface tf = Typeface.createFromAsset(act.getAssets(),
	// "fonts/FZLTHJW.TTF");
	//
	// if (v instanceof TextView) {
	// ((TextView) v).setTypeface(tf);
	// } else if (v instanceof Button) {
	// ((Button) v).setTypeface(tf);
	// } else if (v instanceof EditText) {
	// ((EditText) v).setTypeface(tf);
	// } else if (v instanceof ViewGroup) {
	// changeFonts_one((ViewGroup) v, act);
	// }
	// }

	public static int getNumber(String str) {
		int index = -1;

		for (int i = 0; i < arr.length; i++) {
			if (str.equals(arr[i])) {
				index = i;
			}
		}
		return index;
	}

	// 设置个人认证背景
	public static void setIdentity_bg(View view, String str) {
		if (str.indexOf("、") != -1) {
			str = str.substring(0, str.indexOf("、"));
		} else {
			str = str.substring(0, str.length());
		}

		view.setBackgroundResource(arr_bg[getNumber(str)]);
	}

	// 设置个人认证行业图标
	public static void setItem_bg(ArrayList<View> imageviewList, String str) {
		String[] strarr;
		strarr = str.split("、");
		for (int i = 0; i < strarr.length; i++) {
			for (int j = 0; j < arr.length; j++) {
				if (strarr[i].equals(arr[j])) {
					imageviewList.get(i).setBackgroundResource(arr_item[i]);
				}
			}
		}

	}
}
