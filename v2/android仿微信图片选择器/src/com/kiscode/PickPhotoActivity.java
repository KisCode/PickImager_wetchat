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
 * ѡ����Ƭ Encode by GBK ����������ѡ��GBK
 * 
 * @author K
 * 
 */
public class PickPhotoActivity extends Activity {

	private final String TAG = "PickPhotoActivity";

	/** �ļ�ɨ����� */
	private final int CODE_SEARCH_COMPLETE = 0x01;

	private Context mContext;

	private GridView gridView;

	private PhotoAdapter mAdapter;

	/***
	 * ���ڱ������а���ͼƬ���ļ���·����HashSet��������ظ�
	 */
	private HashSet<String> mDirPaths = new HashSet<String>();

	/** �ļ��м��� */
	private List<ImageFloder> mFloders = new ArrayList<ImageFloder>();

	/** �����ļ����а���ͼƬ�������� */
	private int mPicSize;

	/** �����ļ����а���ͼƬ�����ļ��� */
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
				// ���ָ���ļ����������ļ�
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
	 * ɨ�������������ͼƬ
	 */
	private void searchPhoto() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// ���ô洢�豸δ����
			Toast.makeText(mContext, "���ô洢�豸δ����", Toast.LENGTH_SHORT).show();
			return;
		}

		// 1.������������ͼƬ
		// 2.����ͼƬ�ļ����з���洢mFloders��Ĭ����ʾͼƬ�����ļ����µ�����ͼƬ
		// 3.����popup���ѡ�� ָ���ļ��У���ʾ����������ͼƬ
		new Thread(new Runnable() {
			@Override
			public void run() {
				// ���ش洢ͼƬ·��
				Uri imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver contentResolver = mContext.getContentResolver();

				Cursor cursor = contentResolver.query(imgUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=? ",
						new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_MODIFIED);
				Log.i(TAG, cursor.getCount() + "");
				while (cursor.moveToNext()) {
					// ��ѯͼƬ·�������к�
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
					// ����·��
					mDirPaths.add(dirPath);
					mFloders.add(floder);

					if (imgSize > mPicSize) {
						mPicSize = imgSize;
						mDirFile = dirFile;
					}
				}
				cursor.close();
				mDirPaths = null;
				Log.d(TAG, "�ļ���������" + mFloders.size());
				Log.d(TAG, "����ļ��а���ͼƬ������" + mPicSize);
				mHandler.sendEmptyMessage(CODE_SEARCH_COMPLETE);
			}
		}).start();
	}
}
