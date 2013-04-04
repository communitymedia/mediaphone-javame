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

package ac.robinson.mediaphonejavame.provider;

import java.util.Vector;

import ac.robinson.mediaphonejavame.util.GUIDUtilities;

/**
 * Represents a narrative consisting of a list of frames
 */
public class Narrative {

	private String mInternalId;
	private Vector mFrames = new Vector();

	public Narrative() {
		setGUID(GUIDUtilities.generateGUID());
	}

	public void setGUID(String uuid) {
		mInternalId = uuid;
	}

	public String getGUID() {
		return mInternalId;
	}

	public void setFrames(Vector frames) {
		mFrames = frames;
	}

	public Vector getFrames() {
		return mFrames;
	}

	public void addFrame(Frame frame) {
		mFrames.addElement(frame);
	}

	public void addFrame(String image, String audio, String text) {
		mFrames.addElement(new Frame(image, audio, text));
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Narrative: frames " + mFrames.size() + "\n");
		for (int i = 0, n = mFrames.size(); i < n; i++) {
			stringBuffer.append(((Frame) mFrames.elementAt(i)).toString() + "\n");
		}
		return stringBuffer.toString();
	}
}
