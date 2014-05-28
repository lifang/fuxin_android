package com.fuwu.mobileim.adapter;

import java.util.List;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-27 下午6:47:01
 */
public class FragmentViewPagerAdapter extends PagerAdapter implements
		OnPageChangeListener {

	private List<Fragment> list;
	private ViewPager viewPager;
	private FragmentManager fm;
	private int current = 0;

	public FragmentViewPagerAdapter(List<Fragment> list, ViewPager viewPager,
			FragmentManager fm) {
		super();
		this.list = list;
		this.viewPager = viewPager;
		this.fm = fm;
		this.viewPager.setAdapter(this);
		this.viewPager.setOnPageChangeListener(this);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(list.get(position).getView());
		// super.destroyItem(container, position, object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = list.get(position);
		if (!fragment.isAdded()) { // 如果fragment还没有added
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(fragment, fragment.getClass().getSimpleName());
			ft.commit();
			/**
			 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
			 * 会在进程的主线程中，用异步的方式来执行。 如果想要立即执行这个等待中的操作，就要调用这个方法（只能在主线程中调用）。
			 * 要注意的是，所有的回调和相关的行为都会在这个调用中被执行完成，因此要仔细确认这个方法的调用位置。
			 */
			fm.executePendingTransactions();
		}
		if (fragment.getView().getParent() == null) {
			container.addView(fragment.getView()); // 为viewpager增加布局
		}
		return fragment.getView();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		list.get(current).onPause(); // 调用切换前Fargment的onPause()
		// fragments.get(currentPageIndex).onStop(); // 调用切换前Fargment的onStop()
		if (list.get(arg0).isAdded()) {
			// fragments.get(i).onStart(); // 调用切换后Fargment的onStart()
			list.get(arg0).onResume(); // 调用切换后Fargment的onResume()
		}
		current = arg0;
	}

}
