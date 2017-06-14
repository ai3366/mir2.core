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

import java.awt.image.BufferedImage;

/**
 * 可写图片库接口<br>
 * 用于ImageViewer中的新建/编辑操作或微端中的同步功能
 * 
 * @author johness
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
	 * 添加或替换库中一个图片
	 * 
	 * @param index
	 * 		图片索引
	 * @param image
	 * 		图片对象
	 * @param colorBit
	 * 		图片色彩位数，取值为8或16<br>
	 * 		如果图片不是8位或16位BMP，则可能损失精度
	 * @param offsetX
	 * 		图片横向偏移
	 * @param offsetY
	 * 		图片纵向偏移
	 */
	void tex(int index, BufferedImage image, int colorBit, int offsetX, int offsetY);
	
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
