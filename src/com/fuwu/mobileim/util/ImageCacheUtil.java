package com.fuwu.mobileim.util;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import cn.trinea.android.common.entity.FailedReason;
import cn.trinea.android.common.service.impl.ImageCache;
import cn.trinea.android.common.service.impl.ImageMemoryCache.OnImageCallbackListener;
import cn.trinea.android.common.util.CacheManager;
import cn.trinea.android.common.util.ImageCacheManager;
import cn.trinea.android.common.util.ObjectUtils;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-9 下午12:34:26
 */
public class ImageCacheUtil {
	public static final ImageCache IMAGE_CACHE = CacheManager.getImageCache();
	static {
		IMAGE_CACHE.setOnGetDataListener(ImageCacheManager
				.getImageFromSdcardListener());
		IMAGE_CACHE.setOnImageCallbackListener(new OnImageCallbackListener() {

			@Override
			public void onPreGet(String imageUrl, View view) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetSuccess(String imageUrl, Bitmap loadedImage,
					View view, boolean isInCache) {
				if (view != null && loadedImage != null) {
					ImageView imageView = (ImageView) view;
					// add tag judge, avoid listView cache and so on
					String imageUrlTag = (String) imageView.getTag();
					if (ObjectUtils.isEquals(imageUrlTag, imageUrl)) {
						imageView.setImageBitmap(loadedImage);
					}
				}
			}

			@Override
			public void onGetNotInCache(String imageUrl, View view) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetFailed(String imageUrl, Bitmap loadedImage,
					View view, FailedReason failedReason) {
				// TODO Auto-generated method stub

			}
		});
	}

}
