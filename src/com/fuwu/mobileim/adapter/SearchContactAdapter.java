package com.fuwu.mobileim.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;

/**
 * 联系人列表适配器
 * 
 * @作者 丁作强
 * @时间 2014-5-22 下午4:39:09
 */
public class SearchContactAdapter extends BaseAdapter implements SectionIndexer {
	private List<ShortContactPojo> list = null;
	private Context mContext;

	public SearchContactAdapter(Context mContext, List<ShortContactPojo> list) {
		this.mContext = mContext;
		this.list = list;
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<ShortContactPojo> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final ShortContactPojo contact = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.search_contact_adapter_item, null);
			viewHolder.contact_name = (TextView) view
					.findViewById(R.id.contact_name);
			viewHolder.contact_user_face = (CircularImage) view
					.findViewById(R.id.contact_user_face);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		// ImageCacheUtil.IMAGE_CACHE.get("/sdcard/fuxin/1.jpg",
		// viewHolder.contact_user_face);

		// 设置头像
		String face_str = contact.getUserface_url();
		String ContactId = "" + contact.getContactId();
		Log.i("linshi2", "加载头像---ContactId：" + ContactId);
		if (face_str.length() > 4) {


			File f = new File(Urlinterface.head_pic, contact.getContactId()
					+ "");
			if (f.exists()) {
				Log.i("linshi------------", "加载本地图片");

				viewHolder.contact_user_face.setTag(Urlinterface.head_pic
						+ contact.getContactId());
				// if not in cache, restore default
				if (!ImageCacheUtil.IMAGE_CACHE.get(Urlinterface.head_pic
						+ contact.getContactId(), viewHolder.contact_user_face)) {
					viewHolder.contact_user_face.setImageDrawable(null);
				}
		

			} else {
				FuXunTools.set_bk(contact.getContactId(), face_str,
						viewHolder.contact_user_face);
			}
		} else {
			viewHolder.contact_user_face.setImageResource(R.drawable.moren);
		}

		String customname = contact.getCustomName();
		if (customname != null && customname.length() > 0) {
			viewHolder.contact_name.setText(customname);
		} else {
			viewHolder.contact_name.setText(contact.getName());
		}

		return view;

	}

	final static class ViewHolder {
		TextView contact_name; // 名称
		CircularImage contact_user_face; // 头像
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortKey().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortKey();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 返回单个分组的大小。
	 * 
	 * @param str
	 */
	public int getNumber(String str) {
		int a = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getSortKey().equals(str)) {
				a = a + 1;
			}
		}
		return a;
	}

	@Override
	public Object[] getSections() {
		return null;
	}

}
