package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.activity.MainActivity.menuOnclick;
import com.fuwu.mobileim.adapter.ContactAdapter;
import com.fuwu.mobileim.adapter.FragmentViewPagerAdapter;
import com.fuwu.mobileim.adapter.MainViewPagerAdapter;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.FxApplication;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-27 下午6:36:44
 */
public class FragmengtActivity extends FragmentActivity {
	private ViewPager vp;
	private List<Fragment> list = new ArrayList<Fragment>();
	private ImageView contact_search; // 搜索功能 图标
	private RelativeLayout main_search;// 搜索框全部
	private TextView contact_search_edittext;// 搜索框输入框
	private ImageView contact_search_empty;// 搜索框 清空图标
	private Button contact_search_cancel;// 搜索功能 取消按钮
	private ListView contacts_search_listview;// 搜索到的内容 listview
	private LinearLayout contacts_search_linearLayout;// 搜索 内容显示部分
	private FxApplication fxApplication;
	private List<ContactPojo> SourceDateList;
	private ContactAdapter adapter;
	private ImageView cursor;
	private int offset = 0;
	private int currIndex = 0;
	private int cursorW = 0;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.main);
		findViewById(R.id.menu_talk).setOnClickListener(new menuOnclick(0));
		findViewById(R.id.menu_address_book).setOnClickListener(
				new menuOnclick(1));
		findViewById(R.id.menu_settings).setOnClickListener(new menuOnclick(2));
		vp = (ViewPager) findViewById(R.id.main_viewPager);
		list.add(new TalkActivity());
		list.add(new ContactActivity());
		list.add(new SettingsActivity());

		FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(list,
				vp, this.getSupportFragmentManager());
		adapter.setOnExtraPageChangeListener(new FragmentViewPagerAdapter.OnExtraPageChangeListener() {
			@Override
			public void onExtraPageSelected(int i) {
				super.onExtraPageSelected(i);
				changeLocation(i);
				if (i == 1) {
					contact_search.setVisibility(View.VISIBLE);
				} else {
					contact_search.setVisibility(View.GONE);
				}
				
			}
		});

		contact_search = (ImageView) findViewById(R.id.contact_search);
		fxApplication = (FxApplication) getApplication();
		searchMethod();

		changeTitleStyle();
		setEdittextListening();
		InitImageView();

	}
	
	/**
	 * 初始化动画
	 */
	public void InitImageView() {
		cursor = (ImageView) findViewById(R.id.main_cursor);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		cursorW = cursor.getWidth();
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 3 - cursorW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}

	// 自定义更改图片位置
	public void changeLocation(int index) {
		int one = offset * 2 + cursorW;// 页卡1 -> 页卡2 偏移量
		Animation animation = new TranslateAnimation(one * currIndex, one
				* index, 0, 0);// 显然这个比较简洁，只有一行代码。
		animation.setFillAfter(true);// True:图片停在动画结束位置
		currIndex = index;
		animation.setDuration(300);
		vp.setCurrentItem(index);
		cursor.startAnimation(animation);
	}

	/**
	 * 改变“手机褔务网v1.0” 的 样式
	 */
	public void changeTitleStyle() {
		TextView tv = (TextView) findViewById(R.id.contact_title);
		String tv_str = (String) tv.getText().toString();
		SpannableStringBuilder style2 = new SpannableStringBuilder(tv_str);
		style2.setSpan(new AbsoluteSizeSpan(40), 0, 5,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		style2.setSpan(new AbsoluteSizeSpan(25), 5, tv_str.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(style2);

	}

	/**
	 * 搜索相关设置
	 */
	public void searchMethod() {
		contacts_search_linearLayout = (LinearLayout) findViewById(R.id.contacts_search_linearLayout); //
		contacts_search_listview = (ListView) findViewById(R.id.contacts_search_list_view); // 搜索到的内容
																							// listview
		contact_search = (ImageView) findViewById(R.id.contact_search); // 搜索功能图标
		main_search = (RelativeLayout) findViewById(R.id.main_search);// 搜索框全部
		contact_search_edittext = (TextView) findViewById(R.id.contact_search_edittext);// 搜索框输入框
		contact_search_empty = (ImageView) findViewById(R.id.contact_search_empty);// 搜索框清空图标
		contact_search_cancel = (Button) findViewById(R.id.contact_search_cancel);// 搜索功能
																					// 取消按钮
		contact_search.setOnClickListener(listener1);
		contact_search_empty.setOnClickListener(listener2);
		contact_search_cancel.setOnClickListener(listener3);
		contacts_search_listview.setDivider(null);
		contacts_search_listview
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
						// Toast.makeText(
						// getApplication(),
						// ((ContactPojo) adapter.getItem(position))
						// .getName(), Toast.LENGTH_SHORT).show();
						Toast.makeText(getApplication(), "传参，，跳到对话界面，并清空搜索框",
								Toast.LENGTH_SHORT).show();
						// contact_search_edittext.setText("");
					}
				});

	}

	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			main_search.setVisibility(View.VISIBLE);
			contacts_search_linearLayout.setVisibility(View.VISIBLE);
			// 模拟
			SourceDateList = fxApplication.getContactsList();
			// adapter = new ContactAdapter(MainActivity.this,
			// SourceDateList,-1);
			// contacts_search_listview.setAdapter(adapter);
		}
	};
	private View.OnClickListener listener2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			contact_search_edittext.setText("");
		}
	};
	private View.OnClickListener listener3 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			main_search.setVisibility(View.GONE);
			contacts_search_linearLayout.setVisibility(View.GONE);
			contact_search_edittext.setText("");
		}
	};

	/**
	 * 搜索输入框文本监听
	 */
	public void setEdittextListening() {
		contact_search_edittext.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String content = contact_search_edittext.getText().toString();
				adapter = new ContactAdapter(FragmengtActivity.this,
						findSimilarContacts(content), -1);
				contacts_search_listview.setAdapter(adapter);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
	}

	public List<ContactPojo> findSimilarContacts(String et) {
		List<ContactPojo> findlist = new ArrayList<ContactPojo>();
		if (et.length() > 0) {
			for (int i = 0; i < SourceDateList.size(); i++) {
				if (SourceDateList.get(i).getName().indexOf(et) != -1) {
					findlist.add(SourceDateList.get(i));
				}
			}
		}

		return findlist;
	}
	
	class menuOnclick implements OnClickListener {
		private int index = 0;

		public menuOnclick(int index) {
			super();
			this.index = index;
		}

		@Override
		public void onClick(View v) {
			if (index != currIndex) {
				changeLocation(index);
			}
		}

	}

}
