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

import java.util.Vector;

import ac.robinson.mediaphonejavame.MediaPhoneMidlet;
import ac.robinson.mediaphonejavame.provider.Frame;
import ac.robinson.mediaphonejavame.provider.Narrative;
import ac.robinson.mediaphonejavame.util.UIUtilities;

import com.sun.lwuit.Container;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * Visualises a a narrative as a list of FrameComponents. Also takes care of the navigating between narratives and
 * frames within a narrative.
 */
public class NarrativeComponent extends Container {

	private boolean mIsAddNarrativeButton = false;
	private int mDragX = -1;
	private boolean mDragged = false;
	private static int mComponentWidth = 0;

	private Narrative mNarrative = null;
	private Vector mFrameComponents = new Vector();

	private int mFocusedItem = 0;

	public NarrativeComponent(Narrative narrative) {
		mNarrative = narrative;

		Vector frames = narrative.getFrames();
		setLayout(new BoxLayout(BoxLayout.X_AXIS));
		setFocusable(true);
		setNextFocusLeft(this);
		setNextFocusRight(this);
		setScrollableX(true);
		setScrollVisible(true);

		for (int i = 0, n = frames.size(); i < n; i++) {
			Frame frame = (Frame) frames.elementAt(i);
			FrameComponent frameComponent = new FrameComponent();
			frameComponent.setImage(frame.getImage());
			frameComponent.setAudio(frame.getAudio());
			frameComponent.setText(frame.getText());
			addComponent(frameComponent);
			mFrameComponents.addElement(frameComponent);
		}

		// the new narrative/frame button
		FrameComponent frameComponent = new FrameComponent(true);
		addComponent(frameComponent);
		mFrameComponents.addElement(frameComponent);
		if (frames.size() == 0) {
			mIsAddNarrativeButton = true;
		}
	}

	public void setNarrative(Narrative narrative) {
		mNarrative = narrative;
	}

	public Narrative getNarrative() {
		return mNarrative;
	}

	public void setAsAddNarrativeButton(boolean isAddNarrativeButton) {
		mIsAddNarrativeButton = isAddNarrativeButton;
	}

	public boolean isAddNarrativeButton() {
		return mIsAddNarrativeButton;
	}

	public void focusGained() {
		super.focusGained();
		mFocusedItem = 0;
		if (!mFrameComponents.isEmpty()) {
			((FrameComponent) mFrameComponents.elementAt(mFocusedItem)).setFocus(true);
			getStyle().setBgTransparency(52); // colour doesn't work; 52 = approximately cccccc (HIGHLIGHT_COLOUR)
			getStyle().setBgColor(0x000000);
		}
	}

	public void focusLost() {
		super.focusLost();
		if (!mFrameComponents.isEmpty()) {
			((FrameComponent) mFrameComponents.elementAt(mFocusedItem)).setFocus(false);
			mFocusedItem = 0;
			getStyle().setBgTransparency(0);
			getStyle().setBgColor(0xffffff);
		}
	}

	public void pointerPressed(int x, int y) {
		int previousItem = mFocusedItem;
		for (int i = 0, n = mFrameComponents.size(); i < n; i++) {
			if (((FrameComponent) mFrameComponents.elementAt(i)).contains(x, y)) {
				mFocusedItem = i;
			}
		}
		if (previousItem != mFocusedItem) {
			FrameComponent focused = ((FrameComponent) mFrameComponents.elementAt(previousItem));
			focused.setFocus(false);
			focused = ((FrameComponent) mFrameComponents.elementAt(mFocusedItem));
			focused.setFocus(true);
		}
		mDragged = false;
		mDragX = x;
	}

	public void pointerReleased(int x, int y) {
		if (!mDragged) {
			int newFocus = -1;
			for (int i = 0, n = mFrameComponents.size(); i < n; i++) {
				if (((FrameComponent) mFrameComponents.elementAt(i)).contains(x, y)) {
					newFocus = i;
					break;
				}
			}
			if (mFocusedItem == newFocus) {
				keyReleased(UIUtilities.SOFT_KEY_MIDDLE[0]);
			}
		}
	}

	public void pointerDragged(int x, int y) {
		int scrollDistance = (mDragX - x);
		if (scrollDistance == 0) {
			return;
		}

		int xStart = getScrollX() + (scrollDistance > 0 ? getWidth() : scrollDistance);
		if (mComponentWidth <= 0) {
			// +4 as getWidth doesn't include borders TODO: find a function for border size
			mComponentWidth = ((FrameComponent) mFrameComponents.elementAt(mFocusedItem)).getWidth() + 4;
		}
		int maxX = mComponentWidth * mFrameComponents.size();

		if (xStart < 0) {
			xStart = 0;
		} else if (xStart + scrollDistance > maxX) {
			xStart = maxX - scrollDistance;
		}

		scrollRectToVisible(new Rectangle(xStart, 0, scrollDistance, getHeight()), this);

		mDragged = true;
		mDragX = x;
	}

	public void keyReleased(int keyCode) {
		// MediaPhone.log("Key pressed: " + keyCode); // for key numbers, see: http://www.iteye.com/topic/179073
		FrameComponent focused = (FrameComponent) mFrameComponents.elementAt(mFocusedItem);
		for (int i = 0; i < UIUtilities.SOFT_KEY_MIDDLE.length; i++) {
			if (keyCode == UIUtilities.SOFT_KEY_MIDDLE[i]) {
				if (mIsAddNarrativeButton) {
					MediaPhoneMidlet.getInstance().showFrameEditorForm(null);
				} else if (focused.isAddFrameButton()) {
					MediaPhoneMidlet.getInstance().showFrameEditorForm(mNarrative);
				} else {
					MediaPhoneMidlet.getInstance().showFrameEditorForm(mNarrative, mFocusedItem);
				}
				return;
			}
		}

		focused.setFocus(false);
		boolean rightKey = false;
		for (int i = 0; i < UIUtilities.SOFT_KEY_RIGHT.length; i++) {
			if (keyCode == UIUtilities.SOFT_KEY_RIGHT[i]) {
				mFocusedItem = mFocusedItem + 1 >= mFrameComponents.size() ? 0 : mFocusedItem + 1;
				rightKey = true;
				break;
			}
		}
		if (!rightKey) {
			for (int i = 0; i < UIUtilities.SOFT_KEY_LEFT.length; i++) {
				if (keyCode == UIUtilities.SOFT_KEY_LEFT[i]) {
					mFocusedItem = mFocusedItem - 1 < 0 ? mFrameComponents.size() - 1 : mFocusedItem - 1;
					break;
				}
			}
		}
		focused = (FrameComponent) mFrameComponents.elementAt(mFocusedItem);
		focused.setFocus(true);
		scrollComponentToVisible(focused);
		repaint();
	}
}
