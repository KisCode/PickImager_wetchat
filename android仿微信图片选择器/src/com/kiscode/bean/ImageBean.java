package com.kiscode.bean;

import android.graphics.Bitmap;
import android.widget.ImageView;

/** 显示图片实体类 */
public class ImageBean {
	/** 显示图片的ImageView */
	public ImageView ivPhoto;

	/** 图像路径 */
	public String path;

	/** 图像 */
	public Bitmap bitmap;

	/**
	 * @param ivPhoto
	 * @param path
	 * @param bitmap
	 */
	public ImageBean(ImageView ivPhoto, String path, Bitmap bitmap) {
		this.ivPhoto = ivPhoto;
		this.path = path;
		this.bitmap = bitmap;
	}

}
