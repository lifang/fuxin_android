package com.zhishi.fuxun.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AlphabetIndexer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.comdo.fuxun.R;
import com.zhishi.fuxun.adapter.ContactAdapter;
import com.zhishi.fuxun.pojo.Contact;

/**
 * 联系人列表界面。
 * 
 * @作者 丁作强
 * @时间 2014-5-22 下午4:38:33
 */
public class ContactActivity extends Activity {

	/**
	 * 分组的布局
	 */
	// private LinearLayout titleLayout;

	/**
	 * 弹出式分组的布局
	 */
	private RelativeLayout sectionToastLayout;

	/**
	 * 右侧可滑动字母表
	 */
	private Button alphabetButton;

	/**
	 * 分组上显示的字母
	 */
	// private TextView title;

	/**
	 * 弹出式分组上的文字
	 */
	private TextView sectionToastText;

	/**
	 * 联系人ListView
	 */
	private ListView contactsListView;

	/**
	 * 联系人列表适配器
	 */
	private ContactAdapter adapter;

	/**
	 * 用于进行字母表分组
	 */
	private AlphabetIndexer indexer;

	/**
	 * 存储所有手机中的联系人
	 */
	private List<Contact> contacts = new ArrayList<Contact>();

	/**
	 * 定义字母表的排序规则
	 */
	private String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * 上次第一个可见元素，用于滚动时记录标识。
	 */
	private int lastFirstVisibleItem = -1;
	private Button button_all, button_recently, button_trading,
			button_subscription;
	int width;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_activity);
		Display display = this.getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		setButton();

		adapter = new ContactAdapter(this, R.layout.contact_adapter_item,
				contacts);
		// titleLayout = (LinearLayout) findViewById(R.id.title_layout);
		sectionToastLayout = (RelativeLayout) findViewById(R.id.section_toast_layout);
		// title = (TextView) findViewById(R.id.title);
		sectionToastText = (TextView) findViewById(R.id.section_toast_text);
		alphabetButton = (Button) findViewById(R.id.alphabetButton);
		contactsListView = (ListView) findViewById(R.id.contacts_list_view);
		contactsListView.setDivider(null);
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		Cursor cursor = getContentResolver().query(uri,
				new String[] { "display_name", "sort_key" }, null, null,
				"sort_key");
		if (cursor.moveToFirst()) {
			do {
				String name = cursor.getString(0);
				String sortKey = getSortKey(cursor.getString(1));
				Contact contact = new Contact();
				contact.setName(name);
				contact.setSortKey(sortKey);
				contacts.add(contact);
			} while (cursor.moveToNext());
		}
		startManagingCursor(cursor);
		indexer = new AlphabetIndexer(cursor, 1, alphabet);
		adapter.setIndexer(indexer);
		if (contacts.size() > 0) {
			setupContactsListView();
			setAlpabetListener();
		}
	}

	/**
	 * 为联系人ListView设置监听事件，根据当前的滑动状态来改变分组的显示位置，从而实现挤压动画的效果。
	 */
	private void setupContactsListView() {
		contactsListView.setAdapter(adapter);
		// contactsListView.setOnScrollListener(new OnScrollListener() {
		// @Override
		// public void onScrollStateChanged(AbsListView view, int scrollState) {
		// }
		//
		// @Override
		// public void onScroll(AbsListView view, int firstVisibleItem, int
		// visibleItemCount,
		// int totalItemCount) {
		// int section = indexer.getSectionForPosition(firstVisibleItem);
		// int nextSecPosition = indexer.getPositionForSection(section + 1);
		// if (firstVisibleItem != lastFirstVisibleItem) {
		// MarginLayoutParams params = (MarginLayoutParams)
		// titleLayout.getLayoutParams();
		// params.topMargin = 0;
		// titleLayout.setLayoutParams(params);
		// title.setText(String.valueOf(alphabet.charAt(section)));
		// }
		// if (nextSecPosition == firstVisibleItem + 1) {
		// View childView = view.getChildAt(0);
		// if (childView != null) {
		// int titleHeight = titleLayout.getHeight();
		// int bottom = childView.getBottom();
		// MarginLayoutParams params = (MarginLayoutParams) titleLayout
		// .getLayoutParams();
		// if (bottom < titleHeight) {
		// float pushedDistance = bottom - titleHeight;
		// params.topMargin = (int) pushedDistance;
		// titleLayout.setLayoutParams(params);
		// } else {
		// if (params.topMargin != 0) {
		// params.topMargin = 0;
		// titleLayout.setLayoutParams(params);
		// }
		// }
		// }
		// }
		// lastFirstVisibleItem = firstVisibleItem;
		// }
		// });

	}

	/**
	 * 设置字母表上的触摸事件，根据当前触摸的位置结合字母表的高度，计算出当前触摸在哪个字母上。
	 * 当手指按在字母表上时，展示弹出式分组。手指离开字母表时，将弹出式分组隐藏。
	 */
	private void setAlpabetListener() {
		alphabetButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float alphabetHeight = alphabetButton.getHeight();
				float y = event.getY();
				int sectionPosition = (int) ((y / alphabetHeight) / (1f / 27f));
				if (sectionPosition < 0) {
					sectionPosition = 0;
				} else if (sectionPosition > 26) {
					sectionPosition = 26;
				}
				String sectionLetter = String.valueOf(alphabet
						.charAt(sectionPosition));
				int position = indexer.getPositionForSection(sectionPosition);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// alphabetButton.setBackgroundResource(R.drawable.a_z_click);
					LayoutParams param = (LayoutParams) sectionToastLayout
							.getLayoutParams();
					param.rightMargin = 40;
					param.topMargin = (int) y;
					// if
					// (contacts.get(position).getSortKey().equals(sectionLetter))
					// {
					// TextView tv= (TextView)
					// contactsListView.getChildAt(position).findViewById(R.id.sort_key);
					// tv.setText("aaa");
					// }
					sectionToastLayout.setVisibility(View.VISIBLE);
					sectionToastText.setText(sectionLetter);
					contactsListView.setSelection(position);
					break;
				case MotionEvent.ACTION_MOVE:
					sectionToastText.setText(sectionLetter);
					contactsListView.setSelection(position);
					break;
				default:
					alphabetButton.setBackgroundResource(R.drawable.a_z);
					sectionToastLayout.setVisibility(View.GONE);
				}
				return true;
			}
		});
	}

	/**
	 * 获取sort key的首个字符，如果是英文字母就直接返回，否则返回#。
	 * 
	 * @param sortKeyString
	 *            数据库中读取出的sort key
	 * @return 英文字母或者#
	 */
	private String getSortKey(String sortKeyString) {
		alphabetButton.getHeight();
		String key = sortKeyString.substring(0, 1).toUpperCase();
		if (key.matches("[A-Z]")) {
			return key;
		}
		return "#";
	}

	/**
	 * 设置button的 宽度 以及监听
	 * 
	 * 
	 */
	private void setButton() {

		LinearLayout a_layout = (LinearLayout) findViewById(R.id.a_layout);
		LayoutParams param = (LayoutParams) a_layout.getLayoutParams();
		param.leftMargin = 20;
		int button_width = (width - 20 * 2) / 4;
		button_all = (Button) findViewById(R.id.button_all);
		button_recently = (Button) findViewById(R.id.button_recently);
		button_trading = (Button) findViewById(R.id.button_trading);
		button_subscription = (Button) findViewById(R.id.button_subscription);
		button_all.setWidth(button_width);
		button_recently.setWidth(button_width);
		button_trading.setWidth(button_width);
		button_subscription.setWidth(button_width);
	}
}
