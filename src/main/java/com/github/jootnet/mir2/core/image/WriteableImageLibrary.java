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
 * Support: https://github.com/jootnet/mir2.core
 */
package com.github.jootnet.mir2.core.image;

/**
 * 可写图片库接口<br>
 * 用于ImageViewer中的新建/编辑操作或微端中的同步功能
 * 
 * @author 云中双月
 */
public interface WriteableImageLibrary extends ImageLibrary {

	/**
	 * 将库文件保存到特定文件夹<br>
	 * 不同库类型生成的文件数目，大小，格式都不同
	 * 
	 * @param dir
	 * 		文件夹路径
	 */
	void save(String dir);
	
	/**
	 * 添加或替换库中一个图片<br>
	 * 24位图片(1600万色)
	 * 
	 * @param index
	 * 		图片索引
	 * @param rgbs
	 * 		图片色彩数据<br>
	 * 		从左至右，从上到下<br>
	 * 		每个像素数据可以是argb或rgb分量，通过给定参数长度自行判断
	 * @param colorBit
	 * 		图片色彩位数，取值为8或16<br>
	 * 		如果图片不是8位或16位BMP，则可能损失精度
	 * @param width
	 * 		图片宽度
	 * @param height
	 * 		图片高度
	 * @param offsetX
	 * 		图片横向偏移
	 * @param offsetY
	 * 		图片纵向偏移
	 */
	void tex(int index, byte[] rgbs, int colorBit, int width, int height, int offsetX, int offsetY);
	
	/**
	 * 添加或替换库中一个图片<br>
	 * 8位图片(256色)
	 * 
	 * @param index
	 * 		图片索引
	 * @param pallete
	 * 		图片调色板<br>
	 * 		必须为256个二维数组，第二维可以是4个字节或三个字节，如果是3个字节则是rgb，四个字节是argb分量<br>
	 * 		新的调色板替换旧的调色板<br>
	 * 		传递空值则使用内置调色板或上次设置的调色板
	 * @param rgb8s
	 * 		图片数据，从左上角到右下角从左至右<br>
	 * 		数据内容为调色板索引，并非完整色彩数据<br>
	 * 		数组长度可以是带填充的(BMP的4字节对齐，不知道的请出门左转百度)，亦可不带
	 * @param width
	 * 		图片宽度
	 * @param height
	 * 		图片高度
	 * @param offsetX
	 * 		图片横向偏移
	 * @param offsetY
	 * 		图片纵向偏移
	 * 
	 * @see #tex(int, int, byte[], int, int, int, int)
	 */
	void tex(int index, byte[][] pallete, byte[] rgb8s, int width, int height, int offsetX, int offsetY);
	
	/**
	 * 添加或替换库中一个图片<br>
	 * 8位图片(256色)
	 * 
	 * @param index
	 * 		图片索引
	 * @param pallete
	 * 		图片调色板<br>
	 * 		必须为256个一维数组，是argb格式的调色板数据<br>
	 * 		新的调色板替换旧的调色板<br>
	 * 		传递空值则使用内置调色板或上次设置的调色板
	 * @param rgb8s
	 * 		图片数据，从左上角到右下角从左至右<br>
	 * 		数据内容为调色板索引，并非完整色彩数据<br>
	 * 		数组长度可以是带填充的(BMP的4字节对齐，不知道的请出门左转百度)，亦可不带
	 * @param width
	 * 		图片宽度
	 * @param height
	 * 		图片高度
	 * @param offsetX
	 * 		图片横向偏移
	 * @param offsetY
	 * 		图片纵向偏移
	 * 
	 * @see #tex(int, byte[][], byte[], int, int, int, int)
	 */
	void tex(int index, int[] pallete, byte[] rgb8s, int width, int height, int offsetX, int offsetY);
	
	/**
	 * 
	 * 添加或替换库中一个图片<br>
	 * 16位图片(65536色)
	 * 
	 * @param index
	 * 		图片索引
	 * @param rgb16s
	 * 		图片数据，从左上角到右下角从左至右<br>
	 * 		数据内容为rgb565，以Little-Endian格式给定<br>
	 * 		数组长度可以是带填充的(BMP的4字节对齐，不知道的请出门左转百度)，亦可不带
	 * @param width
	 * 		图片宽度
	 * @param height
	 * 		图片高度
	 * @param offsetX
	 * 		图片横向偏移
	 * @param offsetY
	 * 		图片纵向偏移
	 */
	void tex(int index, short[] rgb16s, int width, int height, int offsetX, int offsetY);
	
	/**
	 * 删除特定索引的图片<br>
	 * 删除图片只是删除内容，不会改变其他图片索引<br>
	 * 这样利于向后兼容
	 * 
	 * @param index
	 * 		要删除的图片索引
	 */
	void remove(int index);
}
