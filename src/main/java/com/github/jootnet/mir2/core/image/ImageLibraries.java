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

import java.util.HashMap;
import java.util.Map;

import com.github.jootnet.mir2.core.SDK;

/**
 * 图片库管理工具类
 * 
 * @author johness
 */
public final class ImageLibraries {

	private static Map<String, ImageLibrary> libraries = new HashMap<String, ImageLibrary>();
	private static Object lib_locker = new Object();
	
	/**
	 * 从指定路径中解析出一个图片库并存入内存缓存
	 * 
	 * @param libName
	 * 		图片库名称<br>
	 * 		用于系统缓存的key
	 * @param libPath
	 * 		图片库全路径<br>
	 * 		可以不带后缀，如果未给定后缀则会尝试多种可能
	 * @return 图片库对象
	 */
	public static final ImageLibrary get(String libName, String libPath) {
		synchronized (lib_locker) {
			if(libraries.containsKey(libName))
				return libraries.get(libName);
			try{
				if(SDK.hasFileExtension(libPath)) {
					String ext = SDK.getFileExtension(libPath);
					if(ext.equals("WIL")) {
						WIL wil = new WIL(libPath);
						if(wil.isLoaded()) {
							libraries.put(libName, wil);
							return wil;
						}
					}
					if(ext.equals("WIS")) {
						WIS wis = new WIS(libPath);
						if(wis.isLoaded()) {
							libraries.put(libName, wis);
							return wis;
						}
					}
					if(ext.equals("WZL")) {
						WZL wzl = new WZL(libPath);
						if(wzl.isLoaded()) {
							libraries.put(libName, wzl);
							return wzl;
						}
					}
				} else {
					String wzlPath = SDK.changeFileExtension(libPath, "wzl");
					WZL wzl = new WZL(wzlPath);
					if(wzl.isLoaded()) {
						libraries.put(libName, wzl);
						return wzl;
					}
					String wisPath = SDK.changeFileExtension(libPath, "wis");
					WIS wis = new WIS(wisPath);
					if(wis.isLoaded()) {
						libraries.put(libName, wis);
						return wis;
					}
					String wilPath = SDK.changeFileExtension(libPath, "wil");
					WIL wil = new WIL(wilPath);
					if(wil.isLoaded()) {
						libraries.put(libName, wil);
						return wil;
					}
				}
				return null;
			}catch(RuntimeException ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 * 从缓存在系统的图片库集合中移除特定编号的图片库
	 * 
	 * @param libName
	 * 		图片库编号
	 */
	public static final void remove(String libName) {
		synchronized (lib_locker) {
			if(libraries.containsKey(libName))
				libraries.remove(libName);
		}
	}
}
