package com.fuwu.mobileim.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.trinea.android.common.service.impl.ImageCache;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.ChangeContactDetailRequest;
import com.fuwu.mobileim.model.Models.ChangeContactDetailResponse;
import com.fuwu.mobileim.model.Models.Contact;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.ImageUtil;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.MyDialog;

/**
 * @作者 马龙
 * @时间 2014-5-16 上午10:48:52
 */
public class MessageListViewAdapter extends BaseAdapter {

	private int user_id;
	private String token;
	public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");
	private Context mContext;
	private Resources res;
	private LayoutInflater mInflater;
	private List<MessagePojo> list = new ArrayList<MessagePojo>();
	private ContactPojo cp;
	private DBManager db;
	private TextView rem;
	private EditText remInfo;
	private Button ok;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if (!db.isOpen()) {
					db = new DBManager(mContext);
				}
				String str = remInfo.getText().toString();
				cp.setCustomName(str);
				db.updateContactRem(user_id, cp.getContactId(), str);
				remInfo.setText(str);
				rem.setText("备注:" + str);
				ok.setText("编辑");
				Toast.makeText(mContext, "修改备注成功!", 0).show();
				break;
			case 2:
				Toast.makeText(mContext, "修改备注失败!", 0).show();
				break;
			}
		}
	};

	public MessageListViewAdapter(Resources res, Context mContext,
			List<MessagePojo> list, ContactPojo cp, int user_id, String token) {
		super();
		this.mContext = mContext;
		this.res = res;
		this.mInflater = LayoutInflater.from(mContext);
		this.list = list;
		this.cp = cp;
		this.user_id = user_id;
		this.token = token;
		db = new DBManager(mContext);
	}

	public void updMessage(MessagePojo cp) {
		list.add(cp);
		notifyDataSetChanged();
	}

	public void updMessageList(List<MessagePojo> list) {
		this.list = list;
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
		final MessagePojo mp = list.get(position);
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
			holder.sendImg = (ImageView) convertView
					.findViewById(R.id.chat_img);
			convertView.setTag(R.drawable.ic_launcher + position);
		} else {
			holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher
					+ position);
		}

		holder.img.setImageBitmap(toRoundBitmap(BitmapFactory.decodeResource(
				res, R.drawable.headpic)));
		if (mp.getSendTime() != null && !mp.getSendTime().equals("")) {
			holder.time.setText(TimeUtil.getChatTime(mp.getSendTime()));
			holder.time.setVisibility(View.VISIBLE);
		}
		if (mp.getIsComMeg() == 0) {
			FuXunTools.set_img(cp.getContactId(), holder.img);
			holder.img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showLoginDialog();
				}
			});
		} else {
			FuXunTools.set_img(user_id, holder.img);
		}
		if (mp.getMsgType() == 1) {
			holder.mes.setText(convertNormalStringToSpannableString(mp
					.getContent()));
		} else {
			holder.sendImg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showBigImage("/sdcard/fuXun/" + mp.getContent());
				}
			});
			holder.mes.setVisibility(View.GONE);
			holder.sendImg.setVisibility(View.VISIBLE);
			holder.sendImg.setImageBitmap(ImageUtil.createImageThumbnail(
					"/sdcard/fuXun/" + mp.getContent(), 720));
			// ImageCacheUtil.IMAGE_CACHE.get("/sdcard/fuXun/" +
			// mp.getContent(),
			// holder.sendImg);
		}

		return convertView;
	}

	public final static class ViewHolder {
		public TextView time;
		public TextView mes;
		public TextView order;
		public ImageView img;
		public ImageView sendImg;
	}

	public void showBigImage(String path) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View imgEntryView = inflater.inflate(R.layout.chat_dialog, null); // 加载自定义的布局文件
		final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
		ImageView img = (ImageView) imgEntryView.findViewById(R.id.large_image);
		ImageCacheUtil.IMAGE_CACHE.get(path, img);
		dialog.setView(imgEntryView); // 自定义dialog
		dialog.show();
		// 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
		imgEntryView.setOnClickListener(new OnClickListener() {
			public void onClick(View paramView) {
				dialog.cancel();
			}
		});
	}

	private void showLoginDialog() {
		View view = mInflater.inflate(R.layout.chat_info, null);
		TextView name = (TextView) view.findViewById(R.id.info_name);
		rem = (TextView) view.findViewById(R.id.info_rem);
		remInfo = (EditText) view.findViewById(R.id.info_remInfo);
		TextView lisence = (TextView) view.findViewById(R.id.info_lisence);
		TextView sign = (TextView) view.findViewById(R.id.info_sign);
		ImageView img = (ImageView) view.findViewById(R.id.info_img);
		ImageView img_gou = (ImageView) view.findViewById(R.id.info_gouIcon);
		ImageView img_yue = (ImageView) view.findViewById(R.id.info_yueIcon);
		ok = (Button) view.findViewById(R.id.info_ok);
		// 设置头像
		FuXunTools.set_img(cp.getContactId(), img);
		String str = FuXunTools.toNumber(cp.getSource());
		if (FuXunTools.isExist(str, 0, 1)) {
			img_gou.setVisibility(View.VISIBLE);
		} else {
			img_gou.setVisibility(View.GONE);
		}
		if (FuXunTools.isExist(str, 2, 3)) {
			img_yue.setVisibility(View.VISIBLE);
		} else {
			img_yue.setVisibility(View.GONE);
		}
		name.setText("" + cp.getName());
		rem.setText("备注:" + cp.getCustomName());
		remInfo.setText("" + cp.getCustomName());
		lisence.setText("" + cp.getLisence());
		sign.setText("" + cp.getIndividualResume());
		remInfo.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				ok.setText("确定");
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = remInfo.getText().toString();
				if (str != null && !str.equals("")) {
					new UpdateContactRem().start();
				}
			}
		});
		final MyDialog builder = new MyDialog(mContext, 1, view,
				R.style.mydialog);
		builder.show();
	}

	class UpdateContactRem extends Thread {
		public void run() {
			try {
				ChangeContactDetailRequest.Builder builder = ChangeContactDetailRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(token);
				Contact.Builder cb = Contact.newBuilder();
				cb.setContactId(cp.getContactId());
				cb.setCustomName(cp.getCustomName());
				builder.setContact(cb);
				ChangeContactDetailRequest response = builder.build();
				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ContactDetail, "PUT");
				if (by != null && by.length > 0) {
					ChangeContactDetailResponse res = ChangeContactDetailResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(1);
					} else {
						handler.sendEmptyMessage(2);
					}
				} else {
					handler.sendEmptyMessage(2);
				}
			} catch (Exception e) {
			}
		}
	}

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

		canvas.drawCircle(roundPx, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
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
