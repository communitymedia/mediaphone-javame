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

public class StringUtilities {
	public static String trimText(String text, int length) {
		return text.length() > length ? text.substring(0, length - 3).trim() + "..." : text.trim();
	}

	public static String millisecondsToTimeString(int milliseconds) {
		// overestimating is better than just rounding
		double secondsDouble = (milliseconds / 1000d);
		int secondsIn = (int) ((secondsDouble - (int) secondsDouble) > 0 ? secondsDouble + 1 : secondsDouble);

		int hours = secondsIn / 3600;
		int remainder = secondsIn % 3600;
		int minutes = remainder / 60;
		int seconds = remainder % 60;

		return (hours > 0 ? hours + ":" : "") + (hours > 0 && minutes < 10 ? "0" : "") + minutes + ":"
				+ (seconds < 10 ? "0" : "") + seconds;
	}
}
