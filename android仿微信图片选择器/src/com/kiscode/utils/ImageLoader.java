package com.kiscode.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.kiscode.R;
import com.kiscode.bean.ImageBean;

public class ImageLoader {
	private final String TAG = "imageLoader";

	/** 加载图片完成 */
	private final int CODE_LOAD_IMAGE = 0x01;

	private static ImageLoader mImageLoader;
	private LruCache<String, Bitmap> mLruCache;

	/** 线程池 */
	private ExecutorService mExecutorService;

	/** 该handler用于处理图片，在子线程加载完毕后 主线程中显示 */
	private Handler mImgHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == CODE_LOAD_IMAGE) {
				ImageBean imageBean = (ImageBean) msg.obj;
				String path = imageBean.path;
				ImageView ivPhto = imageBean.ivPhoto;
				Bitmap bm = imageBean.bitmap;

				if (path.equals(ivPhto.getTag().toString())) {
					// 防止图片错位
					ivPhto.setImageBitmap(bm);
				}
			}
		};
	};

	/**
	 * 
	 */
	private ImageLoader() {
		init(6);
	}

	/***
	 * 初始化
	 * 
	 * @param threadCount
	 *            线程池 包括线程数量
	 */
	private void init(int threadCount) {
		// 获取应用程序最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		Log.i(TAG, "MAXSIZE" + maxMemory);// 128M
		int cacheSize = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
		mExecutorService = Executors.newFixedThreadPool(threadCount);
	}

	public static ImageLoader getInstance() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader();
		}
		return mImageLoader;
	}

	/***
	 * 加载本地图片
	 * 
	 * @param path
	 *            图片全路径
	 * @param iv
	 *            显示图片控件
	 */
	public void loadImage(final String path, final ImageView iv) {
		iv.setTag(path);
		Bitmap bitmap = getBitmapFromLruCache(path);
		if (bitmap == null) {
			iv.setImageResource(R.drawable.pictures_no);
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					// 图片采样为耗时操作，应该放在子线程中
					Bitmap bitmap = BitmapUtils
							.decodeSampledBitmapFromResource(path, 160, 160);
					addBitmapToLruCache(path, bitmap);
					ImageBean imageBean = new ImageBean(iv, path, bitmap);
					Message msg = mImgHandler.obtainMessage();
					msg.obj = imageBean;
					msg.what = CODE_LOAD_IMAGE;
					mImgHandler.sendMessage(msg);
				}
			});
		} else {
			iv.setImageBitmap(bitmap);
		}
	}

	/**
	 * 往LruCache中添加一张图片
	 * 
	 * @param key
	 * @param bitmap
	 */
	private void addBitmapToLruCache(String key, Bitmap bitmap) {
		if (getBitmapFromLruCache(key) == null) {
			if (bitmap != null)
				mLruCache.put(key, bitmap);
		}
	}

	/***
	 * 从Lru中取出一张图片，如果不存在则返回null
	 * 
	 * @param key
	 * @return
	 */
	private Bitmap getBitmapFromLruCache(String key) {
		return mLruCache.get(key);
	}
}
