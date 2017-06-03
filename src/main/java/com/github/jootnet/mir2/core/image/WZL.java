/*
 * Copyright 2017 JOOTNET Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Support: https://jootnet.github.io
 */
package com.github.jootnet.mir2.core.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import com.github.jootnet.mir2.core.BinaryReader;
import com.github.jootnet.mir2.core.SDK;
import com.github.jootnet.mir2.core.Texture;

/**
 * 热血传奇2WZL图片库
 * 
 * @author johness
 */
final class WZL implements ImageLibrary {

	private int imageCount;
	/**
	 * 获取库中图片数量
	 * 
	 * @return 存在于当前WZL库中的图片数量
	 */
	int getImageCount() {
		return imageCount;
	}
	/* 色深度
	private int colorCount = 8; */
    /* 图片数据起始位置 */
    private int[] offsetList;
    /* 图片数据长度 */
    private int[] lengthList;
    private ImageInfo[] imageInfos;
    /**
     * 获取库中图片信息数组
     * 
     * @return 所有存在于当前WZL库中的图片信息数组
     */
	ImageInfo[] getImageInfos() {
		return imageInfos;
	}
	/* WZL文件随机读取对象 */
	private BinaryReader br_wzl;
	private boolean loaded;
	/**
	 * 获取库加载状态
	 * 
	 * @return true表示库加载成功 false表示加载失败
	 */
	public boolean isLoaded() {
		return loaded;
	}
	/* 文件指针读取锁 */
    private Object wzl_locker = new Object();
    
    WZL(String wzlPath) {
    	String wzxPath = SDK.changeFileExtension(wzlPath, "wzx");
    	if(!new File(wzxPath).exists()) return;
    	try {
    		BinaryReader br_wzx = new BinaryReader(new File(wzxPath), "r");
    		br_wzx.skipBytes(44); // 跳过标题
    		imageCount = br_wzx.readIntLE();
			offsetList = new int[imageCount];
			for (int i = 0; i < imageCount; ++i)
			{
				// 读取数据偏移地址
				offsetList[i] = br_wzx.readIntLE();
			}
			br_wzx.close();
			br_wzl = new BinaryReader(new File(wzlPath), "r");
			imageInfos = new ImageInfo[imageCount];
            lengthList = new int[imageCount];
            for (int i = 0; i < imageCount; ++i)
            {
                // 读取图片信息和数据长度
                ImageInfo ii = new ImageInfo();
                br_wzl.seek(offsetList[i] + 4); // 跳过4字节未知数据
                ii.setWidth((short)br_wzl.readUnsignedShortLE());
				ii.setHeight((short)br_wzl.readUnsignedShortLE());
				ii.setOffsetX(br_wzl.readShortLE());
				ii.setOffsetY(br_wzl.readShortLE());
                imageInfos[i] = ii;
                lengthList[i] = br_wzl.readIntLE();
            }
            loaded = true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }

    /** 从zlib解压 */
	private static byte[] unzip(byte[] ziped) {
		InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(ziped));
		ByteArrayOutputStream o = new ByteArrayOutputStream(1024);
		try {
			int i = 1024;
			byte[] buf = new byte[i];

			while ((i = iis.read(buf, 0, i)) > 0) {
				o.write(buf, 0, i);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return o.toByteArray();
	}
    
    /**
     * 关闭WZL对象，释放其引用的文件流以及内存占用
     */
	public final void close() throws IOException {
		synchronized (wzl_locker) {
			offsetList = null;
			lengthList = null;
            imageInfos = null;
            loaded = false;
			if (br_wzl != null)
            {
				br_wzl.close();
            }
		}
	}

	public final Texture tex(int index) {
		if(index < 0) return Texture.EMPTY;
		if(index >= imageCount) return Texture.EMPTY;
    	try{
    		ImageInfo ii = imageInfos[index];
    		if(ii.getWidth() == 0 && ii.getHeight() == 0) return Texture.EMPTY;
    		int offset = offsetList[index];
    		int length = lengthList[index];
    		byte[] pixels = new byte[length];
    		br_wzl.seek(offset + 16);
    		br_wzl.read(pixels);
    		pixels = unzip(pixels);
    		byte[] sRGB = new byte[ii.getWidth() * ii.getHeight() * 3];
    		int p_index = 0;
    		for(int h = ii.getHeight() - 1; h >= 0 ; --h)
    			for(int w = 0; w < ii.getWidth(); ++w) {
    				// 跳过填充字节
    				if(w == 0)
    					p_index += SDK.skipBytes(8, ii.getWidth());
    				byte[] pallete = SDK.palletes[pixels[p_index++] & 0xff];
					int _idx = (w + h * ii.getWidth()) * 3;
					sRGB[_idx] = pallete[1];
					sRGB[_idx + 1] = pallete[2];
					sRGB[_idx + 2] = pallete[3];
    			}
	    	return new Texture(sRGB, ii.getWidth(), ii.getHeight());
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		return Texture.EMPTY;
    	}
    }

	public final ImageInfo info(int index) {
		if(index < 0) return ImageInfo.EMPTY;
		if(index >= imageCount) return ImageInfo.EMPTY;
		return imageInfos[index];
	}

}
