package com.fuwu.mobileim.adapter;


import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ContactPojo;


/**
 * 联系人列表适配器
 * @作者 丁作强
 * @时间 2014-5-22 下午4:39:09
 */
public class ContactAdapter  extends BaseAdapter implements SectionIndexer{
		private List<ContactPojo> list = null;
		private Context mContext;
		
		public ContactAdapter(Context mContext, List<ContactPojo> list) {
			this.mContext = mContext;
			this.list = list;
		}
		
		/**
		 * 当ListView数据发生变化时,调用此方法来更新ListView
		 * @param list
		 */
		public void updateListView(List<ContactPojo> list){
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
			final ContactPojo contact = list.get(position);
			if (view == null) {
				viewHolder = new ViewHolder();
				view = LayoutInflater.from(mContext).inflate(R.layout.contact_adapter_item, null);
				viewHolder.name = (TextView) view.findViewById(R.id.name);
				viewHolder.sort_key = (TextView) view.findViewById(R.id.sort_key);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}
	
			//根据position获取分类的首字母的Char ascii值
			int section = getSectionForPosition(position);
			
			//如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
			if(position == getPositionForSection(section)){
				viewHolder.sort_key.setVisibility(View.VISIBLE);
				viewHolder.sort_key.setText(contact.getSortKey()+" ["+getNumber(contact.getSortKey())+"人]");
			}else{
				viewHolder.sort_key.setVisibility(View.GONE);
			}
		
			viewHolder.name.setText(this.list.get(position).getName());
			
			return view;

		}
		


		final static class ViewHolder {
			TextView sort_key; // 分组关键字
			TextView name;  //  名称
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
		 * 提取英文的首字母，非英文字母用#代替。
		 * 
		 * @param str
		 * @return
		 */
		private String getAlpha(String str) {
			String  sortStr = str.trim().substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if (sortStr.matches("[A-Z]")) {
				return sortStr;
			} else {
				return "#";
			}
		}
		/**
		 * 返回单个分组的大小。
		 * 
		 * @param str
		 */
		public int getNumber(String str) {
			int a =0;
			for (int i = 0; i < list.size(); i++) {
				if(list.get(i).getSortKey().equals(str)){
					a=a+1;
				}
			}
			return a;
		}
		@Override
		public Object[] getSections() {
			return null;
		}
	}
