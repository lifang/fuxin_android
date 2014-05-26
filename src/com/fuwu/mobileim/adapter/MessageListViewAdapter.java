package com.fuwu.mobileim.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.TimeUtil;

/**
 * @作者 马龙
 * @时间 2014-5-16 上午10:48:52
 */
public class MessageListViewAdapter extends BaseAdapter {

	public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");
	private Context mContext;
	private Resources res;
	private LayoutInflater mInflater;
	private List<MessagePojo> list = new ArrayList<MessagePojo>();

	public MessageListViewAdapter(Resources res, Context context,
			List<MessagePojo> list) {
		super();
		this.mContext = context;
		this.res = res;
		this.mInflater = LayoutInflater.from(context);
		this.list = list;
	}

	public void updMessage(MessagePojo cp) {
		list.add(cp);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MessagePojo mp = list.get(position);
		ViewHolder holder = null;
		if (convertView == null
				|| convertView.getTag(R.drawable.ic_launcher + position) == null) {
			holder = new ViewHolder();
			if (mp.getIsComMeg() == 0) {
				convertView = mInflater.inflate(R.layout.chat_item_left, null);
			} else {
				convertView = mInflater.inflate(R.layout.chat_item_right, null);
			}
			holder.time = (TextView) convertView
					.findViewById(R.id.chat_datetime);
			holder.mes = (TextView) convertView
					.findViewById(R.id.chat_textView2);
			holder.img = (ImageView) convertView.findViewById(R.id.chat_icon);
			convertView.setTag(R.drawable.ic_launcher + position);

		} else {
			holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher
					+ position);
		}

		holder.img.setImageBitmap(toRoundBitmap(BitmapFactory.decodeResource(
				res, R.drawable.headpic)));
		// if (mp.getIsTimeShow() == 0) {
		if (mp.getSendTime() != null && !mp.getSendTime().equals("")) {
			holder.time.setText(TimeUtil.getChatTime(mp.getSendTime()));
			holder.time.setVisibility(View.VISIBLE);
		}
		// }

		holder.mes
				.setText(convertNormalStringToSpannableString(mp.getContent()));
		return convertView;
	}

	public final class ViewHolder {
		public TextView time;
		public TextView mes;
		public TextView order;
		public ImageView img;
	}

	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public Bitmap toRoundBitmap(Bitmap bitmap) {
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

	private CharSequence convertNormalStringToSpannableString(String message) {
		String hackTxt;
		if (message.startsWith("[") && message.endsWith("]")) {
			hackTxt = message + " ";
		} else {
			hackTxt = message;
		}
		SpannableString value = SpannableString.valueOf(hackTxt);

		Matcher localMatcher = EMOTION_URL.matcher(value);
		while (localMatcher.find()) {
			String str2 = localMatcher.group(0);
			int k = localMatcher.start();
			int m = localMatcher.end();
			// k = str2.lastIndexOf("[");
			// Log.i("way", "str2.length = "+str2.length()+", k = " + k);
			// str2 = str2.substring(k, m);
			if (m - k < 8) {
				if (FxApplication.getInstance().getFaceMap().containsKey(str2)) {
					int face = FxApplication.getInstance().getFaceMap()
							.get(str2);
					Bitmap bitmap = BitmapFactory.decodeResource(res, face);
					if (bitmap != null) {
						ImageSpan localImageSpan = new ImageSpan(mContext,
								bitmap, ImageSpan.ALIGN_BASELINE);
						value.setSpan(localImageSpan, k, m,
								Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}
		}
		return value;
	}

}
