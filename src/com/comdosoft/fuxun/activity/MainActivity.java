package com.comdosoft.fuxun.activity;

import java.util.ArrayList;
import com.comdo.fuxun.R;
import com.comdosoft.fuxun.adapter.MainViewPagerAdapter;
import android.os.Bundle;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
 * @作者 马龙
 * @时间 2014-5-14 下午12:06:08
 */
public class MainActivity extends Activity implements OnPageChangeListener {

	private Context context = null;
	private LocalActivityManager manager = null;
	private ArrayList<View> views;
	private ImageView cursor;
	private ViewPager viewPager;
	private int offset = 0;
	private int currIndex = 0;
	private int cursorW = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViewById(R.id.menu_talk).setOnClickListener(new menuOnclick(0));
		findViewById(R.id.menu_address_book).setOnClickListener(
				new menuOnclick(1));
		findViewById(R.id.menu_settings).setOnClickListener(new menuOnclick(2));

		context = MainActivity.this;
		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);

		InitImageView();
		InitViewPager();
	}

	public void InitViewPager() {
		viewPager = (ViewPager) findViewById(R.id.main_viewPager);
		views = new ArrayList<View>();
		Intent intent = new Intent(context, TalkActivity.class);
		views.add(getView("Talk", intent));
		Intent intent2 = new Intent(context, AddressBookActivity.class);
		views.add(getView("AddressBook", intent2));
		Intent intent3 = new Intent(context, SettingsActivity.class);
		views.add(getView("Settings", intent3));

		viewPager.setAdapter(new MainViewPagerAdapter(views));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(this);
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
		viewPager.setCurrentItem(index);
		cursor.startAnimation(animation);
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

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		changeLocation(arg0);
	}

	@SuppressWarnings("deprecation")
	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}

}
