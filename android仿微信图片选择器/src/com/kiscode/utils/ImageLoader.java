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

	/** ����ͼƬ��� */
	private final int CODE_LOAD_IMAGE = 0x01;

	private static ImageLoader mImageLoader;
	private LruCache<String, Bitmap> mLruCache;

	/** �̳߳� */
	private ExecutorService mExecutorService;

	/** ��handler���ڴ���ͼƬ�������̼߳�����Ϻ� ���߳�����ʾ */
	private Handler mImgHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == CODE_LOAD_IMAGE) {
				ImageBean imageBean = (ImageBean) msg.obj;
				String path = imageBean.path;
				ImageView ivPhto = imageBean.ivPhoto;
				Bitmap bm = imageBean.bitmap;

				if (path.equals(ivPhto.getTag().toString())) {
					// ��ֹͼƬ��λ
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
	 * ��ʼ��
	 * 
	 * @param threadCount
	 *            �̳߳� �����߳�����
	 */
	private void init(int threadCount) {
		// ��ȡӦ�ó����������ڴ�
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
	 * ���ر���ͼƬ
	 * 
	 * @param path
	 *            ͼƬȫ·��
	 * @param iv
	 *            ��ʾͼƬ�ؼ�
	 */
	public void loadImage(final String path, final ImageView iv) {
		iv.setTag(path);
		Bitmap bitmap = getBitmapFromLruCache(path);
		if (bitmap == null) {
			iv.setImageResource(R.drawable.pictures_no);
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					// ͼƬ����Ϊ��ʱ������Ӧ�÷������߳���
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
	 * ��LruCache�����һ��ͼƬ
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
	 * ��Lru��ȡ��һ��ͼƬ������������򷵻�null
	 * 
	 * @param key
	 * @return
	 */
	private Bitmap getBitmapFromLruCache(String key) {
		return mLruCache.get(key);
	}
}
