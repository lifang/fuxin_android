package com.fuwu.mobileim.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ContactPojo;

/**
 * @作者 马龙
 * @时间 2014-5-14 下午3:05:36
 */
public class ContactListViewAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<ContactPojo> list = new ArrayList<ContactPojo>();

	public ContactListViewAdapter(Context context, List<ContactPojo> list) {
		super();
		this.mInflater = LayoutInflater.from(context);
		this.list = list;
	}

	public ContactListViewAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
	}

	public void addContact(ContactPojo cp) {
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

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.contact_item, null);
			holder.name = (TextView) convertView
					.findViewById(R.id.contact_name);
			holder.date = (TextView) convertView
					.findViewById(R.id.contact_date);
			holder.lastMes = (TextView) convertView
					.findViewById(R.id.contact_lastMes);
			holder.order = (TextView) convertView
					.findViewById(R.id.contact_order);
			holder.img = (ImageView) convertView.findViewById(R.id.contact_img);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ContactPojo cp = list.get(position);
		holder.name.setText(cp.getName());
		holder.date.setText(cp.getDate());
		holder.lastMes.setText(cp.getLastMes());
		holder.order.setText(cp.getOrder() == 0 ? "(订购者)" : "");

		return convertView;
	}

	public final class ViewHolder {
		public TextView name;
		public TextView lastMes;
		public TextView date;
		public TextView order;
		public ImageView img;
	}

}
