package com.fuwu.mobileim.adapter;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class TalkListViewAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context mcontext = null;
	private List<TalkPojo> list;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private static DBManager db;
	private SharedPreferences preferences;
	private int user_id = -1;
	public TalkListViewAdapter(Context context, List<TalkPojo> list,
			DisplayImageOptions options) {
		this.mcontext = context;
		this.mInflater = LayoutInflater.from(mcontext);
		this.list = list;
		this.options = options;
		db = new DBManager(context);
		preferences = mcontext.getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = preferences.getInt("user_id", -1);
	}

	public void updateList(List<TalkPojo> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ViewHolder holder;
		if (arg1 == null) {
			arg1 = mInflater.inflate(R.layout.talk_adpter, null);
			holder = new ViewHolder();
			holder.head = (CircularImage) arg1.findViewById(R.id.head);
			holder.statics = (LinearLayout) arg1.findViewById(R.id.statics);
			holder.size = (TextView) arg1.findViewById(R.id.size);
			holder.name = (TextView) arg1.findViewById(R.id.name);
			holder.content = (TextView) arg1.findViewById(R.id.content);
			holder.dath = (TextView) arg1.findViewById(R.id.dath);
			holder.pingbi=(ImageView) arg1.findViewById(R.id.pingbi);
			arg1.setTag(holder);
		} else {
			holder = (ViewHolder) arg1.getTag();
		}

		if (list.get(arg0).getMes_count() == 0) {
			holder.statics.setVisibility(View.GONE);
		} else {
			if (list.get(arg0).getMes_count() > 99) {
				holder.size.setText(99 + "");
			} else {
				holder.size.setText(list.get(arg0).getMes_count() + "");
			}
			holder.statics.setVisibility(View.VISIBLE);
		}
		String names = list.get(arg0).getNick_name();
		if (names != null && !names.equals("")) {
			holder.name.setText(names);
		} else {
			holder.name.setText("暂未设置昵称");
		}
		holder.content.setText(list.get(arg0).getContent());
		holder.dath.setText(TimeUtil.getTalkTime(list.get(arg0).getTime()));
		// ImageCacheUtil.IMAGE_CACHE.get("/sdcard/fuxin/1.jpg", holder.head);
		if (list.get(arg0).getContact_id() == 0) {
			holder.head.setImageResource(R.drawable.system_user_face);
		} else {
		File f = new File(Urlinterface.head_pic, list.get(arg0).getContact_id()
				+ "");
		if (f.exists()) {
			holder.head.setTag(Urlinterface.head_pic
					+ list.get(arg0).getContact_id());
			// if not in cache, restore default
			if (!ImageCacheUtil.IMAGE_CACHE.get(Urlinterface.head_pic
					+ list.get(arg0).getContact_id(), holder.head)) {
				holder.head.setImageDrawable(null);
			}
		} else {
			holder.head.setImageResource(R.drawable.moren);
		}
		
		
		ShortContactPojo cp = db.queryContact(user_id, list.get(arg0).getContact_id());
		if (cp.getIsBlocked()==1) {
			holder.pingbi.setVisibility(View.VISIBLE);
		}else {
			holder.pingbi.setVisibility(View.GONE);
		}
		}
		return arg1;
	}

	static final class ViewHolder {
		CircularImage head;
		LinearLayout statics;
		TextView size;
		TextView name;
		TextView content;
		TextView dath;
		ImageView pingbi;
	}

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
}
