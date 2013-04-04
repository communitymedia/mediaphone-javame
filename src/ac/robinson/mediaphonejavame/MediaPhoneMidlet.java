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

package ac.robinson.mediaphonejavame;

import java.util.Vector;

import javax.microedition.midlet.MIDlet;

import ac.robinson.mediaphonejavame.component.NarrativeComponent;
import ac.robinson.mediaphonejavame.form.FrameEditorForm;
import ac.robinson.mediaphonejavame.form.PlayNarrativeForm;
import ac.robinson.mediaphonejavame.localization.L10nConstants;
import ac.robinson.mediaphonejavame.provider.Narrative;
import ac.robinson.mediaphonejavame.provider.NarrativeManager;
import ac.robinson.mediaphonejavame.util.UIUtilities;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * A MIDlet for creating narratives. Shows the narratives as a vertical list, where each horizontal row contains the
 * frames included in the narrative.
 */
public class MediaPhoneMidlet extends MIDlet implements ActionListener {

	private static MediaPhoneMidlet mInstance;

	public Form mNarrativeListForm = null;
	private FrameEditorForm mFrameEditorForm = null;

	public MediaPhoneMidlet() {
		Display.init(this);
		mInstance = this;
		UIUtilities.configureApplicationStyle();
	}

	public static MediaPhoneMidlet getInstance() {
		return mInstance;
	}

	public void startApp() {
		// TODO: allow vertical scrolling on touch screens
		mNarrativeListForm = new Form(MediaPhone.getString(L10nConstants.keys.TITLE_NARRATIVES));
		mNarrativeListForm.addCommandListener(this);
		mNarrativeListForm.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		mNarrativeListForm.setCyclicFocus(true);
		mNarrativeListForm.setFocusScrolling(true);
		mNarrativeListForm.setFocusable(true);
		mNarrativeListForm.setHandlesInput(true);
		showNarrativeListForm();
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}

	public void actionPerformed(ActionEvent event) {
		switch (event.getCommand().getId()) {
			case MediaPhone.id.button_new_frame:
				showFrameEditorForm(null);
				break;
			case MediaPhone.id.button_play_narrative:
				// TODO: improve for touch screen
				NarrativeComponent componentToPlay = (NarrativeComponent) mNarrativeListForm.getFocused();
				if (!componentToPlay.isAddNarrativeButton()) {
					new PlayNarrativeForm(componentToPlay.getNarrative(), 0).show(); // start from the beginning
				}
				break;
			case MediaPhone.id.button_delete_narrative:
				// TODO: show confirmation before deleting, and improve for touch screen
				NarrativeComponent componentToDelete = (NarrativeComponent) mNarrativeListForm.getFocused();
				NarrativeManager.getInstance().removeNarrative(componentToDelete.getNarrative());
				showNarrativeListForm();
				break;
			case MediaPhone.id.button_exit:
				destroyApp(false);
				notifyDestroyed();
				break;
		}
	}

	public void showFrameEditorForm(Narrative narrative) {
		mFrameEditorForm = new FrameEditorForm(narrative, -1);
		mFrameEditorForm.show();
	}

	public void showFrameEditorForm(Narrative narrative, int selectedItem) {
		mFrameEditorForm = new FrameEditorForm(narrative, selectedItem);
		mFrameEditorForm.show();
	}

	public void showNarrativeListForm() {
		mNarrativeListForm.removeAll();
		mNarrativeListForm.addComponent(new Label(MediaPhone.getString(L10nConstants.keys.HINT_LOADING)));
		mNarrativeListForm.show();

		new Thread(new Runnable() {
			public void run() {
				mNarrativeListForm.removeAll();

				mNarrativeListForm.addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_EXIT),
						MediaPhone.id.button_exit));
				mNarrativeListForm.addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_DELETE),
						MediaPhone.id.button_delete_narrative));
				mNarrativeListForm.addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_PLAY),
						MediaPhone.id.button_play_narrative));
				mNarrativeListForm.addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_NEW),
						MediaPhone.id.button_new_frame));

				// this must be done after we know the size of the menu bar
				MediaPhone.COMPONENT_SIZE_NORMAL = (int) (UIUtilities.getAvailableHeight(mNarrativeListForm) / 3.8f);
				MediaPhone.COMPONENT_SIZE_SMALL = (int) (MediaPhone.COMPONENT_SIZE_NORMAL * 0.65);

				mNarrativeListForm.addComponent(new NarrativeComponent(new Narrative())); // the new narrative button

				Vector narratives = NarrativeManager.getInstance().getNarratives();
				for (int i = narratives.size() - 1; i >= 0; i--) {
					mNarrativeListForm.addComponent(new NarrativeComponent((Narrative) narratives.elementAt(i)));
				}

				mNarrativeListForm.show();
				mNarrativeListForm.scrollComponentToVisible(mNarrativeListForm.getFocused());
			}
		}).start();
	}
}
