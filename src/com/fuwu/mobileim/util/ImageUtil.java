package com.fuwu.mobileim.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-5 下午6:20:09
 */
public class ImageUtil {

	public static Bitmap createImageThumbnail(String filePath, int width) {
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);

		int w = opts.outWidth;
		int h = opts.outHeight;
		if (opts.outWidth > width / 2) {
			w = width / 2;
		}
		if (opts.outHeight > width / 3) {
			h = width / 3;
		}

		opts.inSampleSize = computeSampleSize(opts, -1, w * h);
		opts.inJustDecodeBounds = false;

		try {
			bitmap = BitmapFactory.decodeFile(filePath, opts);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return bitmap;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static void saveBitmap(String bitName, String jpgType, Bitmap mBitmap) {
		try {
			File f = new File("/sdcard/FuXun/" + bitName + "." + jpgType);
			f.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			if (fOut != null) {
				mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
				fOut.flush();
				fOut.close();
			}
		} catch (IOException e) {
		}
	}

	public static Bitmap getLoacalBitmap(String url) {
		try {
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * convert Bitmap to byte array
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] bitmapToByte(Bitmap b) {
		if (b == null) {
			return null;
		}

		ByteArrayOutputStream o = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.PNG, 100, o);
		return o.toByteArray();
	}

	/**
	 * convert byte array to Bitmap
	 * 
	 * @param b
	 * @return
	 */
	public static Bitmap byteToBitmap(byte[] b) {
		return (b == null || b.length == 0) ? null : BitmapFactory
				.decodeByteArray(b, 0, b.length);
	}

	/**
	 * convert Drawable to Bitmap
	 * 
	 * @param d
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable d) {
		return d == null ? null : ((BitmapDrawable) d).getBitmap();
	}

	/**
	 * convert Bitmap to Drawable
	 * 
	 * @param b
	 * @return
	 */
	public static Drawable bitmapToDrawable(Bitmap b) {
		return b == null ? null : new BitmapDrawable(b);
	}

	/**
	 * convert Drawable to byte array
	 * 
	 * @param d
	 * @return
	 */
	public static byte[] drawableToByte(Drawable d) {
		return bitmapToByte(drawableToBitmap(d));
	}

	/**
	 * convert byte array to Drawable
	 * 
	 * @param b
	 * @return
	 */
	public static Drawable byteToDrawable(byte[] b) {
		return bitmapToDrawable(byteToBitmap(b));
	}

	/**
	 * get input stream from network by imageurl, you need to close inputStream
	 * yourself
	 * 
	 * @param imageUrl
	 * @param readTimeOutMillis
	 * @return
	 * @see ImageUtils#getInputStreamFromUrl(String, int, boolean)
	 */
	public static InputStream getInputStreamFromUrl(String imageUrl,
			int readTimeOutMillis) {
		return getInputStreamFromUrl(imageUrl, readTimeOutMillis, null);
	}

	/**
	 * get input stream from network by imageurl, you need to close inputStream
	 * yourself
	 * 
	 * @param imageUrl
	 * @param readTimeOutMillis
	 *            read time out, if less than 0, not set, in mills
	 * @param requestProperties
	 *            http request properties
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromUrl(String imageUrl,
			int readTimeOutMillis, Map<String, String> requestProperties) {
		InputStream stream = null;
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// HttpUtils.setURLConnection(requestProperties, con);
			if (readTimeOutMillis > 0) {
				con.setReadTimeout(readTimeOutMillis);
			}
			stream = con.getInputStream();
		} catch (MalformedURLException e) {
			closeInputStream(stream);
			// throw new RuntimeException("MalformedURLException occurred. ",
			// e);
		} catch (IOException e) {
			closeInputStream(stream);
			// throw new RuntimeException("IOException occurred. ", e);
		}
		return stream;
	}

	/**
	 * get drawable by imageUrl
	 * 
	 * @param imageUrl
	 * @param readTimeOutMillis
	 * @return
	 * @see ImageUtils#getDrawableFromUrl(String, int, boolean)
	 */
	public static Drawable getDrawableFromUrl(String imageUrl,
			int readTimeOutMillis) {
		return getDrawableFromUrl(imageUrl, readTimeOutMillis, null);
	}

	/**
	 * get drawable by imageUrl
	 * 
	 * @param imageUrl
	 * @param readTimeOutMillis
	 *            read time out, if less than 0, not set, in mills
	 * @param requestProperties
	 *            http request properties
	 * @return
	 */
	public static Drawable getDrawableFromUrl(String imageUrl,
			int readTimeOutMillis, Map<String, String> requestProperties) {
		InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOutMillis,
				requestProperties);
		Drawable d = Drawable.createFromStream(stream, "src");
		closeInputStream(stream);
		return d;
	}

	/**
	 * get Bitmap by imageUrl
	 * 
	 * @param imageUrl
	 * @param readTimeOut
	 * @return
	 * @see ImageUtils#getBitmapFromUrl(String, int, boolean)
	 */
	public static Bitmap getBitmapFromUrl(String imageUrl, int readTimeOut) {
		return getBitmapFromUrl(imageUrl, readTimeOut, null);
	}

	/**
	 * get Bitmap by imageUrl
	 * 
	 * @param imageUrl
	 * @param requestProperties
	 *            http request properties
	 * @return
	 */
	public static Bitmap getBitmapFromUrl(String imageUrl, int readTimeOut,
			Map<String, String> requestProperties) {
		InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOut,
				requestProperties);
		Bitmap b = BitmapFactory.decodeStream(stream);
		closeInputStream(stream);
		return b;
	}

	/**
	 * scale image
	 * 
	 * @param org
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
		return scaleImage(org, (float) newWidth / org.getWidth(),
				(float) newHeight / org.getHeight());
	}

	/**
	 * scale image
	 * 
	 * @param org
	 * @param scaleWidth
	 *            sacle of width
	 * @param scaleHeight
	 *            scale of height
	 * @return
	 */
	public static Bitmap scaleImage(Bitmap org, float scaleWidth,
			float scaleHeight) {
		if (org == null) {
			return null;
		}

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(),
				matrix, true);
	}

	/**
	 * close inputStream
	 * 
	 * @param s
	 */
	private static void closeInputStream(InputStream s) {
		if (s == null) {
			return;
		}

		try {
			s.close();
		} catch (IOException e) {
			throw new RuntimeException("IOException occurred. ", e);
		}
	}

}
