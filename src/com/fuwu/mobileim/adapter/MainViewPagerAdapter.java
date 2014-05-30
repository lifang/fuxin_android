package com.fuwu.mobileim.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-14 下午12:04:58
 */
public class MainViewPagerAdapter extends PagerAdapter {
	private List<View> mListViews;

	public MainViewPagerAdapter(List<View> mListViews) {
		this.mListViews = mListViews;
	}

	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mListViews.get(position));
	}

	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(mListViews.get(position), 0);
		return mListViews.get(position);
	}

	public int getCount() {
		return mListViews.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
}
