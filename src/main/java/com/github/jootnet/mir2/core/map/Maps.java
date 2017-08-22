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

import java.io.File;
import java.util.HashMap;

import com.github.jootnet.mir2.core.BinaryReader;

/**
 * 地图管理类<br>
 * 地图文件头以Delphi语言描述如下<br>
 * <pre>
 * TMapHeader = packed record
    wWidth      :Word;                 	//宽度			2
    wHeight     :Word;                 	//高度			2
    sTitle      :String[15]; 			//标题			16
    UpdateDate  :TDateTime;          	//更新日期			8
    VerFlag     :Byte;					//标识(新的格式为02)	1
    Reserved    :array[0..22] of Char;  //保留			23
  end;
 * </pre>
 * 十周年之后的版本可能出现新版本地图，3KM2中式20110428加入的对新版地图的支持<br>
 * 不过除了问陈天桥之外暂时不能知道新版地图中最后两个字节是干嘛用的
 * 
 * @author 云中双月
 */
public final class Maps {

	private static java.util.Map<String, Map> maps = new HashMap<String, Map>();
	private static Object map_locker = new Object();
	
	/**
	 * 获取一个地图对象
	 * 
	 * @param mapNo
	 * 		地图编号<br>
	 * 		用于将地图对象放入系统缓存
	 * @param mapPath
	 * 		地图文件全路径
	 * @return 解析出来的地图对象
	 */
	public static final Map get(String mapNo, String mapPath) {
		synchronized (map_locker) {
			if(maps.containsKey(mapNo))
				return maps.get(mapNo);
			try{
				BinaryReader br_map = new BinaryReader(new File(mapPath), "r");
				Map ret = new Map();
				ret.setWidth(br_map.readShortLE());
				ret.setHeight(br_map.readShortLE());
				br_map.skipBytes(28);
				boolean newMapFlag = br_map.readByte() == 2; // 新版地图每一个Tile占用14个字节，最后的两个字节作用未知
				br_map.skipBytes(23);
				MapTileInfo[][] mapTileInfos = new MapTileInfo[ret.getWidth()][ret.getHeight()];
				for (int width = 0; width < ret.getWidth(); ++width)
					for (int height = 0; height < ret.getHeight(); ++height) {
						MapTileInfo mi = new MapTileInfo();
						// 读取背景
						short bng = br_map.readShortLE();
						// 读取中间层
						short mid = br_map.readShortLE();
						// 读取对象层
						short obj = br_map.readShortLE();
						// 设置背景
						if((bng & 0x7fff) > 0) {
							mi.setBngImgIdx((short) ((bng & 0x7fff) - 1));
							mi.setHasBng(true);
						}
						// 设置中间层
						if((mid & 0x7fff) > 0) {
							mi.setMidImgIdx((short) ((mid & 0x7fff) - 1));
							mi.setHasMid(true);
						}
						// 设置对象层
						if((obj & 0x7fff) > 0) {
							mi.setObjImgIdx((short) ((obj & 0x7fff) - 1));
							mi.setHasObj(true);
						}
						// 设置是否可站立
						mi.setCanWalk((bng & 0x8000) != 0x8000 && (obj & 0x8000) != 0x8000);
						// 设置是否可飞行
						mi.setCanFly((obj & 0x8000) != 0x8000);
						
						// 读取门索引(第7个byte)
						byte btTmp = br_map.readByte();
						if((btTmp & 0x80) == 0x80) {
							mi.setDoorIdx((byte) (btTmp & 0x7F));
							mi.setHasDoor(true);
						}
						// 读取门偏移(第8个byte)
						btTmp = br_map.readByte();
						mi.setDoorOffset(btTmp);
						if((btTmp & 0x80) == 0x80) mi.setDoorOpen(true);
						// 读取动画帧数(第9个byte)
						btTmp = br_map.readByte();
						mi.setAniFrame(btTmp);
						if((btTmp & 0x80) == 0x80) {
							mi.setAniFrame((byte) (btTmp & 0x7F));
							mi.setHasAni(true);
						}
						// 读取并设置动画跳帧数(第10个byte)
						mi.setAniTick(br_map.readByte());
						// 读取资源文件索引(第11个byte)
						mi.setObjFileIdx(br_map.readByte());
						// 读取光照(第12个byte)
						mi.setLight(br_map.readByte());
						if(newMapFlag)
							br_map.skipBytes(2);
						if (width % 2 != 0 || height % 2 != 0)
							mi.setHasBng(false);
						mapTileInfos[width][height] = mi;
					}
				ret.setMapTiles(mapTileInfos);
				br_map.close();
				maps.put(mapNo, ret);
				return ret;
			}catch(Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 * 从缓存在系统的地图集合中移除特定编号的地图
	 * 
	 * @param mapNo
	 * 		地图编号
	 */
	public static final void remove(String mapNo) {
		synchronized (map_locker) {
			if(maps.containsKey(mapNo))
				maps.remove(mapNo);
		}
	}
}
