package com.fuwu.mobileim.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.activity.ContactInfoActivity;
import com.fuwu.mobileim.activity.ZoomImageActivity;
import com.fuwu.mobileim.model.Models.ChangeContactDetailRequest;
import com.fuwu.mobileim.model.Models.ChangeContactDetailResponse;
import com.fuwu.mobileim.model.Models.Contact;
import com.fuwu.mobileim.model.Models.ContactDetailRequest;
import com.fuwu.mobileim.model.Models.ContactDetailResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.util.ContactCache;
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
	public String time_sign; //  时间标志
	public String customName;
	private int user_id;
	private String token;
	public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");
	private Context mContext;
	private Resources res;
	private LayoutInflater mInflater;
	private List<MessagePojo> list = new ArrayList<MessagePojo>();
	private ShortContactPojo cp;
	private ContactPojo contactDetail;
	private DBManager db;
	private TextView rem;
	private EditText remInfo;
	private Button ok;
	private File f;
	private ProgressDialog pd;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				cp.setCustomName(customName);
				db.updateContactRem(user_id, cp.getContactId(), customName);
				remInfo.setText(customName);
				rem.setText("备注:" + customName);
				ContactCache.cp.setCustomName(customName);
				Toast.makeText(mContext, "修改备注成功!", 0).show();
				break;
			case 2:
				Toast.makeText(mContext, "修改备注失败!", 0).show();
				break;
			case 3:
				pd.dismiss();
				showContactDialog();
				break;
			case 4:
				pd.dismiss();
				Toast.makeText(mContext, "获取联系人详细信息失败!", 0).show();
				break;
			case 8:
				Toast.makeText(mContext, "网络异常", 0).show();
				break;
			}
		}
	};

	public MessageListViewAdapter(Resources res, Context mContext,
			List<MessagePojo> list, ShortContactPojo cp, int user_id,
			String token) {
		super();
		this.mContext = mContext;
		this.res = res;
		this.mInflater = LayoutInflater.from(mContext);
		this.list = list;
		this.cp = cp;
		this.user_id = user_id;
		this.token = token;
		db = new DBManager(mContext);
		pd = new ProgressDialog(mContext);
		pd.setMessage("正在加载详细信息...");
	}

	public void updMessage(MessagePojo cp) {
		list.add(cp);
		notifyDataSetChanged();
	}

	public void updMessageList(List<MessagePojo> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public void closeDB() {
		if (db != null) {
			db.closeDB();
		}
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
		if (position==0) {
			time_sign = "";
		}
		
		ViewHolder holder = null;
		if (convertView == null
				|| convertView.getTag(R.drawable.ic_launcher + position) == null) {
			holder = new ViewHolder();
			if (mp.getIsComMeg() == 0) {  //  别人发的消息
				convertView = mInflater.inflate(R.layout.chat_item_left, null);
			} else {//  自己发的消息
				convertView = mInflater.inflate(R.layout.chat_item_right, null);
				holder.load = (ProgressBar) convertView
						.findViewById(R.id.loadingcircle);
				holder.sendFail = (ImageView) convertView
						.findViewById(R.id.sendfail);
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

//		if (mp.getSendTime() != null && !mp.getSendTime().equals("")) {
//			holder.time.setText(TimeUtil.getChatTime(mp.getSendTime()));
//			holder.time.setVisibility(View.VISIBLE);
//		}
		holder.time.setText(TimeUtil.getChatTime(mp.getSendTime()));
		if (!TimeUtil.isFiveMin(time_sign, mp.getSendTime())) {
			holder.time.setVisibility(View.GONE);
			
		}else {
			time_sign = mp.getSendTime();	
		}
		if (mp.getIsComMeg() == 0) {
			f = new File(Urlinterface.head_pic, cp.getContactId() + "");
			if (f.exists()) {
				holder.img.setTag(Urlinterface.head_pic + cp.getContactId());
				if (!ImageCacheUtil.IMAGE_CACHE.get(
						Urlinterface.head_pic + cp.getContactId(), holder.img)) {
					holder.img.setImageDrawable(null);
				}
			} else {
				holder.img.setImageResource(R.drawable.moren);
			}

			// ImageCacheUtil.IMAGE_CACHE.get(
			// Urlinterface.head_pic + cp.getContactId(), holder.img);
			holder.img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.setClass(mContext, ContactInfoActivity.class);
					mContext.startActivity(i);
				}
			});
		} else {
			if (mp.getStatus() == 0) {
				holder.load.setVisibility(View.GONE);
			} else if (mp.getStatus() == 2) {
				holder.load.setVisibility(View.GONE);
				holder.sendFail.setVisibility(View.VISIBLE);
				holder.sendFail.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						v.setVisibility(View.GONE);
					}
				});
			}

			f = new File(Urlinterface.head_pic, user_id + "");
			if (f.exists()) {
				holder.img.setTag(Urlinterface.head_pic + user_id);
				if (!ImageCacheUtil.IMAGE_CACHE.get(
						Urlinterface.head_pic + user_id, holder.img)) {
					holder.img.setImageDrawable(null);
				}
			} else {
				holder.img.setImageResource(R.drawable.moren);
			}
			// ImageCacheUtil.IMAGE_CACHE.get(Urlinterface.head_pic + user_id,
			// holder.img);
		}
		if (mp.getMsgType() == 1) {
			holder.mes.setText(convertNormalStringToSpannableString(mp
					.getContent()));
		} else {
			holder.sendImg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(mContext, ZoomImageActivity.class);
					intent.putExtra("image_path",
							Urlinterface.SDCARD + mp.getContent());
					mContext.startActivity(intent);
				}
			});
			holder.mes.setVisibility(View.GONE);
			holder.sendImg.setVisibility(View.VISIBLE);
			holder.sendImg.setImageBitmap(ImageUtil.createImageThumbnail(
					Urlinterface.SDCARD + mp.getContent(), 720));
		}
		return convertView;
	}

	public final static class ViewHolder {
		public TextView time;
		public TextView mes;
		public TextView order;
		public ImageView img;
		public ImageView sendImg;
		public ProgressBar load;
		public ImageView sendFail;
	}

	public void showContactDialog() {
		View view = mInflater.inflate(R.layout.chat_info, null);
		TextView name = (TextView) view.findViewById(R.id.info_name);
		rem = (TextView) view.findViewById(R.id.info_rem);
		remInfo = (EditText) view.findViewById(R.id.info_remInfo);
		View fzLine = view.findViewById(R.id.info_fzLine);
		View rzLine = view.findViewById(R.id.info_rzLine);
		LinearLayout fz = (LinearLayout) view.findViewById(R.id.info_fz);
		LinearLayout rz = (LinearLayout) view.findViewById(R.id.info_rz);
		LinearLayout jj = (LinearLayout) view.findViewById(R.id.info_jj);
		TextView lisence = (TextView) view.findViewById(R.id.info_lisence);
		TextView sign = (TextView) view.findViewById(R.id.info_sign);
		TextView fuzhi = (TextView) view.findViewById(R.id.info_fuzhi);
		ImageView img = (ImageView) view.findViewById(R.id.info_img);
		ImageView img_gou = (ImageView) view.findViewById(R.id.info_gouIcon);
		ImageView img_yue = (ImageView) view.findViewById(R.id.info_yueIcon);
		ImageView sexView = (ImageView) view.findViewById(R.id.info_sex);
		ok = (Button) view.findViewById(R.id.info_ok);
		ImageCacheUtil.IMAGE_CACHE.get(
				Urlinterface.head_pic + contactDetail.getContactId(), img);
		String str = FuXunTools.toNumber(contactDetail.getSource());
		if (FuXunTools.isExist(str, 0, 1)) {
			img_yue.setVisibility(View.VISIBLE);
		} else {
			img_yue.setVisibility(View.GONE);
		}
		if (FuXunTools.isExist(str, 2, 3)) {
			img_gou.setVisibility(View.VISIBLE);
		} else {
			img_gou.setVisibility(View.GONE);
		}
		if (contactDetail.getIsProvider() == 0) {
			fz.setVisibility(View.GONE);
			rz.setVisibility(View.GONE);
			jj.setVisibility(View.GONE);
			fzLine.setVisibility(View.GONE);
			rzLine.setVisibility(View.GONE);
		}
		int sex = cp.getSex();
		if (sex == 0) {// 男
			sexView.setImageResource(R.drawable.nan);
		} else if (sex == 1) {// 女
			sexView.setImageResource(R.drawable.nv);
		} else {
			sexView.setVisibility(View.GONE);
		}

		name.setText("" + contactDetail.getName());
		rem.setText("备注:" + contactDetail.getCustomName());
		remInfo.setText("" + contactDetail.getCustomName());
		lisence.setText("" + contactDetail.getLisence());
		sign.setText("" + contactDetail.getIndividualResume());
		fuzhi.setText("" + contactDetail.getFuzhi());
		remInfo.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				ok.setText("确定");
			}
		});
		final MyDialog builder = new MyDialog(mContext, 1, view,
				R.style.mydialog);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				customName = remInfo.getText().toString();
				if (customName != null && !customName.equals("")) {
					if (FuXunTools.isConnect(mContext)) {
						new UpdateContactRem().start();
						builder.dismiss();
					} else {
						handler.sendEmptyMessage(8);
					}
				}
			}
		});
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
				cb.setCustomName(customName);
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
