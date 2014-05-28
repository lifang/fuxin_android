package com.fuwu.mobileim.activity;

import com.fuwu.mobileim.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-14 下午12:06:40
 */
public class FragmentBActivity extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.login, container, false);
	}

}
