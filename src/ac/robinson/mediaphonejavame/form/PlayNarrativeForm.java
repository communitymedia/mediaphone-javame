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

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import ac.robinson.mediaphonejavame.MediaPhone;
import ac.robinson.mediaphonejavame.MediaPhoneMidlet;
import ac.robinson.mediaphonejavame.localization.L10nConstants;
import ac.robinson.mediaphonejavame.provider.Frame;
import ac.robinson.mediaphonejavame.provider.Narrative;
import ac.robinson.mediaphonejavame.util.ImageCacheUtilities;

import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Style;

/**
 * Creates a view for playing back the narrative. The animation is created as a set of timer tasks, where each tasks
 * represents a frame. The length of the animation is calculated using the length of possible recordings or a default
 * value for each frame. The timer tasks are then fired after each other in the same order as in the narrative.
 */
public class PlayNarrativeForm extends Form implements ActionListener {

	private Narrative mNarrative;

	private Timer mTimer;
	private int mStartingFrame;

	private Player mPlayer = null;
	private InputStream mInputStream = null;
	private FileConnection mFileConnection = null;

	private Label mImageLabel = new Label();
	private Label mAudioLabel = new Label();
	private TextArea mTextArea = new TextArea();

	public PlayNarrativeForm(Narrative narrative, int currentFrame) {
		super();
		mNarrative = narrative;
		mStartingFrame = currentFrame;

		setLayout(new BoxLayout(BoxLayout.Y_AXIS));

		mAudioLabel.getStyle().setAlignment(Label.LEFT);
		mAudioLabel.setPreferredSize(new Dimension(MediaPhone.COMPONENT_SIZE_SMALL, MediaPhone.COMPONENT_SIZE_SMALL));

		mImageLabel.getStyle().setAlignment(Label.CENTER);
		mImageLabel.setPreferredSize(new Dimension(160, 120)); // TODO: globals

		Style style = mTextArea.getStyle();
		style.setAlignment(Label.CENTER);
		style.setBorder(null);
		mTextArea.setGrowByContent(true);
		mTextArea.setEditable(false);
		mTextArea.setFocusable(false);

		addComponent(mAudioLabel);
		addComponent(mImageLabel);
		addComponent(mTextArea);

		addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_BACK),
				MediaPhone.id.button_finished_playback));
		addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_REPLAY),
				MediaPhone.id.button_replay_narrative));
		addCommandListener(this);
	}

	public void show() {
		startPlayback();
		super.show();
	}

	public void startPlayback() {
		setTitle(MediaPhone.getString(L10nConstants.keys.TITLE_PLAYBACK));

		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimer = new Timer();

		int nextStartTime = 0;
		Vector allFrames = mNarrative.getFrames();
		for (int i = mStartingFrame, n = allFrames.size(); i < n; i++) {

			final Frame currentFrame = (Frame) allFrames.elementAt(i);
			final boolean hasAudio = currentFrame.getAudio() != null;

			mTimer.schedule(new TimerTask() {
				public void run() {
					if (currentFrame.getImage() != null) {
						// TODO: size to globals
						mImageLabel.setIcon(ImageCacheUtilities.getScaledCachedImage(currentFrame.getImage(), 160, 120));
					} else {
						mImageLabel.setIcon(null);
					}

					if (hasAudio) {
						mAudioLabel.setIcon(ImageCacheUtilities.getScaledCachedSquareImage(
								MediaPhone.drawable.ic_audio_playback, MediaPhone.COMPONENT_SIZE_SMALL));
						playAudio(currentFrame.getAudio());
					} else {
						mAudioLabel.setIcon(null);
					}

					if (currentFrame.getText() != null) {
						mTextArea.setText(currentFrame.getText());
					} else {
						mTextArea.setText("");
					}

					repaint();
				}
			}, nextStartTime);

			if (hasAudio) {
				nextStartTime += currentFrame.getAudioDurationMilliseconds();
			} else {
				nextStartTime += MediaPhone.DEFAULT_FRAME_DURATION;
			}
		}

		mTimer.schedule(new TimerTask() {
			public void run() {
				setTitle(MediaPhone.getString(L10nConstants.keys.TITLE_PLAYBACK_COMPLETE));
				closePlayer();
				mStartingFrame = 0;
			}
		}, nextStartTime);
	}

	public void actionPerformed(ActionEvent event) {
		switch (event.getCommand().getId()) {
			case MediaPhone.id.button_finished_playback:
				closePlayer();
				MediaPhoneMidlet.getInstance().showNarrativeListForm();
				break;
			case MediaPhone.id.button_replay_narrative:
				closePlayer();
				mStartingFrame = 0;
				startPlayback();
				break;
		}
	}

	private void closePlayer() {
		try {
			if (mPlayer != null) {
				mPlayer.stop();
				mPlayer.close();
				mPlayer = null;
			}
		} catch (Exception e) {
		}
		try {
			mInputStream.close();
		} catch (Exception e) {
		}
		try {
			mFileConnection.close();
		} catch (Exception e) {
		}
	}

	private void playAudio(String fileName) {
		try {
			closePlayer();
			mFileConnection = (FileConnection) Connector.open(fileName, Connector.READ);
			mInputStream = mFileConnection.openInputStream();
			mPlayer = Manager.createPlayer(mInputStream, "audio/amr");
			if (mPlayer != null) {
				mPlayer.prefetch();
				mPlayer.start();
			}
		} catch (Exception e) {
			if (MediaPhone.DEBUG) {
				mAudioLabel.setText("Error: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
