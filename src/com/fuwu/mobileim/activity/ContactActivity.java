package com.fuwu.mobileim.activity;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.ContactAdapter;
import com.fuwu.mobileim.pojo.ContactPojo;

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
	private List<ContactPojo> contacts = new ArrayList<ContactPojo>();

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
	private Button view1, view2, view3;
	int width;
	private List<Button> btnList = new ArrayList<Button>();
	private int buttonNumber = -1;

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
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getApplicationContext(), "跳转到对话界面" + position,
						Toast.LENGTH_LONG).show();
			}
		});
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		Cursor cursor = getContentResolver().query(uri,
				new String[] { "display_name", "sort_key" }, null, null,
				"sort_key");
		if (cursor.moveToFirst()) {
			do {
				String name = cursor.getString(0);
				String sortKey = getSortKey(cursor.getString(1));
				ContactPojo contact = new ContactPojo();
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
		String key = sortKeyString.substring(0, 1).toUpperCase();// toUpperCase
																	// 方法返回一个字符串，该字符串中的所有字母都被转化为大写字母
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
		int width0 = 4; // 边框宽度
		int width1 = 20; // 外部边框距左右边界距离
		int hight0 = 100; // 外部边框高度
		int hight1 = hight0 - width0 * 2; // button高度
		LinearLayout a_layout = (LinearLayout) findViewById(R.id.a_layout);
		LayoutParams param = (LayoutParams) a_layout.getLayoutParams();
		param.leftMargin = 20;
		param.rightMargin = 20;
		param.topMargin = 10;
		param.bottomMargin = 10;
		param.height = hight0;

		view1 = (Button) findViewById(R.id.view_1);
		view2 = (Button) findViewById(R.id.view_2);
		view3 = (Button) findViewById(R.id.view_3);
		view1.setWidth(width0);
		view2.setWidth(width0);
		view3.setWidth(width0);
		view1.setHeight(hight1);
		view2.setHeight(hight1);
		view3.setHeight(hight1);

		int button_width = (width - width1 * 2 - 5 * width0) / 4;
		button_all = (Button) findViewById(R.id.button_all);
		button_recently = (Button) findViewById(R.id.button_recently);
		button_trading = (Button) findViewById(R.id.button_trading);
		button_subscription = (Button) findViewById(R.id.button_subscription);
		btnList.add(button_all);
		btnList.add(button_recently);
		btnList.add(button_trading);
		btnList.add(button_subscription);
		for (int i = 0; i < btnList.size(); i++) {
			btnList.get(i).setWidth(button_width);
			btnList.get(i).setHeight(hight1);
			//

		}
		button_all.setOnClickListener(listener_0);
		button_recently.setOnClickListener(listener_1);
		button_trading.setOnClickListener(listener_2);
		button_subscription.setOnClickListener(listener_3);
	}

	private View.OnClickListener listener_0 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 0;
			setButtonColor(buttonNumber);
		}
	};
	private View.OnClickListener listener_1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 1;
			setButtonColor(buttonNumber);
		}
	};	private View.OnClickListener listener_2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 2;
			setButtonColor(buttonNumber);
		}
	};	private View.OnClickListener listener_3 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 3;
			setButtonColor(buttonNumber);
		}
	};
	private void setButtonColor(int buttonNumber) {
		btnList.get(0).setBackgroundResource(R.drawable.left_shape_white);
		btnList.get(1).setBackgroundResource(R.drawable.middle_shape_white);
		btnList.get(2).setBackgroundResource(R.drawable.middle_shape_white);
		btnList.get(3).setBackgroundResource(R.drawable.right_shape_white);
		switch (buttonNumber) {
		case 0:
			btnList.get(buttonNumber).setBackgroundResource(R.drawable.left_shape_red);
			break;
		case 1:
		case 2:
			btnList.get(buttonNumber).setBackgroundResource(R.drawable.middle_shape_red);
			break;
		case 3:
			btnList.get(buttonNumber).setBackgroundResource(R.drawable.right_shape_red);
			break;
		default:
			break;
		}
	}
}
