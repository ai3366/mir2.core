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
package com.github.jootnet.mir2.core;

import java.awt.Point;
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

	private static int EMPTY_COLOR_INDEX = 0;
	/**
	 * 空图片
	 */
	public static final Texture EMPTY = new Texture(new byte[]{SDK.palletes[EMPTY_COLOR_INDEX][1],SDK.palletes[EMPTY_COLOR_INDEX][2],SDK.palletes[EMPTY_COLOR_INDEX][3]}, 1, 1);
	/**
	 * 空BufferedImage图片
	 */
	public static final BufferedImage EMPTY_BUFFEREDIMAGE;
	
	static {
		EMPTY_BUFFEREDIMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		byte[] cls = ((DataBufferByte)EMPTY_BUFFEREDIMAGE.getRaster().getDataBuffer()).getData();
		cls[0] = SDK.palletes[EMPTY_COLOR_INDEX][0];
		cls[1] = SDK.palletes[EMPTY_COLOR_INDEX][1];
		cls[2] = SDK.palletes[EMPTY_COLOR_INDEX][2];
		cls[3] = SDK.palletes[EMPTY_COLOR_INDEX][3];
	}
	
	private byte[] pixels;
	private int width;
	private int height;
	private volatile boolean dirty;
	
	private boolean emptyHoldFlag;
	private static byte[] emptyPixels;
	private static long clearCount;
	private static Object clear_locker = new Object();
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
		this(sRGB, width, height, true);
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
	 * 
	 * @throws IllegalArgumentException 传入的像素数据长度不符合要求
	 */
	public Texture(byte[] sRGB, int width, int height, boolean emptyHoldFlag) throws IllegalArgumentException {
		if(sRGB != null && width > 0 && height > 0 && sRGB.length != (width * height * 3))
			throw new IllegalArgumentException("sRGB length not match width * height * 3 !!!");
		this.pixels = sRGB;
		this.width = width;
		this.height = height;
		this.emptyHoldFlag = emptyHoldFlag;
	}
	
	/**
	 * 判断当前图片是否为空
	 * 
	 * @return true表示当前图片为空，不可用于任何处理/绘制/序列化
	 */
	public final boolean empty() {
		return this == EMPTY || pixels == null || pixels.length == 0 || width < 1 || height < 1;
	}
	
	/**
	 * 判断当前图片是否被修改过<br>
	 * 当前函数返回之后，图片会被置为未修改，即下次调用会返回false<br>
	 * 一般与{@link #toBufferedImage(boolean)}配合使用，判定时机
	 * 
	 * @return 上次调用此函数之后图片是否被修改过
	 * @see #toBufferedImage(boolean)
	 */
	public final boolean dirty() {
		synchronized (proc_locker) {
			boolean _dirty = dirty;
			dirty = false;
			return _dirty;
		}
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
	 * 将图片数据转换为{@link BufferedImage}对象<br>
	 * 默认不支持Alpha通道，因为从图像算法角度讲是没有“透明色”概念的，只有在两张图片叠加时才有意义<br>
	 * 如果需要在图片中将特定颜色置为透明，则使用{@link #toBufferedImageTransparent(byte, byte, byte)}
	 * 
	 * @param disaposable
	 * 		结果是否是一次性的<br>
	 * 		当此值只为false时返回结果中的BufferedImage中图片数据是与当前对象使用同一个字节数组<br>
	 * 		对当前对象的任何操作都会影响到函数返回的图片展示，甚至可能在多线程中出现图片撕裂<br>
	 * 		因此，除非你认为自己头脑是清晰的，否则请传递true<br>
	 * 		理论上，传递false的函数调用，调用一次和多次效果都是一样的，传递true的调用则需要通过{@link #dirty()}进行时机判断
	 * 
	 * @return 图片数据对应的{@link BufferedImage}对象
	 * 
	 * @see #toBufferedImageTransparent(byte, byte, byte)
	 * @see #dirty()
	 * @see DataBufferByte
	 */
	public final BufferedImage toBufferedImage(boolean disaposable) {
		if(empty())
			return EMPTY_BUFFEREDIMAGE;
		synchronized (proc_locker) {
			byte[] _pixels = null;
			if(!disaposable) {
				_pixels = pixels;
			} else {
				_pixels = new byte[pixels.length];
				System.arraycopy(pixels, 0, _pixels, 0, pixels.length);
			}
			// 将byte[]转为DataBufferByte用于后续创建BufferedImage对象
	        DataBufferByte dataBuffer = new DataBufferByte(_pixels, pixels.length);
	        // sRGB色彩空间对象
	        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	        int[] nBits = {8, 8, 8};
	        int[] bOffs = {0, 1, 2};
	        ComponentColorModel colorModel = new ComponentColorModel(cs, nBits, false, false,
	                                             Transparency.OPAQUE,
	                                             DataBuffer.TYPE_BYTE);        
	        WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width*3, 3, bOffs, null);
	        return new BufferedImage(colorModel,raster,false,null);
		}
	}
	
	/**
	 * 将图片数据转换为{@link BufferedImage}对象<br>
	 * 如果不需要设置透明色，则使用{@link #toBufferedImage(boolean)}<br>
	 * 此函数不会将返回值存入缓存，是一次性的
	 * 
	 * @param r
	 * 		透明色R分量
	 * @param g
	 * 		透明色G分量
	 * @param b
	 * 		透明色B分量
	 * @return 将指定颜色置为透明色的BufferedImage
	 * 
	 * @see #toBufferedImage(boolean)
	 */
	public final BufferedImage toBufferedImageTransparent(byte r, byte g, byte b) {
		if(empty())
			return EMPTY_BUFFEREDIMAGE;
		synchronized (proc_locker) {
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			DataBufferByte dataBuffer = (DataBufferByte)bi.getRaster().getDataBuffer();
			byte[] _pixels = dataBuffer.getData();
			for(int h = 0; h < height; ++h) {
				for(int w = 0; w < width; ++w) {
					byte _r = pixels[(w + h * width) * 3];
					byte _g = pixels[(w + h * width) * 3 + 1];
					byte _b = pixels[(w + h * width) * 3 + 2];
					byte _a = _r == r && _g == g && _b == b ? 0 : (byte)255;
					_pixels[(w + h * width) * 4] = _a;
					_pixels[(w + h * width) * 4 + 1] = _b;
					_pixels[(w + h * width) * 4 + 2] = _g;
					_pixels[(w + h * width) * 4 + 3] = _r;
				}
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
	
	/**
	 * 将一副目标图像混合到当前图像上<br>
	 * 使用普通的图像叠加方式<br>
	 * 即直接使用目标rgb作为新图片的rgb<br>
	 * 如果需要使用Overlay方式，则使用{@link #blendAdd(Texture, Point, float)}方式<br>
	 * 如果需要支持透明色，则使用{@link #blendNormalTransparent(Texture, Point, float, byte, byte, byte)}
	 * 此操作不改变目标图像数据，即使传递了alpha参数
	 * 
	 * @param tar
	 * 		目标图像
	 * @param loc
	 * 		图像叠加起始坐标
	 * @param alpha
	 * 		目标图像透明度
	 * 
	 * @see #blendAdd(Texture, Point, float)
	 * @see #blendAddTransparent(Texture, Point, float, byte, byte, byte)
	 * @see #blendNormalTransparent(Texture, Point, float, byte, byte, byte)
	 */
	public final void blendNormal(Texture tar, Point loc, float alpha) {
		if(empty()) return;
		if(tar.empty()) return;
		synchronized (proc_locker) {
			int x = loc.x;
			int y = loc.y;
			if(x < 0 || x > width || y < 0 || y < height) return;
			int rx = x + tar.width;
			if(rx >= width)
				rx = width - 1;
			int by = y + tar.height;
			if(by >= height)
				by = height - 1;
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx_this = (j + i * width) * 3;
					int _idx_that = (j - x + (i - y) * width) * 3;
					pixels[_idx_this] = (byte) (tar.pixels[_idx_that] * alpha);
					pixels[_idx_this + 1] = (byte) (tar.pixels[_idx_that + 1] * alpha);
					pixels[_idx_this + 2] = (byte) (tar.pixels[_idx_that + 2] * alpha);
				}
			}
			dirty = true;
		}
	}
	
	/**
	 * 将一副目标图像混合到当前图像上<br>
	 * 使用普通的图像叠加方式<br>
	 * 即直接使用目标rgb作为新图片的rgb<br>
	 * 如果需要使用Overlay方式，则使用{@link #blendAddTransparent(Texture, Point, float, byte, byte, byte)}方式<br>
	 * 此操作不改变目标图像数据，即使传递了alpha参数<br>
	 * 支持透明色，即如果目标坐标目标图片的颜色是给定值则忽略
	 * 
	 * @param tar
	 * 		目标图像
	 * @param loc
	 * 		图像叠加起始坐标
	 * @param alpha
	 * 		目标图像透明度
	 * @param r
	 * 		透明色R分量
	 * @param g
	 * 		透明色分量
	 * @param b
	 * 		透明色分量
	 * 
	 * @see #blendAdd(Texture, Point, float)
	 * @see #blendAddTransparent(Texture, Point, float, byte, byte, byte)
	 * @see #blendNormal(Texture, Point, float)
	 */
	public final void blendNormalTransparent(Texture tar, Point loc, float alpha, byte r, byte g, byte b) {
		if(empty()) return;
		if(tar.empty()) return;
		synchronized (proc_locker) {
			int x = loc.x;
			int y = loc.y;
			if(x < 0 || x > width || y < 0 || y < height) return;
			int rx = x + tar.width;
			if(rx >= width)
				rx = width - 1;
			int by = y + tar.height;
			if(by >= height)
				by = height - 1;
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx_this = (j + i * width) * 3;
					int _idx_that = (j - x + (i - y) * width) * 3;
					byte _r = tar.pixels[_idx_that];
					byte _g = tar.pixels[_idx_that + 1];
					byte _b = tar.pixels[_idx_that + 2];
					if(r != _r || _g != g || _b != b) {
						pixels[_idx_this] = (byte) (_r * alpha);
						pixels[_idx_this + 1] = (byte) (_g * alpha);
						pixels[_idx_this + 2] = (byte) (_b * alpha);
					}
				}
			}
			dirty = true;
		}
	}
	
	/**
	 * 将一副目标图像混合到当前图像上<br>
	 * 使用Overlay的图像叠加方式<br>
	 * 即显卡的Add混合模式，在OpenGL里是glBlendFunc(GL_SRC_COLOR, GL_ONE)<br>
	 * 如果需要使用普通方式，则使用{@link #blendNormal(Texture, Point, float)}方式<br>
	 * 如需支持透明色，则使用{@link #blendNormalTransparent(Texture, Point, float, byte, byte, byte)}
	 * 此操作不改变目标图像数据，即使传递了alpha参数
	 * 
	 * @param tar
	 * 		目标图像
	 * @param loc
	 * 		图像叠加起始坐标
	 * @param alpha
	 * 		目标图像透明度
	 * 
	 * @see #blendNormal(Texture, Point, float)
	 * @see #blendNormalTransparent(Texture, Point, float, byte, byte, byte)
	 * @see #blendAddTransparent(Texture, Point, float, byte, byte, byte)
	 */
	public final void blendAdd(Texture tar, Point loc, float alpha) {
		if(empty()) return;
		if(tar.empty()) return;
		synchronized (proc_locker) {
			int x = loc.x;
			int y = loc.y;
			if(x < 0 || x > width || y < 0 || y < height) return;
			int rx = x + tar.width;
			if(rx >= width)
				rx = width - 1;
			int by = y + tar.height;
			if(by >= height)
				by = height - 1;
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx_this = (j + i * width) * 3;
					int _idx_that = (j - x + (i - y) * width) * 3;
					byte r = (byte) (tar.pixels[_idx_that] * alpha);
					byte g = (byte) (tar.pixels[_idx_that + 1] * alpha);
					byte b = (byte) (tar.pixels[_idx_that + 2] * alpha);
					pixels[_idx_this] = (byte) ((r < 128) ? (2 * pixels[_idx_this] * r / 255) : (255 - 2 * (255 - pixels[_idx_this]) * (255 - r) / 255));
					pixels[_idx_this + 1] = (byte) ((g < 128) ? (2 * pixels[_idx_this + 1] * g / 255) : (255 - 2 * (255 - pixels[_idx_this + 1]) * (255 - g) / 255));
					pixels[_idx_this + 2] = (byte) ((b < 128) ? (2 * pixels[_idx_this + 2] * b / 255) : (255 - 2 * (255 - pixels[_idx_this + 2]) * (255 - b) / 255));
				}
			}
		}
		dirty = true;
	}
	
	/**
	 * 将一副目标图像混合到当前图像上<br>
	 * 使用Overlay的图像叠加方式<br>
	 * 即显卡的Add混合模式，在OpenGL里是glBlendFunc(GL_SRC_COLOR, GL_ONE)<br>
	 * 如果需要使用普通方式，则使用{@link #blendNormalTransparent(Texture, Point, float, byte, byte, byte)}方式<br>
	 * 此操作不改变目标图像数据，即使传递了alpha参数<br>
	 * 支持透明色，即如果目标坐标目标图片的颜色是给定值则忽略
	 * 
	 * @param tar
	 * 		目标图像
	 * @param loc
	 * 		图像叠加起始坐标
	 * @param alpha
	 * 		目标图像透明度
	 * @param r
	 * 		透明色R分量
	 * @param g
	 * 		透明色分量
	 * @param b
	 * 		透明色分量
	 * 
	 * @see #blendNormal(Texture, Point, float)
	 * @see #blendNormalTransparent(Texture, Point, float, byte, byte, byte)
	 * @see #blendAdd(Texture, Point, float)
	 */
	public final void blendAddTransparent(Texture tar, Point loc, float alpha, byte r, byte g, byte b) {
		if(empty()) return;
		if(tar.empty()) return;
		synchronized (proc_locker) {
			int x = loc.x;
			int y = loc.y;
			if(x < 0 || x > width || y < 0 || y < height) return;
			int rx = x + tar.width;
			if(rx >= width)
				rx = width - 1;
			int by = y + tar.height;
			if(by >= height)
				by = height - 1;
			for(int i = y; i < by; ++i) {
				for(int j = x; j < rx; ++j) {
					int _idx_this = (j + i * width) * 3;
					int _idx_that = (j - x + (i - y) * width) * 3;
					byte _r = (byte) (tar.pixels[_idx_that] * alpha);
					byte _g = (byte) (tar.pixels[_idx_that + 1] * alpha);
					byte _b = (byte) (tar.pixels[_idx_that + 2] * alpha);
					if(r != _r || _g != g || _b != b) {
						pixels[_idx_this] = (byte) ((_r < 128) ? (2 * pixels[_idx_this] * _r / 255) : (255 - 2 * (255 - pixels[_idx_this]) * (255 - _r) / 255));
						pixels[_idx_this + 1] = (byte) ((_g < 128) ? (2 * pixels[_idx_this + 1] * _g / 255) : (255 - 2 * (255 - pixels[_idx_this + 1]) * (255 - _g) / 255));
						pixels[_idx_this + 2] = (byte) ((_b < 128) ? (2 * pixels[_idx_this + 2] * _b / 255) : (255 - 2 * (255 - pixels[_idx_this + 2]) * (255 - _b) / 255));
					}
				}
			}
		}
		dirty = true;
	}
}
