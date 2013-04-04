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

/**
 * Represents a frame in a narrative. Can contain any combination of a photo, an audio clip or a text string.
 */
public class Frame {

	private String mImage = null;
	private String mAudio = null;
	private String mText = null;

	private int mAudioDurationMilliseconds = 0;

	public Frame() {
	}

	public Frame(String image, String audio, String text) {
		setImage(image);
		setAudio(audio);
		setText(text);
	}

	public void setImage(String image) {
		mImage = image;
	}

	public String getImage() {
		return mImage;
	}

	public void setAudio(String audio) {
		mAudio = audio;
	}

	public String getAudio() {
		return mAudio;
	}

	public void setText(String text) {
		mText = text;
	}

	public String getText() {
		return mText;
	}

	public void setAudioDurationMilliseconds(int duration) {
		mAudioDurationMilliseconds = duration;
	}

	public int getAudioDurationMilliseconds() {
		return mAudioDurationMilliseconds;
	}

	public boolean hasContent() {
		return mImage != null || mAudio != null || mText != null;
	}

	public String toString() {
		return "Frame (" + mAudioDurationMilliseconds + "ms) : image " + mImage + ", audio " + mAudio + ", text "
				+ mText;
	}
}
