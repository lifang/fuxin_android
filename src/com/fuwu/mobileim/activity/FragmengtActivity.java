package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.FragmentViewPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-27 下午6:36:44
 */
public class FragmengtActivity extends FragmentActivity {
	private ViewPager vp;
	private List<Fragment> list = new ArrayList<Fragment>();

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.main);
		vp = (ViewPager) findViewById(R.id.main_viewPager);
		list.add(new FragmentAActivity());
		list.add(new ContactActivity());
		list.add(new FragmentCActivity());

		new FragmentViewPagerAdapter(list, vp, this.getSupportFragmentManager());
	}

}
