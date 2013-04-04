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

package ac.robinson.mediaphonejavame.component;

import ac.robinson.mediaphonejavame.MediaPhone;
import ac.robinson.mediaphonejavame.util.ImageCacheUtilities;
import ac.robinson.mediaphonejavame.util.StringUtilities;

import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.Style;

/**
 * Visualises a frame of a narrative as a button. The icon of the button indicates whether the frame includes an image,
 * audio or text
 */
public class FrameComponent extends Button {

	private boolean mIsAddFrameButton = false;

	public FrameComponent() {
		super();
		setTextPosition(Label.BOTTOM);
		setFocusable(false);
		setPreferredSize(new Dimension(MediaPhone.COMPONENT_SIZE_NORMAL, MediaPhone.COMPONENT_SIZE_NORMAL));
	}

	public FrameComponent(boolean isAddFrameButton) {
		this();
		mIsAddFrameButton = isAddFrameButton;
		if (mIsAddFrameButton) {
			setIcon(ImageCacheUtilities.getScaledCachedSquareImage(MediaPhone.drawable.ic_menu_add,
					MediaPhone.COMPONENT_SIZE_SMALL));
			setAddFrameButtonStyle(getStyle(), false);
			setAddFrameButtonStyle(getSelectedStyle(), true);
		}
	}

	private void setAddFrameButtonStyle(Style currentStyle, boolean selected) {
		currentStyle.setBorder(null, false);
		if (selected) {
			int borderRadius = MediaPhone.COMPONENT_SIZE_SMALL / MediaPhone.BORDER_WIDTH;
			currentStyle.setBgColor(MediaPhone.SELECTION_COLOUR, false);
			currentStyle.setBorder(
					Border.createRoundBorder(borderRadius, borderRadius, MediaPhone.SELECTION_COLOUR, true), false);
		} else {
			currentStyle.setBgTransparency(0, false);
		}
	}

	public void setImage(String image) {
		if (image != null) {
			setIcon(ImageCacheUtilities.getScaledCachedSquareImage(image, MediaPhone.COMPONENT_SIZE_NORMAL));
		}
	}

	public void setAudio(String audio) {
		if (audio != null && getIcon() == null) {
			setIcon(ImageCacheUtilities.getScaledCachedSquareImage(MediaPhone.drawable.ic_audio_playback,
					MediaPhone.COMPONENT_SIZE_SMALL));
		}
	}

	public void setText(String text) {
		if (text != null) {
			super.setText(StringUtilities.trimText(text, MediaPhone.TEXT_LENGTH_SHORT));
		}
	}

	public void setAsAddFrameButton(boolean isAddFrameButton) {
		mIsAddFrameButton = isAddFrameButton;
	}

	public boolean isAddFrameButton() {
		return mIsAddFrameButton;
	}
}
