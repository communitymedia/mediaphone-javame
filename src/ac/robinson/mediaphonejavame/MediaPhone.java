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

import ac.robinson.mediaphonejavame.localization.L10nConstants;
import ac.robinson.mediaphonejavame.localization.L10nResources;

public class MediaPhone {

	public static final boolean DEBUG = false;

	public static void log(Object o) {
		System.out.println(o != null ? o.toString() : "(null log string)");
	}

	// where to save the images and audio files we create
	public static final String IMAGES_PATH = System.getProperty("fileconn.dir.photos");
	public static final String AUDIO_PATH = System.getProperty("fileconn.dir.recordings");

	// the RecordStore key for our XML content
	public static final String NARRATIVES_KEY = "mediaphone";

	// note: these values are updated to fit the screen size at startup
	public static int COMPONENT_SIZE_NORMAL = 64; // size of icons (pixels)
	public static int COMPONENT_SIZE_SMALL = 44; // size of add button and image/text icons (pixels)

	public static final int DEFAULT_FRAME_DURATION = 2500; // when no audio is present, frame duration in milliseconds
	public static final int TEXT_LENGTH_SHORT = 10; // number of characters (must be 3 or greater)
	public static final int TEXT_LENGTH_LONG = 30; // number of characters (must be 3 or greater)

	// interface configuration
	public static int BORDER_WIDTH = 3; // width of the button selection outline
	public static int SELECTION_COLOUR = 0x33b5e5; // the blue highlight for narratives
	public static String HIGHLIGHT_COLOUR = "cccccc";

	// localised strings
	private static L10nResources mL10n;
	static {
		// for some bizarre reason, the localisation code doesn't pick the default locale if the current isn't supported
		mL10n = L10nResources.getL10nResources(null);
		String localeCheck = getString(L10nConstants.keys.APP_NAME);
		if (localeCheck.startsWith("!!")) {
			mL10n.setLocale("en-US"); // value of L10nResources.DEFAULT_LOCALE
		}
	}

	public static String getString(String key) {
		return mL10n.getString(key);
	}

	// button actions
	public static class id {
		// narratives browser
		public static final int button_new_frame = 1;
		public static final int button_play_narrative = 2;
		public static final int button_delete_narrative = 3;
		public static final int button_exit = 4;

		// frame editor
		public static final int button_save_frame = 5;
		public static final int button_add_frame_after = 6;

		// image editor
		public static final int button_cancel_picture = 7;
		public static final int button_save_picture = 8;
		public static final int button_take_picture = 9;
		public static final int button_retake_picture = 10;

		// audio editor
		public static final int button_cancel_audio = 11;
		public static final int button_save_audio = 12;
		public static final int button_abort_audio = 13;
		public static final int button_start_audio = 14;
		public static final int button_stop_audio = 15;
		public static final int button_play_audio = 16;

		// text editor
		public static final int button_cancel_text = 17;
		public static final int button_save_text = 18;

		// playback
		public static final int button_finished_playback = 19;
		public static final int button_replay_narrative = 20;
	}

	public static class drawable {
		public static final String DRAWABLE_ROOT = "/drawable/";
		public static final String ic_audio_playback = DRAWABLE_ROOT + "ic_audio_playback.png";
		public static final String ic_menu_add = DRAWABLE_ROOT + "ic_menu_add.png";
		public static final String ic_menu_camera = DRAWABLE_ROOT + "ic_menu_camera.png";
		public static final String ic_menu_audio = DRAWABLE_ROOT + "ic_menu_audio.png";
		public static final String ic_menu_text = DRAWABLE_ROOT + "ic_menu_text.png";
	}
}
