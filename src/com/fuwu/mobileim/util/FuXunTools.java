package com.fuwu.mobileim.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.License;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.view.MyDialog;

public class FuXunTools {
	static int index = 0;
	static int index2 = 0;
	static ArrayList<ImageView> imageviewList0;
	static List<License> licenses0;
	public static int[] image_id = { R.id.info_face0, R.id.info_face1,
			R.id.info_face2, R.id.info_face3, R.id.info_face4, R.id.info_face5 };
	private static Bitmap bm = null;

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
					options.inSampleSize = 1;
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
					// initTrustSSL();
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
					options.inSampleSize = 1;
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
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = false;
						options.inSampleSize = 1;
						bm = BitmapFactory.decodeStream(is, null, options);
						Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
						is.close();
						if (bm != null) {
							File f = new File(Urlinterface.head_pic,
									contactsList.get(i).getContactId() + "");
							if (f.exists()) {
								f.delete();
							}
							if (!f.getParentFile().exists()) {
								f.getParentFile().mkdirs();
							}
							FileOutputStream out = new FileOutputStream(f);
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
		// if (customName != null && customName.length() > 0) {
		// sortKey = findSortKey(customName);
		// } else {
		// sortKey = findSortKey(name);
		// }
		return sortKey;
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

	// // 设置个人认证背景
	// public static void setIdentity_bg(View view, String str) {
	// if (str.indexOf("、") != -1) {
	// str = str.substring(0, str.indexOf("、"));
	// } else {
	// str = str.substring(0, str.length());
	// }
	// view.setBackgroundResource(arr_bg[getNumber(str)]);
	// }

	final static Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				index = index + 1;

				String name = licenses0.get(index - 1).getName() + ".png";
				File f = new File(Urlinterface.head_pic, name);
				if (f.exists()) {
					imageviewList0.get(index - 1).setBackgroundDrawable(
							new BitmapDrawable(FuXunTools
									.createRoundConerImage(BitmapFactory
											.decodeFile(Urlinterface.head_pic
													+ name))));

				}

				break;
			default:
				break;
			}
		}
	};
	static String url;
	static String name;

	// 设置个人认证行业图标
	public static void setItem_bg(ArrayList<ImageView> imageviewList,
			List<License> licenses) {
		index = 0;
		index2 = licenses.size();
		imageviewList0 = imageviewList;
		licenses0 = licenses;
		getUserBitmap(licenses);

	}

	private static void getUserBitmap(List<License> licenses) {
		ExecutorService singleThreadExecutor = Executors
				.newSingleThreadExecutor();
		for (int i = 0; i < licenses.size(); i++) {
			final String url = licenses.get(i).getIconUrl();
			Log.i("FuWu", "url---" + licenses.get(i).getIconUrl());
			final String name = licenses.get(i).getName() + ".png";
			File f = new File(Urlinterface.head_pic, name + "");

			if (f.exists()) {
				mHandler.sendEmptyMessage(1);
			} else {

				singleThreadExecutor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							URL myurl = new URL(url);
							initTrustSSL();
							// 获得连接
							HttpURLConnection conn = (HttpURLConnection) myurl
									.openConnection();
							conn.setConnectTimeout(6000);// 设置超时
							conn.setDoInput(true);
							conn.setUseCaches(false);// 不缓存
							conn.connect();
							InputStream is = conn.getInputStream();// 获得图片的数据流

							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inJustDecodeBounds = false;
							options.inSampleSize = 1;
							bm = BitmapFactory.decodeStream(is, null, options);
							Log.i("linshi",
									bm.getWidth() + "---" + bm.getHeight());
							is.close();
							if (bm != null) {
								Log.i("linshi",
										bm.getWidth() + "---2---"
												+ bm.getHeight());
								File f = new File(Urlinterface.head_pic, name
										+ "");

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
							mHandler.sendEmptyMessage(1);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							mHandler.sendEmptyMessage(1);
						}
					}
				});
			}
		}
	}

	public static void set_view_bk(final int provider, final String name,
			final String url, final View view) {

		final Handler mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case 0:
					Drawable drawable = (Drawable) msg.obj;
					view.setBackgroundDrawable(drawable);
					break;
				case 1:// 下载失败
					if (provider == 1) {
						view.setBackgroundResource(R.drawable.unauthorized_bg);
					}
					if (provider == 0) {
						view.setBackgroundResource(R.drawable.fuke_bg);
					}

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
					initTrustSSL();
					// 获得连接
					HttpURLConnection conn = (HttpURLConnection) myurl
							.openConnection();
					conn.setConnectTimeout(6000);// 设置超时
					conn.setDoInput(true);
					conn.setUseCaches(false);// 不缓存
					conn.connect();
					InputStream is = conn.getInputStream();// 获得图片的数据流

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = false;
					options.inSampleSize = 1;
					bm = BitmapFactory.decodeStream(is, null, options);
					Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
					is.close();
					if (bm != null) {
						Log.i("linshi",
								bm.getWidth() + "---2---" + bm.getHeight());
						File f = new File(Urlinterface.head_pic, name);

						if (f.exists()) {
							f.delete();
						}
						if (!f.getParentFile().exists()) {
							f.getParentFile().mkdirs();
						}
						f.createNewFile();
						FileOutputStream out = new FileOutputStream(f);
						bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
						out.flush();
						out.close();

						face_drawable = new BitmapDrawable(bm);
						Message msg = new Message();// 创建Message 对象
						msg.what = 0;
						msg.obj = face_drawable;
						mHandler.sendMessage(msg);
					} else {
						mHandler.sendEmptyMessage(1);
					}

				} catch (Exception e) {
					Log.i("linshi", "发生异常");
					mHandler.sendEmptyMessage(1);
				}

			}
		};

		thread.start();

	}

	public static void initTrustSSL() {
		try {
			SSLContext sslCtx = SSLContext.getInstance("TLS");
			sslCtx.init(null, new TrustManager[] { new X509TrustManager() {
				// do nothing, let the check pass.
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} }, new SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx
					.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
		} catch (Exception E) {
		}
	}

	/**
	 * 获得本地存储的 个人信息
	 */
	public static ProfilePojo getProfilePojo(SharedPreferences preferences,
			FxApplication fxApplication) {
		ProfilePojo profilePojo;
		int profile_userid = preferences.getInt("profile_userid", -1);
		String name = preferences.getString("profile_name", "");// 名称
		String nickName = preferences.getString("profile_nickName", "");// 昵称
		int gender = preferences.getInt("profile_gender", -1);// 性别
		String tileUrl = preferences.getString("profile_tileUrl", "");// 头像
		Boolean isProvider = preferences
				.getBoolean("profile_isProvider", false);//
		String lisence = preferences.getString("profile_lisence", "");// 行业认证
		String mobile = preferences.getString("profile_mobile", "");// 手机号码
		String email = preferences.getString("profile_email", "");// 邮箱
		String birthday = preferences.getString("profile_birthday", "");// 生日
		Boolean isAuthentication = preferences.getBoolean(
				"profile_isAuthentication", false);// 实名认证
		String fuzhi = preferences.getString("profile_fuZhi", "");// 福指
		String location = preferences.getString("profile_location", "");// 所在地
		String description = preferences.getString("profile_description", "");// 福师简介
		String backgroundUrl = preferences.getString("backgroundUrl", "");// 背景
		List<License> licenses = fxApplication.getLicenses();
		profilePojo = new ProfilePojo(profile_userid, name, nickName, gender,
				tileUrl, isProvider, lisence, mobile, email, birthday,
				isAuthentication, fuzhi, location, description, licenses,
				backgroundUrl);
		return profilePojo;
	}

	public static void putProfile(ProfilePojo pro,
			SharedPreferences preferences, FxApplication fxApplication) {
		Editor editor = preferences.edit();
		editor.putInt("profile_userid", pro.getUserId());
		editor.putString("profile_name", pro.getName());
		editor.putString("profile_nickName", pro.getNickName());
		editor.putInt("profile_gender", pro.getGender());
		editor.putString("profile_tileUrl", pro.getTileUrl());
		editor.putBoolean("profile_isProvider", pro.getIsProvider());
		editor.putString("profile_lisence", pro.getLisence());
		editor.putString("profile_mobile", pro.getMobile());
		editor.putString("profile_email", pro.getEmail());
		editor.putString("profile_birthday", pro.getBirthday());
		editor.putBoolean("profile_isAuthentication", pro.getIsAuthentication());
		editor.putString("profile_fuZhi", pro.getFuZhi());
		editor.putString("profile_location", pro.getLocation());
		editor.putString("profile_description", pro.getDescription());
		editor.putString("backgroundUrl", pro.getBackgroundUrl());
		editor.putString("profile_user", pro.getUserId() + "");// 用于判断本地是否有当前用户的信息
		editor.commit();
		fxApplication.setLicenses(pro.getLicenses());
	}

	public static void initdate(SharedPreferences preferences,
			FxApplication fxApplication) {
		Editor editor = preferences.edit();
		editor.putInt("exit_user_id", preferences.getInt("user_id", 0))
				.commit();
		editor.putString("exit_Token", preferences.getString("Token", "null"))
				.commit();
		editor.putString("exit_clientid", preferences.getString("clientid", ""))
				.commit();
		editor.putString("Token", "null").commit();
		editor.putString("pwd", "").commit();
		editor.putString("clientid", "").commit();
		editor.putString("profile_user", "").commit();
		editor.putString("NewVersionUrl", "").commit();
		editor.putString("contactTimeStamp", "").commit();
		editor.putString("hasnew", "");
		// 用于判断本地是否有当前用户的信息
		editor.commit();
		fxApplication.initData();
	}

	public static String del_tag(String str) {// 去除HTML标签
		Pattern p_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(str);
		String content = m_html.replaceAll(""); // 过滤html标签
		return content;
	}

	public static MyDialog showLoading(LayoutInflater li, Context context,
			String str) {
		View view = li.inflate(R.layout.loading_main, null);
		ImageView iv = (ImageView) view.findViewById(R.id.loading_iv);
		final AnimationDrawable loadingDw = ((AnimationDrawable) iv
				.getBackground());
		// loadingDw.start();
		iv.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				loadingDw.start();
				return true; // 必须要有这个true返回
			}
		});
		TextView tv = (TextView) view.findViewById(R.id.loading_tv);
		tv.setText(str);
		MyDialog builder = new MyDialog(context, 3, view, R.style.mydialog);
		builder.setCanceledOnTouchOutside(false);
		builder.show();
		return builder;
	}
}
