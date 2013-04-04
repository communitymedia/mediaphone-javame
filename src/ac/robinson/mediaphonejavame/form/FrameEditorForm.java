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

package ac.robinson.mediaphonejavame.form;

import java.util.Vector;

import ac.robinson.mediaphonejavame.MediaPhone;
import ac.robinson.mediaphonejavame.MediaPhoneMidlet;
import ac.robinson.mediaphonejavame.localization.L10nConstants;
import ac.robinson.mediaphonejavame.provider.Frame;
import ac.robinson.mediaphonejavame.provider.Narrative;
import ac.robinson.mediaphonejavame.provider.NarrativeManager;
import ac.robinson.mediaphonejavame.util.ImageCacheUtilities;
import ac.robinson.mediaphonejavame.util.StringUtilities;
import ac.robinson.mediaphonejavame.util.UIUtilities;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Style;

/**
 * Shows the view for creating a narrative. The form has options to capture a photo, record voice or add text.
 */
public class FrameEditorForm extends Form implements ActionListener {

	private Button mPhotoButton;
	private Button mAudioButton;
	private Button mTextButton;

	private Narrative mNarrative = null;

	private Vector mFrames = new Vector();

	private Frame mCurrentFrame = new Frame();
	private int mFrameIndex = -1;

	public FrameEditorForm(Narrative narrative, int editFrame) {
		super();

		mNarrative = narrative;
		if (mNarrative != null) {
			mFrames = narrative.getFrames();
			mFrameIndex = editFrame;
			if (mFrameIndex >= 0) {
				mCurrentFrame = (Frame) mFrames.elementAt(mFrameIndex);
			}
		}

		setLayout(new BoxLayout(BoxLayout.Y_AXIS));

		addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_ADD_FRAME),
				MediaPhone.id.button_add_frame_after));
		addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_SAVE), MediaPhone.id.button_save_frame));
		addCommandListener(this);

		initialiseButtons();
	}

	public void show() {
		refreshLayout(); // so we update the content
		super.show();
	}

	private void initialiseButtons() {
		mPhotoButton = new Button();
		mPhotoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new CameraForm(FrameEditorForm.this).show();
			};
		});
		mAudioButton = new Button();
		mAudioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new AudioForm(FrameEditorForm.this).show();
			};
		});
		mTextButton = new Button();
		mTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new TextForm(FrameEditorForm.this).show();
			};
		});

		refreshLayout();

		addButton(mPhotoButton);
		addButton(mAudioButton);
		addButton(mTextButton);
	}

	private void addButton(Button button) {
		Style style = button.getStyle();
		style.setAlignment(Button.CENTER);
		style.setBorder(null);
		button.setPreferredW(getWidth());
		button.setTextPosition(Label.BOTTOM);
		addComponent(button);
	}

	private void refreshLayout() {
		setTitle(MediaPhone.getString(L10nConstants.keys.TITLE_FRAME_EDITOR) + " "
				+ ((mFrameIndex >= 0 ? mFrameIndex : mFrames.size()) + 1));

		mPhotoButton.setIcon(ImageCacheUtilities.getScaledCachedSquareImage(
				getCurrentImage() != null ? getCurrentImage() : MediaPhone.drawable.ic_menu_camera,
				MediaPhone.COMPONENT_SIZE_NORMAL));

		if (getCurrentAudio() != null) {
			mAudioButton.setIcon(ImageCacheUtilities.getScaledCachedSquareImage(MediaPhone.drawable.ic_menu_audio,
					MediaPhone.COMPONENT_SIZE_SMALL));
			mAudioButton
					.setText(StringUtilities.millisecondsToTimeString(mCurrentFrame.getAudioDurationMilliseconds()));
		} else {
			mAudioButton.setIcon(ImageCacheUtilities.getScaledCachedSquareImage(MediaPhone.drawable.ic_menu_audio,
					MediaPhone.COMPONENT_SIZE_NORMAL));
			mAudioButton.setText("");
		}

		if (getCurrentText() != null) {
			mTextButton.setIcon(ImageCacheUtilities.getScaledCachedSquareImage(MediaPhone.drawable.ic_menu_text,
					MediaPhone.COMPONENT_SIZE_SMALL));
			mTextButton.setText(StringUtilities.trimText(getCurrentText(), MediaPhone.TEXT_LENGTH_LONG));
		} else {
			mTextButton.setIcon(ImageCacheUtilities.getScaledCachedSquareImage(MediaPhone.drawable.ic_menu_text,
					MediaPhone.COMPONENT_SIZE_NORMAL));
			mTextButton.setText("");
		}

		// try to fit to height (3.2 as setPreferredH doesn't include borders TODO: find a function for border height)
		int buttonHeight = (int) (UIUtilities.getAvailableHeight(this) / 3.2);
		mPhotoButton.setPreferredH(buttonHeight);
		mAudioButton.setPreferredH(buttonHeight);
		mTextButton.setPreferredH(buttonHeight);
	}

	public void setCurrentImage(String image) {
		mCurrentFrame.setImage(image);
	}

	public String getCurrentImage() {
		return mCurrentFrame.getImage();
	}

	public void setCurrentAudio(String audio, int duration) {
		mCurrentFrame.setAudio(audio);
		mCurrentFrame.setAudioDurationMilliseconds(duration);
	}

	public String getCurrentAudio() {
		return mCurrentFrame.getAudio();
	}

	public void setCurrentText(String text) {
		mCurrentFrame.setText(text);
	}

	public String getCurrentText() {
		return mCurrentFrame.getText();
	}

	public void actionPerformed(ActionEvent event) {
		switch (event.getCommand().getId()) {
			case MediaPhone.id.button_save_frame:
				if (mCurrentFrame.hasContent()) {
					if (mFrameIndex >= 0) {
						mFrames.setElementAt(mCurrentFrame, mFrameIndex); // replace the existing frame
					} else {
						mFrames.addElement(mCurrentFrame);
					}
				} else if (mFrameIndex >= 0) {
					mFrames.removeElementAt(mFrameIndex);
				}
				if (!mFrames.isEmpty()) {
					if (mNarrative == null) {
						mNarrative = new Narrative();
					}
					mNarrative.setFrames(mFrames);
					NarrativeManager.getInstance().addNarrative(mNarrative); // checks for duplicates and saves
				} else if (mNarrative != null) {
					NarrativeManager.getInstance().removeNarrative(mNarrative);
				}
				MediaPhoneMidlet.getInstance().showNarrativeListForm();
				break;
			case MediaPhone.id.button_add_frame_after:
				if (mCurrentFrame.hasContent()) {
					if (mFrameIndex >= 0) {
						mFrames.setElementAt(mCurrentFrame, mFrameIndex); // replace the existing frame
					} else {
						mFrames.addElement(mCurrentFrame);
					}
					mCurrentFrame = new Frame();
					if (mFrameIndex >= 0) {
						mFrameIndex += 1;
						mFrames.insertElementAt(mCurrentFrame, mFrameIndex);
					}
					refreshLayout();
				}
				break;
		}
	}
}
