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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.github.jootnet.mir2.core.BinaryReader;
import com.github.jootnet.mir2.core.SDK;
import com.github.jootnet.mir2.core.Texture;

/**
 * 热血传奇2WIL图片库
 * 
 * @author johness
 */
final class WIL implements ImageLibrary {

	/**
	 * 是否只用WIL中的数据解析图片，而不看WIX的内容
	 */
	public static boolean GLOBAL_ONLYWIL_MODE = true;
	
	private int imageCount;
	/**
	 * 获取库中图片数量
	 * 
	 * @return 存在于当前WIL库中的图片数量
	 */
	int getImageCount() {
		return imageCount;
	}
	/* 版本标识 */
    private int verFlag;
    /* 图片数据起始位置 */
    private int[] offsetList;
    private ImageInfo[] imageInfos;
    /**
     * 获取库中图片信息数组
     * 
     * @return 所有存在于当前WIL库中的图片信息数组
     */
	ImageInfo[] getImageInfos() {
		return imageInfos;
	}
	/* WIL文件随机读取对象 */
	private BinaryReader br_wil;
	private volatile boolean loaded;
	/**
	 * 获取库加载状态
	 * 
	 * @return true表示库加载成功 false表示加载失败
	 */
	public boolean isLoaded() {
		return loaded;
	}
	/* 文件指针读取锁 */
    private Object wil_locker = new Object();
    
