package com.fuwu.mobileim.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.TimeUtil;
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

	public TalkListViewAdapter(Context context, List<TalkPojo> list,
			DisplayImageOptions options) {
		this.mcontext = context;
		this.mInflater = LayoutInflater.from(mcontext);
		this.list = list;
		this.options = options;
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
			arg1.setTag(holder);
		} else {
			holder = (ViewHolder) arg1.getTag();
		}

		if (list.get(arg0).getMes_count() == 0) {
			holder.statics.setVisibility(View.GONE);
		} else {
			holder.size.setText(list.get(arg0).getMes_count() + "");
			holder.statics.setVisibility(View.VISIBLE);
		}
		String names = list.get(arg0).getNick_name();
		if (names.equals("")) {
			holder.name.setText("暂未设置昵称");
		} else {
			holder.name.setText(names);
		}
		holder.content.setText(list.get(arg0).getContent());
		holder.dath.setText(TimeUtil.getChatTime(list.get(arg0).getTime()));
		imageLoader.displayImage(
				"http://p4.gexing.com/touxiang/2012/6/8/201268195891134.jpg",
				holder.head, options, animateFirstListener);
		return arg1;

	}

	static final class ViewHolder {
		CircularImage head;
		LinearLayout statics;
		TextView size;
		TextView name;
		TextView content;
		TextView dath;
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
