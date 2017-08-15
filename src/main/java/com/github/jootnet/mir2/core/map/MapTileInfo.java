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
package com.github.jootnet.mir2.core.map;

/**
 * MapTile方便程序逻辑的另类解读方式
 * 
 * @author johness
 */
public final class MapTileInfo {

	/** 背景图索引 */
	private short bngImgIdx;
	/** 是否有背景图(在热血传奇2地图中，背景图大小为4个地图块，具体到绘制地图时则表现在只有横纵坐标都为双数时才绘制) */
	private boolean hasBng;
	/** 是否可行走(站立) */
	private boolean canWalk;
	/** 补充背景图索引 */
	private short midImgIdx;
	/** 是否有补充图 */
	private boolean hasMid;
	/** 对象图索引 */
	private short objImgIdx;
	/** 是否有对象图 */
	private boolean hasObj;
	/** 是否可以飞越 */
	private boolean canFly;
	/** 门索引 */
	private byte doorIdx;
	/** 是否有门 */
	private boolean hasDoor;
	/** 门偏移 */
	private byte doorOffset;
	/** 门是否开启 */
	private boolean doorOpen;
	/** 动画帧数 */
	private byte aniFrame;
	/** 是否有动画 */
	private boolean hasAni;
	/** 动画跳帧数 */
	private byte aniTick;
	/** 资源文件索引 */
	private byte objFileIdx;
	/** 光线 */
	private byte light;
	
	MapTileInfo() { }

	/** 获取背景图索引 */
	public short getBngImgIdx() {
		return bngImgIdx;
	}
	/** 设置背景图索引 */
	void setBngImgIdx(short bngImgIdx) {
		this.bngImgIdx = bngImgIdx;
	}
	/** 获取该地图块是否有背景图 */
	public boolean isHasBng() {
		return hasBng;
	}
	/** 设置该地图块是否有背景图 */
	void setHasBng(boolean hasBng) {
		this.hasBng = hasBng;
	}
	/** 获取该地图块是否可以站立或走过 */
	public boolean isCanWalk() {
		return canWalk;
	}
	/** 设置该地图块是否可以站立或走过 */
	void setCanWalk(boolean canWalk) {
		this.canWalk = canWalk;
	}
	/** 获取补充图索引 */
	public short getMidImgIdx() {
		return midImgIdx;
	}
	/** 设置补充图索引 */
	void setMidImgIdx(short midImgIdx) {
		this.midImgIdx = midImgIdx;
	}
	/** 获取该地图块是否有补充图 */
	public boolean isHasMid() {
		return hasMid;
	}
	/** 设置该地图块是否有补充图 */
	void setHasMid(boolean hasMid) {
		this.hasMid = hasMid;
	}
	/** 获取对象图索引 */
	public short getObjImgIdx() {
		return objImgIdx;
	}
	/** 设置对象图索引 */
	void setObjImgIdx(short objImgIdx) {
		this.objImgIdx = objImgIdx;
	}
	/** 获取该地图块是否有对象图 */
	public boolean isHasObj() {
		return hasObj;
	}
	/** 设置该地图块是否有对象图 */
	void setHasObj(boolean hasObj) {
		this.hasObj = hasObj;
	}
	/** 获取该地图块是否可以飞越 */
	public boolean isCanFly() {
		return canFly;
	}
	/** 设置该地图块是否可以飞越 */
	void setCanFly(boolean canFly) {
		this.canFly = canFly;
	}
	/** 获取门索引 */
	public byte getDoorIdx() {
		return doorIdx;
	}
	/** 设置门索引 */
	void setDoorIdx(byte doorIdx) {
		this.doorIdx = doorIdx;
	}
	/** 获取该地图块是否有门 */
	public boolean isHasDoor() {
		return hasDoor;
	}
	/** 设置该地图块是否有门 */
	void setHasDoor(boolean hasDoor) {
		this.hasDoor = hasDoor;
	}
	/** 获取门偏移 */
	public byte getDoorOffset() {
		return doorOffset;
	}
	/** 设置门偏移 */
	void setDoorOffset(byte doorOffset) {
		this.doorOffset = doorOffset;
	}
	/** 获取该地图块门是否打开 */
	public boolean isDoorOpen() {
		return doorOpen;
	}
	/** 设置该地图块门是否打开 */
	void setDoorOpen(boolean doorOpen) {
		this.doorOpen = doorOpen;
	}
	/** 获取动画帧数 */
	public byte getAniFrame() {
		return aniFrame;
	}
	/** 设置动画帧数 */
	void setAniFrame(byte aniFrame) {
		this.aniFrame = aniFrame;
	}
	/** 获取该地图块是否有动画 */
	public boolean isHasAni() {
		return hasAni;
	}
	/** 设置该地图块是否有动画 */
	void setHasAni(boolean hasAni) {
		this.hasAni = hasAni;
	}
	/** 获取动画跳帧数 */
	public byte getAniTick() {
		return aniTick;
	}
	/** 设置动画跳帧数 */
	void setAniTick(byte aniTick) {
		this.aniTick = aniTick;
	}
	/** 获取资源文件索引 */
	public byte getObjFileIdx() {
		return objFileIdx;
	}
	/** 设置资源文件索引 */
	void setObjFileIdx(byte objFileIdx) {
		this.objFileIdx = objFileIdx;
	}
	/** 获取亮度 */
	public byte getLight() {
		return light;
	}
	/** 设置亮度 */
	void setLight(byte light) {
		this.light = light;
	}
}
