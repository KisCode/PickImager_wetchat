package com.kiscode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import com.kiscode.adapter.PhotoAdapter;
import com.kiscode.bean.ImageFilter;
import com.kiscode.bean.ImageFloder;

/***
 * 选择照片 Encode by GBK 出现乱码请选择GBK
 * 
 * @author K
 * 
 */
public class PickPhotoActivity extends Activity {

	private final String TAG = "PickPhotoActivity";

	/** 文件扫描完毕 */
	private final int CODE_SEARCH_COMPLETE = 0x01;

	private Context mContext;

	private GridView gridView;

	private PhotoAdapter mAdapter;

	/***
	 * 用于保存所有包含图片的文件夹路径，HashSet不能添加重复
	 */
	private HashSet<String> mDirPaths = new HashSet<String>();

	/** 文件夹集合 */
	private List<ImageFloder> mFloders = new ArrayList<ImageFloder>();

	/** 所有文件夹中包含图片最多的数量 */
	private int mPicSize;

	/** 所有文件夹中包含图片最多的文件夹 */
	private File mDirFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
		initViews();
		searchPhoto();
	}

	private void init() {
		mContext = this;
	}

	private void initViews() {
		gridView = (GridView) findViewById(R.id.gridview_photo);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CODE_SEARCH_COMPLETE:
				// 获得指定文件夹下所有文件
				List<String> filenameList = Arrays.asList(mDirFile
						.list(new ImageFilter()));
				mAdapter = new PhotoAdapter(mContext, filenameList,
						mDirFile.getAbsolutePath());
				gridView.setAdapter(mAdapter);
				break;

			default:
				break;
			}
		};
	};

	/***
	 * 扫描遍历本地所有图片
	 */
	private void searchPhoto() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// 内置存储设备未挂载
			Toast.makeText(mContext, "内置存储设备未挂载", Toast.LENGTH_SHORT).show();
			return;
		}

		// 1.遍历本地所有图片
		// 2.根据图片文件进行分类存储mFloders，默认显示图片最多的文件夹下的所有图片
		// 3.根据popup点击选择 指定文件夹，显示包含的所有图片
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 本地存储图片路径
				Uri imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver contentResolver = mContext.getContentResolver();

				Cursor cursor = contentResolver.query(imgUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=? ",
						new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_MODIFIED);
				Log.i(TAG, cursor.getCount() + "");
				while (cursor.moveToNext()) {
					// 查询图片路径所在列号
					int columIndex = cursor
							.getColumnIndex(MediaStore.Images.Media.DATA);
					String imgPath = cursor.getString(columIndex);
					Log.i(TAG, imgPath);
					File dirFile = new File(imgPath).getParentFile();
					if (dirFile == null) {
						continue;
					}

					String dirPath = dirFile.getAbsolutePath();
					if (mDirPaths.contains(dirPath)) {
						continue;
					}

					ImageFloder floder = new ImageFloder();
					floder.setDir(dirPath);
					floder.setFirstImagePath(imgPath);

					if (dirFile.list(new ImageFilter()) == null) {
						continue;
					}
					int imgSize = dirFile.list(new ImageFilter()).length;
					floder.setCount(imgSize);
					// 保存路径
					mDirPaths.add(dirPath);
					mFloders.add(floder);

					if (imgSize > mPicSize) {
						mPicSize = imgSize;
						mDirFile = dirFile;
					}
				}
				cursor.close();
				mDirPaths = null;
				Log.d(TAG, "文件夹数量：" + mFloders.size());
				Log.d(TAG, "最大文件夹包含图片数量：" + mPicSize);
				mHandler.sendEmptyMessage(CODE_SEARCH_COMPLETE);
			}
		}).start();
	}
}
