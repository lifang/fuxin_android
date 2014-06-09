package com.fuwu.mobileim.util;

import cn.trinea.android.common.service.impl.ImageCache;
import cn.trinea.android.common.util.CacheManager;
import cn.trinea.android.common.util.ImageCacheManager;



/**
 * @作者 马龙
 * @时间 创建时间：2014-6-9 下午12:34:26
 */
public class ImageCacheUtil {
	public static final ImageCache IMAGE_CACHE = CacheManager.getImageCache();
	static {
		IMAGE_CACHE.setOnGetDataListener(ImageCacheManager
				.getImageFromSdcardListener());
	}
}
