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

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 文件二进制读取类
 * <br>
 * 此类继承{@link RandomAccessFile}，添加一系列以<b>LE</b>结尾的函数将读取的字节以<b>Little-Endian</b>格式返回
 * 
 * @author johness
 */
public final class BinaryReader extends RandomAccessFile {

	public BinaryReader(File file, String mode) throws FileNotFoundException {
		super(file, mode);
	}
	
	/**
	 * 从流中读取一个短整形数据
	 * <br>
	 * 流位置向前推进两个字节
	 * 
	 * @return 一个短整形，以Little-Endian格式返回
	 * @throws IOException 文件已达到末尾
	 */
	public final short readShortLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch2 << 8) + (ch1 << 0));
    }
	
	/**
	 * 从流中读取一个无符号短整形数据
	 * <br>
	 * 流位置向前推进两个字节
	 * 
	 * @return 一个无符号短整形，以Little-Endian格式返回
	 * @throws IOException 文件已达到末尾
	 */
	public final int readUnsignedShortLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch2 << 8) + (ch1 << 0);
    }
	
	/**
	 * 从流中读取一个双字节字符
	 * <br>
	 * 流位置向前推进两个字节
	 * 
	 * @return 一个双字节字符，以Little-Endian格式返回
	 * @throws IOException 文件已达到末尾
	 */
	public final char readCharLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch2 << 8) + (ch1 << 0));
    }
	
	/**
	 * 从流中读取一个四字节整形
	 * <br>
	 * 流位置向前推进四个字节
	 * 
	 * @return 一个整形，以Little-Endian格式返回
	 * @throws IOException 文件已达到末尾
	 */
	public final int readIntLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
    }
	
	/**
	 * 从流中读取一个八字节长整形
	 * <br>
	 * 流位置向前推进八个字节
	 * 
	 * @return 一个长整形，以Little-Endian格式返回
	 * @throws IOException 文件已达到末尾
	 */
	public final long readLongLE() throws IOException {
        return ((long)(readIntLE()) & 0xFFFFFFFFL) + (readIntLE() << 32);
    }
	
	/**
	 * 从流中读取一个单精度浮点数
	 * <br>
	 * 流位置向前推进四个字节
	 * 
	 * @return 一个单精度浮点数，以Little-Endian格式返回
	 * @throws IOException 文件已达到末尾
	 */
	public final float readFloatLE() throws IOException {
        return Float.intBitsToFloat(readIntLE());
    }
	
	/**
	 * 从流中读取一个双精度浮点数
	 * <br>
	 * 流位置向前推进八个字节
	 * 
	 * @return 一个双精度浮点数，以Little-Endian格式返回
	 * @throws IOException 文件已达到末尾
	 */
	public final double readDoubleLE() throws IOException {
        return Double.longBitsToDouble(readLongLE());
    }
}