    WIL(String wilPath) {
		File f_wil = new File(wilPath);
		if(!f_wil.exists()) return;
		if(!f_wil.isFile()) return;
		if(!f_wil.canRead()) return;
    	try {
        	String wixPath = SDK.changeFileExtension(wilPath, "wix");
        	boolean wilOnlyMode = false;
    		File f_wix = new File(wixPath);
    		if(GLOBAL_ONLYWIL_MODE || !f_wix.exists()) {
    			wilOnlyMode = true;
    		}
    		if(!wilOnlyMode && !f_wix.isFile()) return;
    		if(!wilOnlyMode && !f_wix.canRead()) return;
			br_wil = new BinaryReader(f_wil, "r");
			br_wil.skipBytes(44); // 跳过标题
			imageCount = br_wil.readIntLE(); // 图片数量
			offsetList = new int[imageCount + 1];
			offsetList[imageCount] = (int)br_wil.length();
			int colorCount = SDK.colorCountToBitCount(br_wil.readIntLE()); // 色深度
			if(colorCount == 8) {
				// 8位灰度图可能版本标识不为0，此时操作不一样
				br_wil.skipBytes(4); // 忽略调色板
				verFlag = br_wil.readIntLE();
			}
    		if(!wilOnlyMode) {
	    		BinaryReader br_wix = new BinaryReader(f_wix, "r");
				br_wix.skipBytes(44); // 跳过标题
				int indexCount = br_wix.readIntLE(); // 索引数量(也是图片数量)
				if(verFlag != 0)
					br_wix.skipBytes(4); // 版本标识不为0需要跳过4字节
				for (int i = 0; i < indexCount; ++i)
	            {
	                // 读取数据偏移量
					offsetList[i] = br_wix.readIntLE();
	            }
				br_wix.close();
    		} else {
				imageInfos = new ImageInfo[imageCount];
				int lastOffset = 1024 + 48 + 8;
				for(int i = 0; i < imageCount; ++i) {
					offsetList[i] = lastOffset;
	    			if(colorCount == 8) {
    					if(lastOffset + 9 > br_wil.length()) {
    						// 数据出错，直接赋值为空图片
    						imageInfos[i] = ImageInfo.EMPTY;
    	            		continue;
    					}
	    			} else {
    					if(lastOffset + 12 > br_wil.length()) {
    						// 数据出错，直接赋值为空图片
    						imageInfos[i] = ImageInfo.EMPTY;
    	            		continue;
    					}
	    			}
					br_wil.seek(lastOffset);
					short w = (short)br_wil.readUnsignedShortLE();
					short h = (short)br_wil.readUnsignedShortLE();
	                lastOffset += 8;
					if(w == 1 && h == 1) {
						// WIL可能有空图片，此时图片大小为1x1
		    			if(colorCount == 8) {
							lastOffset += 1;
		    			} else {
							lastOffset += 4;
		    			}
						imageInfos[i] = ImageInfo.EMPTY;
	            		continue;
					}
					ImageInfo ii = new ImageInfo();
	                ii.setColorBit((byte) colorCount);
	                ii.setWidth(w);
	                ii.setHeight(h);
					ii.setOffsetX(br_wil.readShortLE());
					ii.setOffsetY(br_wil.readShortLE());
	                imageInfos[i] = ii;
	                lastOffset += SDK.widthBytes(colorCount * w) * h;
				}
				loaded = true;
				return;
    		}
			imageInfos = new ImageInfo[imageCount];
			for (int i = 0; i < imageCount; ++i) {
				int offset = offsetList[i];
				if(offset + 9 > br_wil.length()) {
					// 数据出错，直接赋值为空图片
					imageInfos[i] = ImageInfo.EMPTY;
            		continue;
				}
				int length = offsetList[i + 1] - offset - 8;
				if(length < 2) {
					// WIL中色彩数据为1个字节的是空图片，此时图片大小为1x1
					imageInfos[i] = ImageInfo.EMPTY;
            		continue;
				}
                // 读取图片信息
                ImageInfo ii = new ImageInfo();
                ii.setColorBit((byte) colorCount);
                br_wil.seek(offset);
				ii.setWidth((short)br_wil.readUnsignedShortLE());
				ii.setHeight((short)br_wil.readUnsignedShortLE());
				ii.setOffsetX(br_wil.readShortLE());
				ii.setOffsetY(br_wil.readShortLE());
                imageInfos[i] = ii;
            }
            loaded = true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }

    /**
     * 关闭WIL对象，释放其引用的文件流以及内存占用
     */
	public final void close() throws IOException {
		synchronized (wil_locker) {
			offsetList = null;
            imageInfos = null;
            loaded = false;
			if (br_wil != null)
            {
				br_wil.close();
            }
		}
	}

	public final Texture tex(int index) {
		if(!loaded) return Texture.EMPTY;
		if(index < 0) return Texture.EMPTY;
		if(index >= imageCount) return Texture.EMPTY;
		if(imageInfos[index] == ImageInfo.EMPTY) return Texture.EMPTY;
    	try{
	    	ImageInfo ii = imageInfos[index];
	    	byte[] pixels = null;
	    	synchronized(wil_locker) {
	    		br_wil.seek(offsetList[index] + 8);
	    		int pixelLength = offsetList[index + 1] - offsetList[index];
                pixels = new byte[pixelLength - 8];
				br_wil.readFully(pixels);
				if(pixels.length == 1) {
					// 空白图片
					byte[] sRGB = new byte[3];
					byte[] pallete = SDK.palletes[pixels[0] & 0xff];
					sRGB[0] = pallete[1];
					sRGB[1] = pallete[2];
					sRGB[2] = pallete[3];
					return new Texture(sRGB, 1, 1);
				}
	    	}
	    	byte[] sRGB = new byte[ii.getWidth() * ii.getHeight() * 3];
	    	if (ii.getColorBit() == 8)
            {
                int p_index = 0;
                for (int h = ii.getHeight() - 1; h >= 0; --h)
                    for (int w = 0; w < ii.getWidth(); ++w)
                    {
                        // 跳过填充字节
                        if (w == 0)
                            p_index += SDK.skipBytes(8, ii.getWidth());
                        byte[] pallete = SDK.palletes[pixels[p_index++] & 0xff];
    					int _idx = (w + h * ii.getWidth()) * 3;
    					sRGB[_idx] = pallete[1];
    					sRGB[_idx + 1] = pallete[2];
    					sRGB[_idx + 2] = pallete[3];
                    }
            }
	    	else if (ii.getColorBit() == 16)
            {
	    		ByteBuffer bb = ByteBuffer.wrap(pixels);
	    		bb.order(ByteOrder.LITTLE_ENDIAN);
	    		int p_index = 0;
                for (int h = ii.getHeight() - 1; h >= 0; --h)
                    for (int w = 0; w < ii.getWidth(); ++w, p_index += 2)
                    {
                        // 跳过填充字节
                        if (w == 0)
                            p_index += SDK.skipBytes(16, ii.getWidth());
                        short pdata = bb.getShort(p_index);
                        byte r = (byte) ((pdata & 0xf800) >> 8);// 由于是与16位做与操作，所以多出了后面8位
                        byte g = (byte) ((pdata & 0x7e0) >> 3);// 多出了3位，在强转时前8位会自动丢失
                        byte b = (byte) ((pdata & 0x1f) << 3);// 少了3位
    					int _idx = (w + h * ii.getWidth()) * 3;
    					sRGB[_idx] = r;
    					sRGB[_idx + 1] = g;
    					sRGB[_idx + 2] = b;
                    }
            }
	    	return new Texture(sRGB, ii.getWidth(), ii.getHeight());
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		return Texture.EMPTY;
    	}
    }

	public final ImageInfo info(int index) {
		if(!loaded) return ImageInfo.EMPTY;
		if(index < 0) return ImageInfo.EMPTY;
		if(index >= imageCount) return ImageInfo.EMPTY;
		return imageInfos[index];
	}

	public int count() {
		return imageCount;
	}
}
