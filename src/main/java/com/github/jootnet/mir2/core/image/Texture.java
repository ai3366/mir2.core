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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * 热血传奇图片数据<br>
 * 使用三字节sRGB方式存放色彩数据<br>
 * 图片不支持透明色，背景为黑色<br>
 * 使用双缓冲加速图像处理<br>
 * 此图片与{@link BufferedImage}相互转换
 * 
 * @author johness
 */
public final class Texture implements Cloneable {

	/**
	 * 空图片
	 */
	public static final Texture EMPTY = new Texture(null, 0, 0);
	/**
	 * 空BufferedImage图片
	 */
	public static final BufferedImage EMPTY_BUFFEREDIMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	
	private byte[] pixels;
	private int width;
	private int height;
	
	private boolean emptyHoldFlag;
	private static byte[] emptyPixels;
	private static long clearCount;
	private static Object clear_locker = new Object();
	private boolean bufferedImageHoldFlag;
	private BufferedImage bufferedImage;
	private boolean dirty = true; // 先将此值置为true，第一次转换为BufferedImage时才能正确处理
	private Object proc_locker = new Object();
	
	/**
	 * 从RGB字节数组创建图片数据
	 * 
	 * @param sRGB
	 * 		图片色彩数据数据<br>
	 * 		每个像素占用三个字节进行存储，从图片左上角到右下角，必须是RGB顺序
	 * @param width
	 * 		图片宽度
	 * @param height
	 * 		图片高度
	 * 
	 * @throws IllegalArgumentException 传入的像素数据长度不符合要求
	 */
	public Texture(byte[] sRGB, int width, int height) throws IllegalArgumentException {
		this(sRGB, width, height, true, true);
	}
	
	/**
	 * 从RGB字节数组创建图片数据
	 * 
	 * @param sRGB
	 * 		图片色彩数据数据<br>
	 * 		每个像素占用三个字节进行存储，从图片左上角到右下角，必须是RGB顺序
	 * @param width
	 * 		图片宽度
	 * @param height
	 * 		图片高度
	 * @param emptyHoldFlag
	 * 		是否存储一个空的字节数组，用于在将图片清空时快速反应
	 * @param bufferedImageHoldFlag
	 * 		是否存储一个{@link BufferedImage}对象，用于在请求图片数据转换时快速反映<br>
	 * 		此值建议为true，当为false时会每次重新创建BufferedImage，而会浪费效率
	 * 
	 * @throws IllegalArgumentException 传入的像素数据长度不符合要求
	 */
	public Texture(byte[] sRGB, int width, int height, boolean emptyHoldFlag, boolean bufferedImageHoldFlag) throws IllegalArgumentException {
		if(sRGB != null && width > 0 && height > 0 && sRGB.length != (width * height * 3))
			throw new IllegalArgumentException("sRGB length not match width * height * 3 !!!");
		this.pixels = sRGB;
		this.width = width;
		this.height = height;
		this.emptyHoldFlag = emptyHoldFlag;
		this.bufferedImageHoldFlag = bufferedImageHoldFlag;
	}
	
	/**
	 * 判断当前图片是否为空
	 * 
	 * @return true表示当前图片为空，不可用于任何处理/绘制/序列化
	 */
	public final boolean empty() {
		return this == EMPTY || pixels == null || pixels.length == 0 || width < 1 || height < 1;
	}
	
	protected void finalize() {
		if(emptyHoldFlag) {
			synchronized (clear_locker) {
				clearCount--;
				if(clearCount < 1) {
					clearCount = 0;
					emptyPixels = null;
				}
			}
		}
	}
	
