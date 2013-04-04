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

import java.util.Hashtable;

import ac.robinson.mediaphonejavame.MediaPhone;

import com.sun.lwuit.Button;
import com.sun.lwuit.Component;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Form;
import com.sun.lwuit.TextField;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.list.DefaultListCellRenderer;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.LookAndFeel;
import com.sun.lwuit.plaf.UIManager;

public class UIUtilities {
	// see: http://www.iteye.com/topic/179073
	private static final int SOFT_KEY_LEFT_GENERIC = -3;
	private static final int SOFT_KEY_LEFT_NOKIA = -6;
	private static final int SOFT_KEY_LEFT_MOTOROLA = -21;
	private static final int SOFT_KEY_LEFT_MOTOROLA1 = 21;
	private static final int SOFT_KEY_LEFT_MOTOROLA2 = -20;
	// private static final int SOFT_KEY_LEFT_SIEMENS = -1; // removed as it duplicates the up key on other platforms
	// private static final int SOFT_KEY_LEFT_SAMSUNG = -6; // duplicates are unnecessary

	private static final int SOFT_KEY_RIGHT_GENERIC = -4;
	private static final int SOFT_KEY_RIGHT_NOKIA = -7;
	private static final int SOFT_KEY_RIGHT_MOTOROLA = -22;
	private static final int SOFT_KEY_RIGHT_MOTOROLA1 = 22;
	// private static final int SOFT_KEY_RIGHT_SIEMENS = -4; // duplicates are unnecessary
	// private static final int SOFT_KEY_RIGHT_SAMSUNG = -7; // duplicates are unnecessary

	private static final int SOFT_KEY_MIDLE_NOKIA = -5;
	private static final int SOFT_KEY_MIDLE_MOTOROLA = -23;
	private static final int INTERNET_KEY_GENERIC = -10;

	public static final int[] SOFT_KEY_LEFT = new int[] { SOFT_KEY_LEFT_GENERIC, SOFT_KEY_LEFT_NOKIA,
			SOFT_KEY_LEFT_MOTOROLA, SOFT_KEY_LEFT_MOTOROLA1, SOFT_KEY_LEFT_MOTOROLA2 };

	public static final int[] SOFT_KEY_RIGHT = new int[] { SOFT_KEY_RIGHT_GENERIC, SOFT_KEY_RIGHT_NOKIA,
			SOFT_KEY_RIGHT_MOTOROLA, SOFT_KEY_RIGHT_MOTOROLA1 };

	public static final int[] SOFT_KEY_MIDDLE = new int[] { SOFT_KEY_MIDLE_NOKIA, SOFT_KEY_MIDLE_MOTOROLA,
			INTERNET_KEY_GENERIC };

	public static void configureApplicationStyle() {
		Hashtable themeProps = new Hashtable();
		themeProps.put("CommandFocus.sel#bgColor", MediaPhone.HIGHLIGHT_COLOUR); // for custom menus where native fails
		themeProps.put("font", Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
		themeProps.put("TextField.font",
				Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
		themeProps.put("Button.align", new Integer(Component.CENTER));
		themeProps.put("Button.sel#border",
				Border.createLineBorder(MediaPhone.BORDER_WIDTH, MediaPhone.SELECTION_COLOUR));
		UIManager.getInstance().setThemeProps(themeProps);

		LookAndFeel lookAndFeel = UIManager.getInstance().getLookAndFeel();
		lookAndFeel.setReverseSoftButtons(true);
		lookAndFeel.setDefaultSmoothScrolling(true);

		Dialog.setDefaultDialogPosition(BorderLayout.CENTER);

		// TODO: these options are not always good; should be enabled per-device (touch/non-touch)
		// Display defaultDisplay = Display.getInstance();
		// defaultDisplay.setCommandBehavior(Display.COMMAND_BEHAVIOR_NATIVE); // use native buttons where possible
		// defaultDisplay.setDefaultVirtualKeyboard(null); // because it is a UI travesty
		// defaultDisplay.setThirdSoftButton(true); // messes up by adding an extra menu option on 2-button devices

		DefaultListCellRenderer.setShowNumbersDefault(false); // we highlight menus instead of showing numbers

		TextField.setReplaceMenuDefault(false); // don't replace default menu with confusing T9 option
		TextField.setUseNativeTextInput(true); // try to use native input where possible (hint: almost never!)
	}

	public static int getAvailableHeight(Form form) {
		int contentHeight = Display.getInstance().getDisplayHeight() - form.getTitleComponent().getPreferredH();
		if (form.getSoftButtonCount() > 0) {
			Button button = form.getSoftButton(0);
			if (button != null) {
				if (button.getParent() == null) { // when using native UI there is no parent
					contentHeight -= button.getPreferredH();
				} else {
					contentHeight -= button.getParent().getPreferredH();
				}
			}
		}
		return contentHeight;
	}
}
