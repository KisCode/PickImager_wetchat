package com.kiscode.bean;

import java.io.File;
import java.io.FilenameFilter;

/***
 * ͼƬ��������
 * 
 * @author K
 * 
 */
public class ImageFilter implements FilenameFilter {
	@Override
	public boolean accept(File dir, String filename) {
		if (filename.endsWith(".jpg") || filename.endsWith(".png")
				|| filename.endsWith(".jpeg")) {
			return true;
		}
		return false;
	}
}
