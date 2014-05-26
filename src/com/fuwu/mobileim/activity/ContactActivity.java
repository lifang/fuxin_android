package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.view.CharacterParser;
import com.fuwu.mobileim.view.PinyinComparator;
import com.fuwu.mobileim.view.SideBar;
import com.fuwu.mobileim.view.SideBar.OnTouchingLetterChangedListener;

public class ContactActivity extends Activity {

	private FxApplication fxApplication;
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private ContactAdapter adapter;
	/**
	 * 弹出式分组的布局
	 */
	private RelativeLayout sectionToastLayout;
	private TextView sectionToastText;
	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<ContactPojo> SourceDateList;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;
	/**
	 * 定义字母表的排序规则
	 */
	private Button button_all, button_recently, button_trading,
			button_subscription;
	private Button view1, view2, view3;
	int width;
	private int buttonNumber = -1;
	private List<Button> btnList = new ArrayList<Button>();
	private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
	SideBar b;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_activity);
		fxApplication = (FxApplication) getApplication();
		initViews();
		Display display = this.getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		setButton();
	}

	private void initViews() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();
		SourceDateList = filledData(getResources().getStringArray(R.array.date));

		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
		fxApplication.setContactsList(SourceDateList);
		sectionToastLayout = (RelativeLayout) findViewById(R.id.section_toast_layout);
		sectionToastText = (TextView) findViewById(R.id.section_toast_text);

		sideBar = (SideBar) findViewById(R.id.sidrbar);
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {

				sideBar.setTextView(sectionToastText);
				sideBar.setRelativeLayout(sectionToastLayout);
				float alphabetHeight = sideBar.getHeight();
				int pos = getPositionForAlphabet(s);
				float y = (pos * 100 / 27f) * alphabetHeight / 100;
				LayoutParams param = (LayoutParams) sectionToastLayout
						.getLayoutParams();
				param.rightMargin = 60;
				param.topMargin = (int) y;
				// sectionToastText.setText(s);
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);

					// TextView tv= (TextView)
					// sortListView.getChildAt(position).findViewById(R.id.sort_key);
					// String tv_str = (String) tv.getText().toString();
					// SpannableStringBuilder style2 = new
					// SpannableStringBuilder(tv_str);
					// style2.setSpan(new ForegroundColorSpan(Color.RED), 0,
					// 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					// tv.setText(style2);
				}

			}
		});

		sortListView = (ListView) findViewById(R.id.contacts_list_view);
		sortListView.setDivider(null);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				Toast.makeText(getApplication(),
						((ContactPojo) adapter.getItem(position)).getName(),
						Toast.LENGTH_SHORT).show();
			}
		});

		adapter = new ContactAdapter(this, SourceDateList, 1);
		sortListView.setAdapter(adapter);

	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private List<ContactPojo> filledData(String[] date) {
		List<ContactPojo> mSortList = new ArrayList<ContactPojo>();

		for (int i = 0; i < date.length; i++) {
			ContactPojo sortModel = new ContactPojo();
			sortModel.setName(date[i]);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortKey(sortString.toUpperCase());
			} else {
				sortModel.setSortKey("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * 设置button的 宽度 以及监听
	 * 
	 * 
	 */
	private void setButton() {
		int width0 = 4; // 边框宽度
		int width1 = 20; // 外部边框距左右边界距离
		int hight0 = 80; // 外部边框高度
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
			adapter = new ContactAdapter(ContactActivity.this, SourceDateList,
					1);
			sortListView.setAdapter(adapter);
		}
	};
	private View.OnClickListener listener_1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 1;
			setButtonColor(buttonNumber);
			adapter = new ContactAdapter(ContactActivity.this, SourceDateList,
					0);
			sortListView.setAdapter(adapter);
		}
	};
	private View.OnClickListener listener_2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 2;
			setButtonColor(buttonNumber);
			adapter = new ContactAdapter(ContactActivity.this, SourceDateList,
					0);
			sortListView.setAdapter(adapter);
		}
	};
	private View.OnClickListener listener_3 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 3;
			setButtonColor(buttonNumber);
			adapter = new ContactAdapter(ContactActivity.this, SourceDateList,
					0);
			sortListView.setAdapter(adapter);
		}
	};

	private void setButtonColor(int buttonNumber) {
		btnList.get(0).setBackgroundResource(R.drawable.left_shape_white);
		btnList.get(1).setBackgroundResource(R.drawable.middle_shape_white);
		btnList.get(2).setBackgroundResource(R.drawable.middle_shape_white);
		btnList.get(3).setBackgroundResource(R.drawable.right_shape_white);
		switch (buttonNumber) {
		case 0:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.left_shape_red);
			break;
		case 1:
		case 2:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.middle_shape_red);
			break;
		case 3:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.right_shape_red);
			break;
		default:
			break;
		}
	}

	/**
	 * 根据分类的首字母获取 字母表中的位置
	 */
	public int getPositionForAlphabet(String s) {
		for (int i = 0; i < alphabet.length(); i++) {
			String sortStr = alphabet.charAt(i) + "";
			if (s.equals(sortStr)) {
				return i + 1;
			}
		}

		return -1;
	}

}
