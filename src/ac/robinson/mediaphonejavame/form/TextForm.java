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

import ac.robinson.mediaphonejavame.MediaPhone;
import ac.robinson.mediaphonejavame.localization.L10nConstants;
import ac.robinson.mediaphonejavame.util.UIUtilities;

import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * A form for adding text to a frame.
 */
public class TextForm extends Form implements ActionListener {

	private FrameEditorForm mFrameEditorForm;

	private TextField mTextArea;

	// TODO: allow deletion
	public TextForm(FrameEditorForm frameEditorForm) {
		super(MediaPhone.getString(L10nConstants.keys.TITLE_TEXT));
		mFrameEditorForm = frameEditorForm;

		setLayout(new BoxLayout(BoxLayout.Y_AXIS));

		mTextArea = new TextField();
		mTextArea.setSingleLineTextArea(false);
		mTextArea.setText(frameEditorForm.getCurrentText() != null ? frameEditorForm.getCurrentText() : "");
		addComponent(mTextArea);

		addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_CANCEL), MediaPhone.id.button_cancel_text));
		addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_SAVE), MediaPhone.id.button_save_text));

		// set number of rows after we've added the commands (they affect getAvailableHeight)
		// -4 is for the 2-pixel border at the top and the bottom (TODO: find a function to get this size)
		mTextArea.setPreferredH(UIUtilities.getAvailableHeight(frameEditorForm) - 4);

		addCommandListener(this);
	}

	public void actionPerformed(ActionEvent event) {
		switch (event.getCommand().getId()) {
			case MediaPhone.id.button_cancel_text:
				mFrameEditorForm.show();
				break;
			case MediaPhone.id.button_save_text:
				String currentText = mTextArea.getText();
				mFrameEditorForm.setCurrentText(currentText.length() > 0 ? currentText : null);
				mFrameEditorForm.show();
				break;
		}
	}
}
