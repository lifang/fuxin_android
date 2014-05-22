package com.comdosoft.fuxun.activity;

import java.util.ArrayList;
import java.util.List;

import com.comdo.fuxun.R;
import com.comdosoft.fuxun.adapter.ContactListViewAdapter;
import com.comdosoft.fuxun.pojo.ContactPojo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-14 下午12:06:40
 */
public class TalkActivity extends Activity {
	private ListView mListView;
	private ContactListViewAdapter clvAdapter;
	private List<ContactPojo> list = new ArrayList<ContactPojo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.talk);
		mListView = (ListView) findViewById(R.id.talk_listview);
		for (int i = 0; i < 20; i++) {
			list.add(new ContactPojo("联系人" + i, "5月7日", "最近的一条对话文本记录", "", 0));
		}
		clvAdapter = new ContactListViewAdapter(this, list);
		clvAdapter.addContact(new ContactPojo("马龙", "5月15日", "哈喽", "", 1));
		mListView.setAdapter(clvAdapter);
	}
}
