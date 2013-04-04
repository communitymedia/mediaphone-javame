/*
 *  Copyright 2013 Elina Vartiainen and Simon Robinson
 * 
 *  This file is part of Com-Me.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ac.robinson.mediaphonejavame.util;

import java.util.Random;

public class GUIDUtilities {

	// public static SecureRandom secureRandom = new SecureRandom(); // not in Java ME - just use Random instead
	public static Random secureRandom = new Random();

	// constants that are used in the generateGUID method
	private static class GUIDConstants {

		// number of bytes in guid
		public static final int ByteArraySize = 16;

		// guid variant types
		// public static final int VariantReservedNCS = 0x00;
		public static final int VariantStandard = 0x02;
		// public static final int VariantReservedMicrosoft = 0x06;
		// public static final int VariantReservedFuture = 0x07;

		// multiplex variant info
		public static final int VariantByte = 8;
		public static final int VariantByteMask = 0x3f;
		public static final int VariantByteShift = 6;

		// guid version types
		// public static final int VersionTimeBased = 0x01;
		// public static final int VersionReserved = 0x02;
		// public static final int VersionNameBased = 0x03;
		public static final int VersionRandom = 0x04;

		// multiplex version info
		public static final int VersionByte = 6;
		public static final int VersionByteMask = 0x0f;
		public static final int VersionByteShift = 4;
	}

	// create a byte array from a random long
	private static byte[] getEightRandomBytes() {
		final long x = secureRandom.nextLong();
		// ((long)secureRandom.nextInt() << 32) + secureRandom.nextInt();
		final byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = (byte) (x >> ((7 - i) * 8));
		}
		return bytes;
	}

	// adapted from: http://msdn.microsoft.com/en-us/library/aa446557.aspx
	// see also: http://www.java2s.com/Code/Java/J2ME/PasswordMIDlet.htm
	public static String generateGUID() {
		return generateGUID(true);
	}

	public static String generateGUID(boolean addSeparators) {
		byte[] bits = new byte[GUIDConstants.ByteArraySize]; // 16 bytes = 128 bits

		// not available on Java ME
		// secureRandom.nextBytes(bits);

		// instead:
		byte[] tempBits = null;
		for (int i = 0; i < GUIDConstants.ByteArraySize; i++) {
			if (i % 8 == 0) {
				tempBits = getEightRandomBytes();
			}
			bits[i] = tempBits[i % 8];
		}

		// set the variant
		bits[GUIDConstants.VariantByte] &= GUIDConstants.VariantByteMask;
		bits[GUIDConstants.VariantByte] |= ((int) GUIDConstants.VariantStandard << GUIDConstants.VariantByteShift);

		// set the version
		bits[GUIDConstants.VersionByte] &= GUIDConstants.VersionByteMask;
		bits[GUIDConstants.VersionByte] |= ((int) GUIDConstants.VersionRandom << GUIDConstants.VersionByteShift);

		// construct the GUID string
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < bits.length; i++) {
			if (addSeparators) {
				switch (i) {
					case 4:
					case 6:
					case 8:
					case 10:
						stringBuffer.append("-");
						break;
				}
			}
			int b = bits[i] & 0xFF;
			if (b < 0x10)
				stringBuffer.append('0');
			stringBuffer.append(Integer.toHexString(b));
		}

		return stringBuffer.toString();
	}
}
