package com.kiscode.bean;

import android.graphics.Bitmap;
import android.widget.ImageView;

/** ��ʾͼƬʵ���� */
public class ImageBean {
	/** ��ʾͼƬ��ImageView */
	public ImageView ivPhoto;

	/** ͼ��·�� */
	public String path;

	/** ͼ�� */
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
