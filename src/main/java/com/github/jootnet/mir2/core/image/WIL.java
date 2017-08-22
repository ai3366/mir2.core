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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import com.github.jootnet.mir2.core.BinaryReader;
import com.github.jootnet.mir2.core.BinaryWriter;
import com.github.jootnet.mir2.core.SDK;
import com.github.jootnet.mir2.core.Texture;

/**
 * 热血传奇2WIL图片库
 * 
 * @author 云中双月
 */
public final class WIL implements WriteableImageLibrary {

	// wilTitle
	private static final byte[] wilTitle = { (byte) 0x23, (byte) 0x49, (byte) 0x4C, (byte) 0x49, (byte) 0x42,
			(byte) 0x20, (byte) 0x76, (byte) 0x31, (byte) 0x2E, (byte) 0x30, (byte) 0x2D, (byte) 0x57, (byte) 0x45,
			(byte) 0x4D, (byte) 0x41, (byte) 0x44, (byte) 0x45, (byte) 0x20, (byte) 0x45, (byte) 0x6E, (byte) 0x74,
			(byte) 0x65, (byte) 0x72, (byte) 0x74, (byte) 0x61, (byte) 0x69, (byte) 0x6E, (byte) 0x6D, (byte) 0x65,
			(byte) 0x6E, (byte) 0x74, (byte) 0x20, (byte) 0x69, (byte) 0x6E, (byte) 0x63, (byte) 0x2E, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x29, (byte) 0x3D, (byte) 0x40, (byte) 0x00 };

	// wixTitle
	private static final byte[] wixTitle = { (byte) 0x23, (byte) 0x49, (byte) 0x4E, (byte) 0x44, (byte) 0x58,
			(byte) 0x20, (byte) 0x76, (byte) 0x31, (byte) 0x2E, (byte) 0x30, (byte) 0x2D, (byte) 0x57, (byte) 0x45,
			(byte) 0x4D, (byte) 0x41, (byte) 0x44, (byte) 0x45, (byte) 0x20, (byte) 0x45, (byte) 0x6E, (byte) 0x74,
			(byte) 0x65, (byte) 0x72, (byte) 0x74, (byte) 0x61, (byte) 0x69, (byte) 0x6E, (byte) 0x6D, (byte) 0x65,
			(byte) 0x6E, (byte) 0x74, (byte) 0x20, (byte) 0x69, (byte) 0x6E, (byte) 0x63, (byte) 0x2E, (byte) 0x08,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6C, (byte) 0x5F, (byte) 0x1F, (byte) 0x01 };