	/**
	 * 创建当前图片数据的克隆，完整克隆<br>
	 * 如需创建当前图片部分区域的克隆，则使用{@link #clip(int, int, int, int)}
	 * 
	 * @return 当前图片完整克隆
	 * 
	 * @see #clip(int, int, int, int)
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		if(empty())
			return EMPTY;
		synchronized (proc_locker) {
			byte[] sRGB = new byte[pixels.length];
			System.arraycopy(pixels, 0, sRGB, 0, pixels.length);
			return new Texture(sRGB, width, height);
		}
	}
	
	/**
	 * 创建当前图片数据的克隆，部分克隆<br>
	 * 如果区域的右方或下方超出图片宽高则忽略超出部分，但左上方不可超出，如果超出则直接不进行处理<br>
	 * 如需创建当前图完整克隆，则使用{@link #clone()}
	 * 
	 * @param x
	 * 		克隆区域起始x坐标
	 * @param y
	 * 		克隆区域起始y坐标
	 * @param w
	 * 		克隆区域宽度
	 * @param h
	 * 		克隆区域高度
	 * 
	 * @return 当前图片部分区域克隆
	 * 
	 * @see #clone()
	 */
	public final Texture clip(int x, int y, int w, int h) {
		if(empty())
			return EMPTY;
		if(x < 0 || x > width || y < 0 || y > height) return EMPTY;
		synchronized (proc_locker) {
			int rx = x + w;
			if(rx >= width)
				rx = width - 1;
			int by = y + h;
			if(by >= height)
				by = height - 1;
			byte[] npixels = new byte[(rx - x) * (by - y) * 3];
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx = (j + i * width) * 3;
					npixels[(j - x + (i - y) * width) * 3] = pixels[_idx];
					npixels[(j - x + (i - y) * width) * 3 + 1] = pixels[_idx + 1];
					npixels[(j - x + (i - y) * width) * 3 + 2] = pixels[_idx + 2];
				}
			}
			return new Texture(npixels, rx -x, by -y);
		}
	}
	
	/**
	 * 将图片数据转换为{@link BufferedImage}对象
	 * 
	 * @return 图片数据对应的{@link BufferedImage}对象
	 */
	public final BufferedImage toBufferedImage() {
		if(empty())
			return EMPTY_BUFFEREDIMAGE;
		synchronized (proc_locker) {
			BufferedImage bi = null;
			if(!bufferedImageHoldFlag || dirty) {
				// 将byte[]转为DataBufferByte用于后续创建BufferedImage对象
		        DataBufferByte dataBuffer = new DataBufferByte(pixels, pixels.length);
		        // sRGB色彩空间对象
		        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		        int[] nBits = {8, 8, 8};
		        int[] bOffs = {0, 1, 2};
		        ComponentColorModel colorModel = new ComponentColorModel(cs, nBits, false, false,
		                                             Transparency.OPAQUE,
		                                             DataBuffer.TYPE_BYTE);        
		        WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width*3, 3, bOffs, null);
		        bi = new BufferedImage(colorModel,raster,false,null);
			}
			if(bufferedImageHoldFlag) {
				if(dirty)
					bufferedImage = bi;
				dirty = false;
				return bufferedImage;
			}
			return bi;
		}
	}
	
	/**
	 * 清除图片色彩数据<br>
	 * 清除图片全部色彩数据<br>
	 * 如果需要清除部分区域色彩数据则使用{@link #clear(int, int, int, int)}
	 * 
	 * @see #clear(int, int, int, int)
	 */
	public final void clear() {
		if(empty()) return;
		synchronized (proc_locker) {
			if(!emptyHoldFlag) {
				byte[] _emptyPixels = new byte[pixels.length];
				System.arraycopy(_emptyPixels, 0, pixels, 0, _emptyPixels.length);
			} else {
				synchronized (clear_locker) {
					if(emptyPixels == null || emptyPixels.length < pixels.length)
						emptyPixels = new byte[pixels.length];
					System.arraycopy(emptyPixels, 0, pixels, 0, pixels.length);
					clearCount++;
				}
			}
			dirty = true;
		}
	}
	
	/**
	 * 清除图片色彩数据<br>
	 * 清除图片内部分区域色彩数据<br>
	 * 如果区域的右方或下方超出图片宽高则忽略超出部分，但左上方不可超出，如果超出则直接不进行处理<br>
	 * 如果需要清除全部色彩数据则使用{@link #clear()}
	 * 
	 * @param x
	 * 		要清除的区域起始x坐标
	 * @param y
	 * 		要清除的区域起始y坐标
	 * @param w
	 * 		要清除的区域宽度
	 * @param h
	 * 		要清除的区域高度
	 * 
	 * @see #clear()
	 */
	public final void clear(int x, int y, int w, int h) {
		if(empty()) return;
		if(x < 0 || x > width || y < 0 || y > height) return;
		synchronized (proc_locker) {
			int rx = x + w;
			if(rx >= width)
				rx = width - 1;
			int by = y + h;
			if(by >= height)
				by = height - 1;
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx = (j + i * width) * 3;
					pixels[_idx] = pixels[_idx + 1] = pixels[_idx + 2] = 0;
				}
			}
			dirty = true;
		}
	}
	
	/**
	 * 将图片转换为灰白<br>
	 * 将图片全部区域转换为灰白<br>
	 * 如果需要转换部分区域为灰白则使用{@link #toGray(int, int, int, int)}
	 * 
	 * @see #toGray(int, int, int, int)
	 */
	public final void toGray() {
		if(empty()) return;
		synchronized (proc_locker) {
			for(int i = 0; i < pixels.length - 2; i += 3) {
				pixels[i] *= 0.299;
				pixels[i + 1] *= 0.587;
				pixels[i + 2] *= 0.114;
			}
			dirty = true;
		}
	}
	
	/**
	 * 将图片转换为灰白<br>
	 * 将图片部分区域转换为灰白<br>
	 * 如果区域的右方或下方超出图片宽高则忽略超出部分，但左上方不可超出，如果超出则直接不进行处理<br>
	 * 如果需要转换全部区域为灰白则使用{@link #toGray()}
	 * 
	 * @param x
	 * 		要转换的区域起始x坐标
	 * @param y
	 * 		要转换的区域起始y坐标
	 * @param w
	 * 		要转换的区域宽度
	 * @param h
	 * 		要转换的区域高度
	 * 
	 * @see #toGray()
	 */
	public final void toGray(int x, int y, int w, int h) {
		if(empty()) return;
		if(x < 0 || x > width || y < 0 || y > height) return;
		synchronized (proc_locker) {
			int rx = x + w;
			if(rx >= width)
				rx = width - 1;
			int by = y + h;
			if(by >= height)
				by = height - 1;
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx = (j + i * width) * 3;
					pixels[_idx] *= 0.299;
					pixels[_idx + 1] *= 0.587;
					pixels[_idx + 2] *= 0.114;
				}
			}
			dirty = true;
		}
	}
	
	/**
	 * 将图片进行反色处理<br>
	 * 将图片全部区域进行反色处理<br>
	 * 如果需要对部分区域进行反色处理则使用{@link #inverse(int, int, int, int)}
	 * 
	 * @see #inverse(int, int, int, int)
	 */
	public final void inverse() {
		if(empty()) return;
		synchronized (proc_locker) {
			for(int i = 0; i < pixels.length; ++i) {
				pixels[i] ^= 0xff;
			}
			dirty = true;
		}
	}
	
	/**
	 * 将图片进行反色处理<br>
	 * 将图片部分区域进行反色处理<br>
	 * 如果区域的右方或下方超出图片宽高则忽略超出部分，但左上方不可超出，如果超出则直接不进行处理<br>
	 * 如果需要对全部区域进行反色处理则使用{@link #inverse()}
	 * 
	 * @param x
	 * 		要转换的区域起始x坐标
	 * @param y
	 * 		要转换的区域起始y坐标
	 * @param w
	 * 		要转换的区域宽度
	 * @param h
	 * 		要转换的区域高度
	 * 
	 * @see #inverse()
	 */
	public final void inverse(int x, int y, int w, int h) {
		if(empty()) return;
		if(x < 0 || x > width || y < 0 || y > height) return;
		synchronized (proc_locker) {
			int rx = x + w;
			if(rx >= width)
				rx = width - 1;
			int by = y + h;
			if(by >= height)
				by = height - 1;
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx = (j + i * width) * 3;
					pixels[_idx] ^= 0xff;
					pixels[_idx + 1] ^= 0xff;
					pixels[_idx + 2] ^= 0xff;
				}
			}
			dirty = true;
		}
	}
	
	/**
	 * 将图片进行透明度处理<br>
	 * 将图片全部区域进行透明度处理<br>
	 * 如果需要对部分区域进行透明度处理则使用{@link #alpha(float, int, int, int, int)}
	 * 
	 * @param alpha 透明度
	 * 
	 * @see #alpha(float, int, int, int, int)
	 */
	public final void alpha(float alpha) {
		if(empty()) return;
		synchronized (proc_locker) {
			for(int i = 0; i < pixels.length; ++i) {
				pixels[i] *= alpha;
			}
			dirty = true;
		}
	}
	
	/**
	 * 将图片进行透明度处理<br>
	 * 将图片部分区域进行透明度处理<br>
	 * 如果区域的右方或下方超出图片宽高则忽略超出部分，但左上方不可超出，如果超出则直接不进行处理<br>
	 * 如果需要对全部区域进行透明度处理则使用{@link #alpha(float)}
	 * 
	 * @param alpha 透明度
	 * 
	 * @param x
	 * 		要处理的区域起始x坐标
	 * @param y
	 * 		要处理的区域起始y坐标
	 * @param w
	 * 		要处理的区域宽度
	 * @param h
	 * 		要处理的区域高度
	 * 
	 * @see #alpha(float)
	 */
	public final void alpha(float alpha, int x, int y, int w, int h) {
		if(empty()) return;
		if(x < 0 || x > width || y < 0 || y > height) return;
		synchronized (proc_locker) {
			int rx = x + w;
			if(rx >= width)
				rx = width - 1;
			int by = y + h;
			if(by >= height)
				by = height - 1;
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx = (j + i * width) * 3;
					pixels[_idx] *= alpha;
					pixels[_idx + 1] *= alpha;
					pixels[_idx + 2] *= alpha;
				}
			}
			dirty = true;
		}
	}
}
