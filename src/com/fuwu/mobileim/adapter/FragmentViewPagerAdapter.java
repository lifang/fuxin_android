package com.fuwu.mobileim.adapter;

import java.util.List;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-27 下午6:47:01
 */
public class FragmentViewPagerAdapter extends PagerAdapter {

	private List<Fragment> list;
	private ViewPager viewPager;
	private FragmentManager fm;
	private int current = 0;
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return false;
	}

}