	// 调色板
	private static final byte[] globalPallete = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x80, (byte) 0x80, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80,
			(byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x80, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xC0,
			(byte) 0xC0, (byte) 0xC0, (byte) 0x00, (byte) 0x97, (byte) 0x80, (byte) 0x55, (byte) 0x00, (byte) 0xC8,
			(byte) 0xB9, (byte) 0x9D, (byte) 0x00, (byte) 0x73, (byte) 0x73, (byte) 0x7B, (byte) 0x00, (byte) 0x29,
			(byte) 0x29, (byte) 0x2D, (byte) 0x00, (byte) 0x52, (byte) 0x52, (byte) 0x5A, (byte) 0x00, (byte) 0x5A,
			(byte) 0x5A, (byte) 0x63, (byte) 0x00, (byte) 0x39, (byte) 0x39, (byte) 0x42, (byte) 0x00, (byte) 0x18,
			(byte) 0x18, (byte) 0x1D, (byte) 0x00, (byte) 0x10, (byte) 0x10, (byte) 0x18, (byte) 0x00, (byte) 0x18,
			(byte) 0x18, (byte) 0x29, (byte) 0x00, (byte) 0x08, (byte) 0x08, (byte) 0x10, (byte) 0x00, (byte) 0x71,
			(byte) 0x79, (byte) 0xF2, (byte) 0x00, (byte) 0x5F, (byte) 0x67, (byte) 0xE1, (byte) 0x00, (byte) 0x5A,
			(byte) 0x5A, (byte) 0xFF, (byte) 0x00, (byte) 0x31, (byte) 0x31, (byte) 0xFF, (byte) 0x00, (byte) 0x52,
			(byte) 0x5A, (byte) 0xD6, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x94, (byte) 0x00, (byte) 0x18,
			(byte) 0x29, (byte) 0x94, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x39, (byte) 0x00, (byte) 0x00,
			(byte) 0x10, (byte) 0x73, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0xB5, (byte) 0x00, (byte) 0x52,
			(byte) 0x63, (byte) 0xBD, (byte) 0x00, (byte) 0x10, (byte) 0x18, (byte) 0x42, (byte) 0x00, (byte) 0x99,
			(byte) 0xAA, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x5A, (byte) 0x00, (byte) 0x29,
			(byte) 0x39, (byte) 0x73, (byte) 0x00, (byte) 0x31, (byte) 0x4A, (byte) 0xA5, (byte) 0x00, (byte) 0x73,
			(byte) 0x7B, (byte) 0x94, (byte) 0x00, (byte) 0x31, (byte) 0x52, (byte) 0xBD, (byte) 0x00, (byte) 0x10,
			(byte) 0x21, (byte) 0x52, (byte) 0x00, (byte) 0x18, (byte) 0x31, (byte) 0x7B, (byte) 0x00, (byte) 0x10,
			(byte) 0x18, (byte) 0x2D, (byte) 0x00, (byte) 0x31, (byte) 0x4A, (byte) 0x8C, (byte) 0x00, (byte) 0x00,
			(byte) 0x29, (byte) 0x94, (byte) 0x00, (byte) 0x00, (byte) 0x31, (byte) 0xBD, (byte) 0x00, (byte) 0x52,
			(byte) 0x73, (byte) 0xC6, (byte) 0x00, (byte) 0x18, (byte) 0x31, (byte) 0x6B, (byte) 0x00, (byte) 0x42,
			(byte) 0x6B, (byte) 0xC6, (byte) 0x00, (byte) 0x00, (byte) 0x4A, (byte) 0xCE, (byte) 0x00, (byte) 0x39,
			(byte) 0x63, (byte) 0xA5, (byte) 0x00, (byte) 0x18, (byte) 0x31, (byte) 0x5A, (byte) 0x00, (byte) 0x00,
			(byte) 0x10, (byte) 0x2A, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x15, (byte) 0x00, (byte) 0x00,
			(byte) 0x18, (byte) 0x3A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x29, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4A, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x9D, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xDC, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0xDE, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFB, (byte) 0x00, (byte) 0x52,
			(byte) 0x73, (byte) 0x9C, (byte) 0x00, (byte) 0x4A, (byte) 0x6B, (byte) 0x94, (byte) 0x00, (byte) 0x29,
			(byte) 0x4A, (byte) 0x73, (byte) 0x00, (byte) 0x18, (byte) 0x31, (byte) 0x52, (byte) 0x00, (byte) 0x18,
			(byte) 0x4A, (byte) 0x8C, (byte) 0x00, (byte) 0x11, (byte) 0x44, (byte) 0x88, (byte) 0x00, (byte) 0x00,
			(byte) 0x21, (byte) 0x4A, (byte) 0x00, (byte) 0x10, (byte) 0x18, (byte) 0x21, (byte) 0x00, (byte) 0x5A,
			(byte) 0x94, (byte) 0xD6, (byte) 0x00, (byte) 0x21, (byte) 0x6B, (byte) 0xC6, (byte) 0x00, (byte) 0x00,
			(byte) 0x6B, (byte) 0xEF, (byte) 0x00, (byte) 0x00, (byte) 0x77, (byte) 0xFF, (byte) 0x00, (byte) 0x84,
			(byte) 0x94, (byte) 0xA5, (byte) 0x00, (byte) 0x21, (byte) 0x31, (byte) 0x42, (byte) 0x00, (byte) 0x08,
			(byte) 0x10, (byte) 0x18, (byte) 0x00, (byte) 0x08, (byte) 0x18, (byte) 0x29, (byte) 0x00, (byte) 0x00,
			(byte) 0x10, (byte) 0x21, (byte) 0x00, (byte) 0x18, (byte) 0x29, (byte) 0x39, (byte) 0x00, (byte) 0x39,
			(byte) 0x63, (byte) 0x8C, (byte) 0x00, (byte) 0x10, (byte) 0x29, (byte) 0x42, (byte) 0x00, (byte) 0x18,
			(byte) 0x42, (byte) 0x6B, (byte) 0x00, (byte) 0x18, (byte) 0x4A, (byte) 0x7B, (byte) 0x00, (byte) 0x00,
			(byte) 0x4A, (byte) 0x94, (byte) 0x00, (byte) 0x7B, (byte) 0x84, (byte) 0x8C, (byte) 0x00, (byte) 0x5A,
			(byte) 0x63, (byte) 0x6B, (byte) 0x00, (byte) 0x39, (byte) 0x42, (byte) 0x4A, (byte) 0x00, (byte) 0x18,
			(byte) 0x21, (byte) 0x29, (byte) 0x00, (byte) 0x29, (byte) 0x39, (byte) 0x46, (byte) 0x00, (byte) 0x94,
			(byte) 0xA5, (byte) 0xB5, (byte) 0x00, (byte) 0x5A, (byte) 0x6B, (byte) 0x7B, (byte) 0x00, (byte) 0x94,
			(byte) 0xB1, (byte) 0xCE, (byte) 0x00, (byte) 0x73, (byte) 0x8C, (byte) 0xA5, (byte) 0x00, (byte) 0x5A,
			(byte) 0x73, (byte) 0x8C, (byte) 0x00, (byte) 0x73, (byte) 0x94, (byte) 0xB5, (byte) 0x00, (byte) 0x73,
			(byte) 0xA5, (byte) 0xD6, (byte) 0x00, (byte) 0x4A, (byte) 0xA5, (byte) 0xEF, (byte) 0x00, (byte) 0x8C,
			(byte) 0xC6, (byte) 0xEF, (byte) 0x00, (byte) 0x42, (byte) 0x63, (byte) 0x7B, (byte) 0x00, (byte) 0x39,
			(byte) 0x56, (byte) 0x6B, (byte) 0x00, (byte) 0x5A, (byte) 0x94, (byte) 0xBD, (byte) 0x00, (byte) 0x00,
			(byte) 0x39, (byte) 0x63, (byte) 0x00, (byte) 0xAD, (byte) 0xC6, (byte) 0xD6, (byte) 0x00, (byte) 0x29,
			(byte) 0x42, (byte) 0x52, (byte) 0x00, (byte) 0x18, (byte) 0x63, (byte) 0x94, (byte) 0x00, (byte) 0xAD,
			(byte) 0xD6, (byte) 0xEF, (byte) 0x00, (byte) 0x63, (byte) 0x8C, (byte) 0xA5, (byte) 0x00, (byte) 0x4A,
			(byte) 0x5A, (byte) 0x63, (byte) 0x00, (byte) 0x7B, (byte) 0xA5, (byte) 0xBD, (byte) 0x00, (byte) 0x18,
			(byte) 0x42, (byte) 0x5A, (byte) 0x00, (byte) 0x31, (byte) 0x8C, (byte) 0xBD, (byte) 0x00, (byte) 0x29,
			(byte) 0x31, (byte) 0x35, (byte) 0x00, (byte) 0x63, (byte) 0x84, (byte) 0x94, (byte) 0x00, (byte) 0x4A,
			(byte) 0x6B, (byte) 0x7B, (byte) 0x00, (byte) 0x5A, (byte) 0x8C, (byte) 0xA5, (byte) 0x00, (byte) 0x29,
			(byte) 0x4A, (byte) 0x5A, (byte) 0x00, (byte) 0x39, (byte) 0x7B, (byte) 0x9C, (byte) 0x00, (byte) 0x10,
			(byte) 0x31, (byte) 0x42, (byte) 0x00, (byte) 0x21, (byte) 0xAD, (byte) 0xEF, (byte) 0x00, (byte) 0x00,
			(byte) 0x10, (byte) 0x18, (byte) 0x00, (byte) 0x00, (byte) 0x21, (byte) 0x29, (byte) 0x00, (byte) 0x00,
			(byte) 0x6B, (byte) 0x9C, (byte) 0x00, (byte) 0x5A, (byte) 0x84, (byte) 0x94, (byte) 0x00, (byte) 0x18,
			(byte) 0x42, (byte) 0x52, (byte) 0x00, (byte) 0x29, (byte) 0x5A, (byte) 0x6B, (byte) 0x00, (byte) 0x21,
			(byte) 0x63, (byte) 0x7B, (byte) 0x00, (byte) 0x21, (byte) 0x7B, (byte) 0x9C, (byte) 0x00, (byte) 0x00,
			(byte) 0xA5, (byte) 0xDE, (byte) 0x00, (byte) 0x39, (byte) 0x52, (byte) 0x5A, (byte) 0x00, (byte) 0x10,
			(byte) 0x29, (byte) 0x31, (byte) 0x00, (byte) 0x7B, (byte) 0xBD, (byte) 0xCE, (byte) 0x00, (byte) 0x39,
			(byte) 0x5A, (byte) 0x63, (byte) 0x00, (byte) 0x4A, (byte) 0x84, (byte) 0x94, (byte) 0x00, (byte) 0x29,
			(byte) 0xA5, (byte) 0xC6, (byte) 0x00, (byte) 0x18, (byte) 0x9C, (byte) 0x10, (byte) 0x00, (byte) 0x4A,
			(byte) 0x8C, (byte) 0x42, (byte) 0x00, (byte) 0x42, (byte) 0x8C, (byte) 0x31, (byte) 0x00, (byte) 0x29,
			(byte) 0x94, (byte) 0x10, (byte) 0x00, (byte) 0x10, (byte) 0x18, (byte) 0x08, (byte) 0x00, (byte) 0x18,
			(byte) 0x18, (byte) 0x08, (byte) 0x00, (byte) 0x10, (byte) 0x29, (byte) 0x08, (byte) 0x00, (byte) 0x29,
			(byte) 0x42, (byte) 0x18, (byte) 0x00, (byte) 0xAD, (byte) 0xB5, (byte) 0xA5, (byte) 0x00, (byte) 0x73,
			(byte) 0x73, (byte) 0x6B, (byte) 0x00, (byte) 0x29, (byte) 0x29, (byte) 0x18, (byte) 0x00, (byte) 0x4A,
			(byte) 0x42, (byte) 0x18, (byte) 0x00, (byte) 0x4A, (byte) 0x42, (byte) 0x31, (byte) 0x00, (byte) 0xDE,
			(byte) 0xC6, (byte) 0x63, (byte) 0x00, (byte) 0xFF, (byte) 0xDD, (byte) 0x44, (byte) 0x00, (byte) 0xEF,
			(byte) 0xD6, (byte) 0x8C, (byte) 0x00, (byte) 0x39, (byte) 0x6B, (byte) 0x73, (byte) 0x00, (byte) 0x39,
			(byte) 0xDE, (byte) 0xF7, (byte) 0x00, (byte) 0x8C, (byte) 0xEF, (byte) 0xF7, (byte) 0x00, (byte) 0x00,
			(byte) 0xE7, (byte) 0xF7, (byte) 0x00, (byte) 0x5A, (byte) 0x6B, (byte) 0x6B, (byte) 0x00, (byte) 0xA5,
			(byte) 0x8C, (byte) 0x5A, (byte) 0x00, (byte) 0xEF, (byte) 0xB5, (byte) 0x39, (byte) 0x00, (byte) 0xCE,
			(byte) 0x9C, (byte) 0x4A, (byte) 0x00, (byte) 0xB5, (byte) 0x84, (byte) 0x31, (byte) 0x00, (byte) 0x6B,
			(byte) 0x52, (byte) 0x31, (byte) 0x00, (byte) 0xD6, (byte) 0xDE, (byte) 0xDE, (byte) 0x00, (byte) 0xB5,
			(byte) 0xBD, (byte) 0xBD, (byte) 0x00, (byte) 0x84, (byte) 0x8C, (byte) 0x8C, (byte) 0x00, (byte) 0xDE,
			(byte) 0xF7, (byte) 0xF7, (byte) 0x00, (byte) 0x18, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x39,
			(byte) 0x18, (byte) 0x08, (byte) 0x00, (byte) 0x29, (byte) 0x10, (byte) 0x08, (byte) 0x00, (byte) 0x00,
			(byte) 0x18, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x29, (byte) 0x08, (byte) 0x00, (byte) 0xA5,
			(byte) 0x52, (byte) 0x00, (byte) 0x00, (byte) 0xDE, (byte) 0x7B, (byte) 0x00, (byte) 0x00, (byte) 0x4A,
			(byte) 0x29, (byte) 0x10, (byte) 0x00, (byte) 0x6B, (byte) 0x39, (byte) 0x10, (byte) 0x00, (byte) 0x8C,
			(byte) 0x52, (byte) 0x10, (byte) 0x00, (byte) 0xA5, (byte) 0x5A, (byte) 0x21, (byte) 0x00, (byte) 0x5A,
			(byte) 0x31, (byte) 0x10, (byte) 0x00, (byte) 0x84, (byte) 0x42, (byte) 0x10, (byte) 0x00, (byte) 0x84,
			(byte) 0x52, (byte) 0x31, (byte) 0x00, (byte) 0x31, (byte) 0x21, (byte) 0x18, (byte) 0x00, (byte) 0x7B,
			(byte) 0x5A, (byte) 0x4A, (byte) 0x00, (byte) 0xA5, (byte) 0x6B, (byte) 0x52, (byte) 0x00, (byte) 0x63,
			(byte) 0x39, (byte) 0x29, (byte) 0x00, (byte) 0xDE, (byte) 0x4A, (byte) 0x10, (byte) 0x00, (byte) 0x21,
			(byte) 0x29, (byte) 0x29, (byte) 0x00, (byte) 0x39, (byte) 0x4A, (byte) 0x4A, (byte) 0x00, (byte) 0x18,
			(byte) 0x29, (byte) 0x29, (byte) 0x00, (byte) 0x29, (byte) 0x4A, (byte) 0x4A, (byte) 0x00, (byte) 0x42,
			(byte) 0x7B, (byte) 0x7B, (byte) 0x00, (byte) 0x4A, (byte) 0x9C, (byte) 0x9C, (byte) 0x00, (byte) 0x29,
			(byte) 0x5A, (byte) 0x5A, (byte) 0x00, (byte) 0x14, (byte) 0x42, (byte) 0x42, (byte) 0x00, (byte) 0x00,
			(byte) 0x39, (byte) 0x39, (byte) 0x00, (byte) 0x00, (byte) 0x59, (byte) 0x59, (byte) 0x00, (byte) 0x2C,
			(byte) 0x35, (byte) 0xCA, (byte) 0x00, (byte) 0x21, (byte) 0x73, (byte) 0x6B, (byte) 0x00, (byte) 0x00,
			(byte) 0x31, (byte) 0x29, (byte) 0x00, (byte) 0x10, (byte) 0x39, (byte) 0x31, (byte) 0x00, (byte) 0x18,
			(byte) 0x39, (byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x4A, (byte) 0x42, (byte) 0x00, (byte) 0x18,
			(byte) 0x63, (byte) 0x52, (byte) 0x00, (byte) 0x29, (byte) 0x73, (byte) 0x5A, (byte) 0x00, (byte) 0x18,
			(byte) 0x4A, (byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x21, (byte) 0x18, (byte) 0x00, (byte) 0x00,
			(byte) 0x31, (byte) 0x18, (byte) 0x00, (byte) 0x10, (byte) 0x39, (byte) 0x18, (byte) 0x00, (byte) 0x4A,
			(byte) 0x84, (byte) 0x63, (byte) 0x00, (byte) 0x4A, (byte) 0xBD, (byte) 0x6B, (byte) 0x00, (byte) 0x4A,
			(byte) 0xB5, (byte) 0x63, (byte) 0x00, (byte) 0x4A, (byte) 0xBD, (byte) 0x63, (byte) 0x00, (byte) 0x4A,
			(byte) 0x9C, (byte) 0x5A, (byte) 0x00, (byte) 0x39, (byte) 0x8C, (byte) 0x4A, (byte) 0x00, (byte) 0x4A,
			(byte) 0xC6, (byte) 0x63, (byte) 0x00, (byte) 0x4A, (byte) 0xD6, (byte) 0x63, (byte) 0x00, (byte) 0x4A,
			(byte) 0x84, (byte) 0x52, (byte) 0x00, (byte) 0x29, (byte) 0x73, (byte) 0x31, (byte) 0x00, (byte) 0x5A,
			(byte) 0xC6, (byte) 0x63, (byte) 0x00, (byte) 0x4A, (byte) 0xBD, (byte) 0x52, (byte) 0x00, (byte) 0x00,
			(byte) 0xFF, (byte) 0x10, (byte) 0x00, (byte) 0x18, (byte) 0x29, (byte) 0x18, (byte) 0x00, (byte) 0x4A,
			(byte) 0x88, (byte) 0x4A, (byte) 0x00, (byte) 0x4A, (byte) 0xE7, (byte) 0x4A, (byte) 0x00, (byte) 0x00,
			(byte) 0x5A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x88, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x94, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xDE, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0xEE, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFB, (byte) 0x00, (byte) 0x00, (byte) 0x94,
			(byte) 0x5A, (byte) 0x4A, (byte) 0x00, (byte) 0xB5, (byte) 0x73, (byte) 0x63, (byte) 0x00, (byte) 0xD6,
			(byte) 0x8C, (byte) 0x7B, (byte) 0x00, (byte) 0xD6, (byte) 0x7B, (byte) 0x6B, (byte) 0x00, (byte) 0xFF,
			(byte) 0x88, (byte) 0x77, (byte) 0x00, (byte) 0xCE, (byte) 0xC6, (byte) 0xC6, (byte) 0x00, (byte) 0x9C,
			(byte) 0x94, (byte) 0x94, (byte) 0x00, (byte) 0xC6, (byte) 0x94, (byte) 0x9C, (byte) 0x00, (byte) 0x39,
			(byte) 0x31, (byte) 0x31, (byte) 0x00, (byte) 0x84, (byte) 0x18, (byte) 0x29, (byte) 0x00, (byte) 0x84,
			(byte) 0x00, (byte) 0x18, (byte) 0x00, (byte) 0x52, (byte) 0x42, (byte) 0x4A, (byte) 0x00, (byte) 0x7B,
			(byte) 0x42, (byte) 0x52, (byte) 0x00, (byte) 0x73, (byte) 0x5A, (byte) 0x63, (byte) 0x00, (byte) 0xF7,
			(byte) 0xB5, (byte) 0xCE, (byte) 0x00, (byte) 0x9C, (byte) 0x7B, (byte) 0x8C, (byte) 0x00, (byte) 0xCC,
			(byte) 0x22, (byte) 0x77, (byte) 0x00, (byte) 0xFF, (byte) 0xAA, (byte) 0xDD, (byte) 0x00, (byte) 0x2A,
			(byte) 0xB4, (byte) 0xF0, (byte) 0x00, (byte) 0x9F, (byte) 0x00, (byte) 0xDF, (byte) 0x00, (byte) 0xB3,
			(byte) 0x17, (byte) 0xE3, (byte) 0x00, (byte) 0xF0, (byte) 0xFB, (byte) 0xFF, (byte) 0x00, (byte) 0xA4,
			(byte) 0xA0, (byte) 0xA0, (byte) 0x00, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
			(byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0x00 };

	// 图片库名称
	private String libName;
	// 色深度
	private int bitCount;
	// 当前图片库使用的调色板
	private byte[] currentUsePallete = globalPallete;

	/**
	 * 是否只用WIL中的数据解析图片，而不看WIX的内容
	 */
	public volatile static boolean GLOBAL_ONLYWIL_MODE = true;

	private int imageCount;

	/**
	 * 获取库中图片数量
	 * 
	 * @return 存在于当前WIL库中的图片数量
	 */
	int getImageCount() {
		return imageCount;
	}

	/*
	 * 版本标识 private int verFlag;
	 */
	/* 图片数据起始位置 */
	private int[] offsetList;
	private ImageInfo[] imageInfos;

	/**
	 * 获取库中图片信息数组
	 * 
	 * @return 所有存在于当前WIL库中的图片信息数组
	 */
	ImageInfo[] getImageInfos() {
		return imageInfos;
	}

	/* WIL文件随机读取对象 */
	private BinaryReader br_wil;
	/* 临时文件夹 */
	private File tmp_wil_dir;
	private volatile boolean loaded;

	/**
	 * 获取库加载状态
	 * 
	 * @return true表示库加载成功 false表示加载失败
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/* 文件指针读取锁 */
	private Object wil_locker = new Object();

	WIL(String wilPath) {
		File f_wil = new File(wilPath);
		if (!f_wil.exists())
			return;
		if (!f_wil.isFile())
			return;
		if (!f_wil.canRead())
			return;
		try {
			String wixPath = SDK.changeFileExtension(wilPath, "wix");
			boolean wilOnlyMode = false;
			File f_wix = new File(wixPath);
			if (GLOBAL_ONLYWIL_MODE || !f_wix.exists()) {
				wilOnlyMode = true;
			}
			if (!wilOnlyMode && !f_wix.isFile())
				return;
			if (!wilOnlyMode && !f_wix.canRead())
				return;
			br_wil = new BinaryReader(f_wil, "r");
			br_wil.skipBytes(44); // 跳过标题
			imageCount = br_wil.readIntLE(); // 图片数量
			offsetList = new int[imageCount + 1];
			offsetList[imageCount] = (int) br_wil.length();
			bitCount = SDK.colorCountToBitCount(br_wil.readIntLE()); // 色深度
			if (bitCount == 8) {
				// 8位灰度图可能版本标识不为0，此时操作不一样
				br_wil.skipBytes(4); // 忽略调色板
				// verFlag = br_wil.readIntLE();
			}
			if (!wilOnlyMode) {
				BinaryReader br_wix = new BinaryReader(f_wix, "r");
				br_wix.skipBytes(44); // 跳过标题
				int indexCount = br_wix.readIntLE(); // 索引数量(也是图片数量)
				// if(verFlag != 0)
				// br_wix.skipBytes(4); // 版本标识不为0需要跳过4字节
				for (int i = 0; i < indexCount; ++i) {
					// 读取数据偏移量
					offsetList[i] = br_wix.readIntLE();
				}
				br_wix.close();
			} else {
				imageInfos = new ImageInfo[imageCount];
				int lastOffset = 1024 + 4 + 4 + 4 + 44;
				for (int i = 0; i < imageCount; ++i) {
					offsetList[i] = lastOffset;
					if (bitCount == 8) {
						if (lastOffset + 9 > br_wil.length()) {
							// 数据出错，直接赋值为空图片
							imageInfos[i] = ImageInfo.EMPTY;
							continue;
						}
					} else {
						if (lastOffset + 12 > br_wil.length()) {
							// 数据出错，直接赋值为空图片
							imageInfos[i] = ImageInfo.EMPTY;
							continue;
						}
					}
					br_wil.seek(lastOffset);
					short w = (short) br_wil.readUnsignedShortLE();
					short h = (short) br_wil.readUnsignedShortLE();
					lastOffset += 8;
					if (w == 1 && h == 1) {
						// WIL可能有空图片，此时图片大小为1x1
						if (bitCount == 8) {
							lastOffset += 1;
						} else {
							lastOffset += 4;
						}
						imageInfos[i] = ImageInfo.EMPTY;
						continue;
					}
					ImageInfo ii = new ImageInfo();
					ii.setColorBit((byte) bitCount);
					ii.setWidth(w);
					ii.setHeight(h);
					ii.setOffsetX(br_wil.readShortLE());
					ii.setOffsetY(br_wil.readShortLE());
					imageInfos[i] = ii;
					lastOffset += SDK.widthBytes(bitCount * w) * h;
				}
				loaded = true;
				return;
			}
			imageInfos = new ImageInfo[imageCount];
			for (int i = 0; i < imageCount; ++i) {
				int offset = offsetList[i];
				if (offset + 9 > br_wil.length()) {
					// 数据出错，直接赋值为空图片
					imageInfos[i] = ImageInfo.EMPTY;
					continue;
				}
				int length = offsetList[i + 1] - offset - 8;
				if (length < 2) {
					// WIL中色彩数据为1个字节的是空图片，此时图片大小为1x1
					imageInfos[i] = ImageInfo.EMPTY;
					continue;
				}
				// 读取图片信息
				ImageInfo ii = new ImageInfo();
				ii.setColorBit((byte) bitCount);
				br_wil.seek(offset);
				ii.setWidth((short) br_wil.readUnsignedShortLE());
				ii.setHeight((short) br_wil.readUnsignedShortLE());
				ii.setOffsetX(br_wil.readShortLE());
				ii.setOffsetY(br_wil.readShortLE());
				imageInfos[i] = ii;
			}
			loaded = true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private WIL() {
	}

	/**
	 * 关闭WIL对象，释放其引用的文件流以及内存占用
	 */
	public synchronized final void close() throws IOException {
		offsetList = null;
		imageInfos = null;
		loaded = false;
		synchronized (wil_locker) {
			if (br_wil != null) {
				br_wil.close();
			}
			if (tmp_wil_dir != null)
				tmp_wil_dir.delete();
		}
	}

	public synchronized final Texture tex(int index) {
		if (!loaded)
			return Texture.EMPTY;
		if (index < 0)
			return Texture.EMPTY;
		if (index >= imageCount)
			return Texture.EMPTY;
		if (imageInfos[index] == ImageInfo.EMPTY)
			return Texture.EMPTY;
		try {
			ImageInfo ii = imageInfos[index];
			byte[] pixels = null;
			if (tmp_wil_dir != null) {
				File ftmpimg = new File(tmp_wil_dir.getAbsolutePath() + File.separator + index);
				if (ftmpimg.exists()) {
					FileInputStream fis = new FileInputStream(ftmpimg);
					pixels = new byte[fis.available()];
					fis.read(pixels);
					fis.close();
				}
			}
			if (pixels == null) {
				synchronized (wil_locker) {
					br_wil.seek(offsetList[index] + 8);
					int pixelLength = offsetList[index + 1] - offsetList[index];
					pixels = new byte[pixelLength - 8];
					br_wil.readFully(pixels);
					if (pixels.length == 1) {
						// 空白图片
						byte[] sRGB = new byte[3];
						byte[] pallete = SDK.palletes[pixels[0] & 0xff];
						sRGB[0] = pallete[1];
						sRGB[1] = pallete[2];
						sRGB[2] = pallete[3];
						return new Texture(sRGB, 1, 1);
					}
				}
			}
			byte[] sRGB = new byte[ii.getWidth() * ii.getHeight() * 3];
			if (bitCount == 8) {
				int p_index = 0;
				for (int h = ii.getHeight() - 1; h >= 0; --h)
					for (int w = 0; w < ii.getWidth(); ++w) {
						// 跳过填充字节
						if (w == 0)
							p_index += SDK.skipBytes(8, ii.getWidth());
						byte[] pallete = SDK.palletes[pixels[p_index++] & 0xff];
						int _idx = (w + h * ii.getWidth()) * 3;
						sRGB[_idx] = pallete[1];
						sRGB[_idx + 1] = pallete[2];
						sRGB[_idx + 2] = pallete[3];
					}
			} else if (bitCount == 16) {
				ByteBuffer bb = ByteBuffer.wrap(pixels);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				int p_index = 0;
				for (int h = ii.getHeight() - 1; h >= 0; --h)
					for (int w = 0; w < ii.getWidth(); ++w, p_index += 2) {
						// 跳过填充字节
						if (w == 0)
							p_index += SDK.skipBytes(16, ii.getWidth());
						short pdata = bb.getShort(p_index);
						byte r = (byte) ((pdata & 0xf800) >> 8);// 由于是与16位做与操作，所以多出了后面8位
						byte g = (byte) ((pdata & 0x7e0) >> 3);// 多出了3位，在强转时前8位会自动丢失
						byte b = (byte) ((pdata & 0x1f) << 3);// 少了3位
						int _idx = (w + h * ii.getWidth()) * 3;
						sRGB[_idx] = r;
						sRGB[_idx + 1] = g;
						sRGB[_idx + 2] = b;
					}
			}
			return new Texture(sRGB, ii.getWidth(), ii.getHeight());
		} catch (Exception ex) {
			ex.printStackTrace();
			return Texture.EMPTY;
		}
	}

	public synchronized final ImageInfo info(int index) {
		if (!loaded)
			return ImageInfo.EMPTY;
		if (index < 0)
			return ImageInfo.EMPTY;
		if (index >= imageCount)
			return ImageInfo.EMPTY;
		return imageInfos[index];
	}

	public synchronized int count() {
		return imageCount;
	}

	/**
	 * 创建新的库文件
	 * 
	 * @param libName
	 *            文件名
	 * @return 空的可读写的图片库对象
	 */
	public static WriteableImageLibrary create(String libName) {
		if (libName == null || libName.trim().isEmpty())
			return null;
		WIL wil = new WIL();
		wil.libName = libName;
		wil.tmp_wil_dir = new File(new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile() + File.separator
				+ new Random().nextLong());
		wil.tmp_wil_dir.mkdir();
		return wil;
	}

	@Override
	public synchronized void save(String dir) {
		if (!loaded)
			return;
		if (tmp_wil_dir == null)
			return;
		try {
			File fdir = new File(dir);
			if (!fdir.exists())
				return;
			if (!fdir.isDirectory())
				return;
			File fwil = new File(fdir.getAbsolutePath() + File.separator + libName + ".wil");
			if (fwil.exists())
				fwil.delete();
			File fwix = new File(fdir.getAbsolutePath() + File.separator + libName + ".wix");
			if (fwix.exists())
				fwix.delete();
			fwil.createNewFile();
			fwix.createNewFile();
			BinaryWriter bw_wil = new BinaryWriter(fwil, "rw");
			BinaryWriter bw_wix = new BinaryWriter(fwix, "rw");
			bw_wil.write(wilTitle);
			bw_wix.write(wixTitle);
			bw_wil.writeIntLE(imageCount);
			bw_wix.writeIntLE(imageCount);
			bw_wil.writeIntLE(SDK.bitCountToColorCount(bitCount));
			bw_wil.writeIntLE(1024);
			// bw_wil.writeIntLE(0); // verFlag
			bw_wil.write(currentUsePallete);
			int lastOffset = 1024 + 4 + 4 + 4 + 44;
			for (int i = 0; i < imageInfos.length; ++i) {
				bw_wix.writeIntLE(lastOffset);
				lastOffset += 8;
				bw_wil.writeShortLE(imageInfos[i].getWidth() < 1 ? 1 : imageInfos[i].getWidth());
				bw_wil.writeShortLE(imageInfos[i].getHeight() < 1 ? 1 : imageInfos[i].getHeight());
				bw_wil.writeShortLE(imageInfos[i].getOffsetX());
				bw_wil.writeShortLE(imageInfos[i].getOffsetY());
				if (imageInfos[i] == ImageInfo.EMPTY
						|| (imageInfos[i].getWidth() == 1 && imageInfos[i].getHeight() == 1)) {
					if (bitCount == 8) {
						lastOffset += 1;
						bw_wil.writeByte(0);
					} else {
						lastOffset += 4;
						bw_wil.writeByte(0);
						bw_wil.writeByte(0);
						bw_wil.writeByte(0);
						bw_wil.writeByte(0);
					}
				} else {
					int pixelsLength = SDK.widthBytes(bitCount * imageInfos[i].getWidth()) * imageInfos[i].getHeight();
					lastOffset += pixelsLength;
					File tmpFile = new File(tmp_wil_dir.getAbsolutePath() + File.separator + i);
					if (tmpFile.exists()) {
						byte[] pixels = new byte[pixelsLength];
						FileInputStream fis = new FileInputStream(tmpFile);
						fis.read(pixels);
						fis.close();
						bw_wil.write(pixels);
					} else {
						br_wil.seek(offsetList[i] + 8);
						int pixelLength = offsetList[i + 1] - offsetList[i];
						byte[] pixels = new byte[pixelLength - 8];
						br_wil.readFully(pixels);
						bw_wil.write(pixels);
					}
				}
			}
			bw_wil.close();
			bw_wix.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	private void warmup(int index, int colorBit) throws IOException {
		if (tmp_wil_dir == null) {
			tmp_wil_dir = new File(new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile() + File.separator
					+ new Random().nextLong());
			tmp_wil_dir.mkdir();
			File flib = new File(tmp_wil_dir.getAbsolutePath() + File.separator + "lib");
			FileOutputStream fos = new FileOutputStream(flib);
			br_wil.seek(0);
			byte[] buffer = new byte[1024 * 4];
			int readCount = -1;
			while ((readCount = br_wil.read(buffer)) > 0) {
				fos.write(buffer, 0, readCount);
			}
			fos.close();
			br_wil.close();
			br_wil = new BinaryReader(flib, "r");
			bitCount = colorBit;
		}
		if (bitCount != colorBit)
			throw new IllegalArgumentException("colorBit not match bitCount!");
		if (index > imageCount - 1) {
			ImageInfo[] oldInfos = imageInfos;
			imageInfos = new ImageInfo[index + 1];
			for (int i = 0; i < oldInfos.length; ++i) {
				imageInfos[i] = oldInfos[i];
			}
			for (int i = oldInfos.length; i < index + 1; ++i)
				imageInfos[i] = new ImageInfo();
		}
	}

	@Override
	public synchronized void tex(int index, byte[] rgbs, int colorBit, int width, int height, int offsetX,
			int offsetY) {
		if (rgbs == null || rgbs.length != width * height * 3 || rgbs.length != width * height * 4)
			return;
		try {
			warmup(index, colorBit);
			imageInfos[index].setColorBit((byte) bitCount);
			imageInfos[index].setWidth((short) width);
			imageInfos[index].setHeight((short) height);
			imageInfos[index].setOffsetX((short) offsetX);
			imageInfos[index].setOffsetY((short) offsetY);
			File fimg = new File(tmp_wil_dir.getAbsolutePath() + File.separator + index);
			FileOutputStream fosimg = new FileOutputStream(fimg);
			int skipBytes = SDK.skipBytes(bitCount, width);
			for (int h = height - 1; h >= 0; --h) {
				for (int w = 0; w < width; ++w) {
					// 跳过填充字节
					if (w == 0)
						for (int i = 0; i < skipBytes; ++i)
							fosimg.write(0);
					int ti = rgbs.length == width * height * 4 ? 1 : 0;
					byte r = rgbs[(w + h * width) * (3 + ti) + ti];
					byte g = rgbs[(w + h * width) * (3 + ti) + 1 + ti];
					byte b = rgbs[(w + h * width) * (3 + ti) + 2 + ti];
					if (bitCount == 8) {
						if (r == g && g == b && b == 0)
							fosimg.write(0);
						else {
							for (int i = 0; i < SDK.palletes.length; ++i) {
								if (r == SDK.palletes[i][1] && g == SDK.palletes[i][2] && b == SDK.palletes[i][3]) {
									fosimg.write(i);
									break;
								}
							}
						}
					} else if (bitCount == 16) {
						byte right = (byte) ((((r & 0x1f) << 10) | ((g & 0x38) << 5)) >> 8);
						byte left = (byte) (((g & 0x7) << 5) | (b & 0x3f));
						fosimg.write(left);
						fosimg.write(right);
					}
				}
			}
			fosimg.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	@Override
	public synchronized void tex(int index, byte[][] pallete, byte[] rgb8s, int width, int height, int offsetX,
			int offsetY) {
		if (rgb8s == null || rgb8s.length != width * height || rgb8s.length != SDK.widthBytes(8 * width) * height)
			return;
		try {
			warmup(index, 8);
			if (pallete != null) {
				if (pallete.length != 256) {
					throw new RuntimeException("pallete must contains 256 item");
				}
				boolean all3 = true;
				boolean all4 = true;
				for (int i = 0; i < 256; ++i) {
					if (pallete[i] == null)
						throw new RuntimeException("pallete item must contains 3 or 4 weight");
					if (pallete[i].length == 3)
						all4 = false;
					if (pallete[i].length == 4)
						all4 = false;
				}
				if (!all3 && !all4)
					throw new RuntimeException("pallete item must contains 3 or 4 weight");
				if (currentUsePallete == globalPallete)
					currentUsePallete = new byte[1024];
				for (int i = 0; i < 256; i++) {
					if (all4)
						currentUsePallete[i * 4] = pallete[i][0];
					currentUsePallete[i * 4 + 1] = pallete[i][all3 ? 0 : 1];
					currentUsePallete[i * 4 + 2] = pallete[i][all3 ? 1 : 2];
					currentUsePallete[i * 4 + 3] = pallete[i][all3 ? 2 : 3];
				}
			}
			tex8bit(index, rgb8s, width, height, offsetX, offsetY);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void tex(int index, int pallete[], byte[] rgb8s, int width, int height, int offsetX, int offsetY) {
		if (rgb8s == null || rgb8s.length != width * height || rgb8s.length != SDK.widthBytes(8 * width) * height)
			return;
		try {
			warmup(index, 8);
			if (pallete != null) {
				if (pallete.length != 256) {
					throw new RuntimeException("pallete must contains 256 item");
				}
				if (currentUsePallete == globalPallete)
					currentUsePallete = new byte[1024];
				for (int i = 0; i < 256; i++) {
					currentUsePallete[i * 4] = (byte) ((pallete[i] & 0xff000000) >> 24);
					currentUsePallete[i * 4 + 1] = (byte) ((pallete[i] & 0xff0000) >> 16);
					currentUsePallete[i * 4 + 2] = (byte) ((pallete[i] & 0xff00) >> 8);
					currentUsePallete[i * 4 + 3] = (byte) (pallete[i] & 0xff);
				}
			}
			tex8bit(index, rgb8s, width, height, offsetX, offsetY);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	private void tex8bit(int index, byte[] rgb8s, int width, int height, int offsetX, int offsetY) throws IOException {
		imageInfos[index].setColorBit((byte) bitCount);
		imageInfos[index].setWidth((short) width);
		imageInfos[index].setHeight((short) height);
		imageInfos[index].setOffsetX((short) offsetX);
		imageInfos[index].setOffsetY((short) offsetY);
		File fimg = new File(tmp_wil_dir.getAbsolutePath() + File.separator + index);
		FileOutputStream fosimg = new FileOutputStream(fimg);
		int skipBytes = SDK.skipBytes(bitCount, width);
		boolean hasBlank = rgb8s.length == SDK.widthBytes(8 * width) * height;
		for (int h = height - 1; h >= 0; --h) {
			for (int w = 0; w < width; ++w) {
				// 跳过填充字节
				if (w == 0)
					for (int i = 0; i < skipBytes; ++i)
						fosimg.write(0);
				int cindex = w + h * width;
				if (hasBlank)
					cindex += (h + 1) * SDK.widthBytes(8 * width);
				fosimg.write(rgb8s[cindex]);
			}
		}
		fosimg.close();
	}

	@Override
	public void tex(int index, short[] rgb16s, int width, int height, int offsetX, int offsetY) {
		if (rgb16s == null || rgb16s.length != width * height || rgb16s.length != SDK.widthBytes(16 * width) * height)
			return;
		try {
			warmup(index, 16);
			imageInfos[index].setColorBit((byte) bitCount);
			imageInfos[index].setWidth((short) width);
			imageInfos[index].setHeight((short) height);
			imageInfos[index].setOffsetX((short) offsetX);
			imageInfos[index].setOffsetY((short) offsetY);
			File fimg = new File(tmp_wil_dir.getAbsolutePath() + File.separator + index);
			FileOutputStream fosimg = new FileOutputStream(fimg);
			int skipBytes = SDK.skipBytes(bitCount, width);
			boolean hasBlank = rgb16s.length == SDK.widthBytes(16 * width) * height;
			for (int h = height - 1; h >= 0; --h) {
				for (int w = 0; w < width; ++w) {
					// 跳过填充字节
					if (w == 0)
						for (int i = 0; i < skipBytes; ++i)
							fosimg.write(0);
					int cindex = w + h * width;
					if (hasBlank)
						cindex += (h + 1) * SDK.widthBytes(16 * width);
					fosimg.write((rgb16s[cindex] & 0xff00) >> 8);
					fosimg.write(rgb16s[cindex] & 0xff);
				}
			}
			fosimg.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	@Override
	public synchronized void remove(int index) {
		if (!loaded)
			return;
		if (index > imageCount - 1)
			return;
		imageInfos[index] = ImageInfo.EMPTY;
		imageCount -= 1;
	}
}
