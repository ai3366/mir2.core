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

/**
 * 热血传奇2图片信息
 * 
 * @author johness
 */
public final class ImageInfo {

	ImageInfo() { }
	
	private short width;
	private short height;
	private short offsetX;
	private short offsetY;
	
	/**
	 * 获取图片宽度
	 * 
	 * @return 图片宽度,单位为像素
	 */
	public short getWidth() {
		return width;
	}
	void setWidth(short width) {
		this.width = width;
	}

	/**
	 * 获取图片高度
	 * 
	 * @return 图片高度,单位为像素
	 */
	public short getHeight() {
		return height;
	}
	void setHeight(short height) {
		this.height = height;
	}

	/**
	 * 获取图片横向偏移量
	 * 
	 * @return 图片横向偏移量,单位为像素
	 */
	public short getOffsetX() {
		return offsetX;
	}
	void setOffsetX(short offsetX) {
		this.offsetX = offsetX;
	}

	/**
	 * 获取图片纵向偏移量
	 * 
	 * @return 图片纵向偏移量,单位为像素
	 */
	public short getOffsetY() {
		return offsetY;
	}
	void setOffsetY(short offsetY) {
		this.offsetY = offsetY;
	}
}
