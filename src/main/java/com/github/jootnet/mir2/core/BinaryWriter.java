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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 文件二进制写入类
 * <br>
 * 此类继承{@link RandomAccessFile}，添加一系列以<b>LE</b>结尾的函数将读取的字节以<b>Little-Endian</b>格式写入
 *
 * @author johness
 */
public class BinaryWriter extends RandomAccessFile {

	public BinaryWriter(File file, String mode) throws FileNotFoundException {
		super(file, mode);
	}

	/**
	 * 以低位在前(Little-Endian)的方式写入两个字节数据(一个short类型数据)
	 * 
	 * @param v
	 * 		要写入的short
	 * @throws IOException
	 * 		写入过程中发生的I/O异常
	 */
	public final void writeShortLE(short v) throws IOException {
		write((v >>> 0) & 0xFF);
        write((v >>> 8) & 0xFF);
	}

	/**
	 * 以低位在前(Little-Endian)的方式写入两个字节数据(一个char类型数据)
	 * 
	 * @param v
	 * 		要写入的char
	 * @throws IOException
	 * 		写入过程中发生的I/O异常
	 */
	public final void writeCharLE(short v) throws IOException {
		write((v >>> 0) & 0xFF);
        write((v >>> 8) & 0xFF);
	}
	
	/**
	 * 以低位在前(Little-Endian)的方式写入四个字节数据(一个int类型数据)
	 * 
	 * @param v
	 * 		要写入的int
	 * @throws IOException
	 * 		写入过程中发生的I/O异常
	 */
	public final void writeIntLE(int v) throws IOException {
        write((v >>>  0) & 0xFF);
        write((v >>>  8) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>> 24) & 0xFF);
    }
	
	/**
	 * 以低位在前(Little-Endian)的方式写入八个字节数据(一个long类型数据)
	 * 
	 * @param v
	 * 		要写入的long
	 * @throws IOException
	 * 		写入过程中发生的I/O异常
	 */
	public final void writeLongLE(long v) throws IOException {
        write((int)(v >>>  0) & 0xFF);
        write((int)(v >>>  8) & 0xFF);
        write((int)(v >>> 16) & 0xFF);
        write((int)(v >>> 24) & 0xFF);
        write((int)(v >>> 32) & 0xFF);
        write((int)(v >>> 40) & 0xFF);
        write((int)(v >>> 48) & 0xFF);
        write((int)(v >>> 56) & 0xFF);
    }
	
	/**
	 * 以低位在前(Little-Endian)的方式写入四个字节数据(一个float类型数据)
	 * 
	 * @param v
	 * 		要写入的float
	 * @throws IOException
	 * 		写入过程中发生的I/O异常
	 */
	public final void writeFloatLE(float v) throws IOException {
        writeIntLE(Float.floatToIntBits(v));
    }
	
	/**
	 * 以低位在前(Little-Endian)的方式写入八个字节数据(一个double类型数据)
	 * 
	 * @param v
	 * 		要写入的double
	 * @throws IOException
	 * 		写入过程中发生的I/O异常
	 */
	public final void writeDoubleLE(double v) throws IOException {
        writeLongLE(Double.doubleToLongBits(v));
    }
}
