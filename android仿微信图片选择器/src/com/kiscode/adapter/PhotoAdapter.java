package com.kiscode.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.kiscode.R;
import com.kiscode.utils.ImageLoader;

public class PhotoAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;

	/** 文件名集合 */
	private List<String> mFileNames;

	/*** 文件路径 */
	private String mDir;

	private ImageLoader mImageLoader;

	/**
	 * @param mContext
	 * @param mPathList
	 * @param mDir
	 */
	public PhotoAdapter(Context mContext, List<String> mPathList, String mDir) {
		this.mContext = mContext;
		this.mFileNames = mPathList;
		this.mDir = mDir;
		mInflater = LayoutInflater.from(mContext);
		mImageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getCount() {
		return mFileNames.size();
	}

	@Override
	public String getItem(int position) {
		return mFileNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mInflater.inflate(R.layout.grid_item, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.ivPhoto = (ImageView) view
					.findViewById(R.id.iv_photo_item);
			viewHolder.ivCheck = (ImageView) view
					.findViewById(R.id.iv_check_item);
			view.setTag(viewHolder);
		}

		ViewHolder vh = (ViewHolder) view.getTag();
		String filePath = mDir + File.separator + mFileNames.get(position);
		mImageLoader.loadImage(filePath, vh.ivPhoto);

		vh.ivCheck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		return view;
	}

	class ViewHolder {
		ImageView ivPhoto;
		ImageView ivCheck;
	}

}
