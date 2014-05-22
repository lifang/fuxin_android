package com.fuwu.mobileim.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.comdo.fuxun.R;
import com.fuwu.mobileim.pojo.Contact;


/**
 * 联系人列表适配器
 * @作者 丁作强
 * @时间 2014-5-22 下午4:39:09
 */
public class ContactAdapter extends ArrayAdapter<Contact> {

	/**
	 * 需要渲染的item布局文件
	 */
	private int resource;
	private List<Contact> contacts;
	/**
	 * 字母表分组工具
	 */
	private SectionIndexer mIndexer;

	public ContactAdapter(Context context, int textViewResourceId, List<Contact> objects) {
		super(context, textViewResourceId, objects);
		resource = textViewResourceId;
		contacts= objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Contact contact = getItem(position);
		RelativeLayout layout = null;
		if (convertView == null) {
			layout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(resource, null);
		} else {
			layout = (RelativeLayout) convertView;
		}
		TextView name = (TextView) layout.findViewById(R.id.name);
		RelativeLayout sortKeyLayout = (RelativeLayout) layout.findViewById(R.id.sort_key_layout);
		TextView sortKey = (TextView) layout.findViewById(R.id.sort_key);
		name.setText(contact.getName());
		int section = mIndexer.getSectionForPosition(position);
		if (position == mIndexer.getPositionForSection(section)) {
			sortKey.setText(contact.getSortKey()+" ["+getNumber(contact.getSortKey())+"人]");
			sortKeyLayout.setVisibility(View.VISIBLE);
		} else {
			sortKeyLayout.setVisibility(View.GONE);
		}
		return layout;
	}

	
//	public Object[] getSections()  返回索引数组
//	 public int getPositionForSection(int sectionIndex) 按指定索引查找，返回匹配的第一行数据项位置或比较接近的数据项的位置
//	public int getSectionForPosition(int position)  按指定数据项的位置，返回匹配的索引项。
//	public void setCursor(Cursor cursor)  当数据发生变化时，更新数据源（Cursor）
	
	
	/**
	 * 给当前适配器传入一个分组工具。
	 * 
	 * @param indexer
	 */
	public void setIndexer(SectionIndexer indexer) {
		mIndexer = indexer;
	}
	/**
	 * 返回单个分组的大小。
	 * 
	 * @param str
	 */
	public int getNumber(String str) {
		int a =0;
		for (int i = 0; i < contacts.size(); i++) {
			if(contacts.get(i).getSortKey().equals(str)){
				a=a+1;
			}
		}
		return a;
	}

}
