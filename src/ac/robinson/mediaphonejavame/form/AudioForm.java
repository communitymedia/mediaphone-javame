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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.RecordControl;

import ac.robinson.mediaphonejavame.MediaPhone;
import ac.robinson.mediaphonejavame.localization.L10nConstants;
import ac.robinson.mediaphonejavame.util.StringUtilities;

import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * Records, plays back and saves a voice recording.
 */
public class AudioForm extends Form implements ActionListener, PlayerListener {

	private FrameEditorForm mFrameEditorForm;

	private Player mRecordingPlayer;
	private Player mPlaybackPlayer;
	private RecordControl mRecordControl;

	private ByteArrayOutputStream mRecordingOutput;
	private int mRecordingDuration;

	private Timer mTimer;

	private TextArea mStatusText;

	// TODO: load previous audio here, allow selecting from library, and allow deletion
	public AudioForm(FrameEditorForm frameEditorForm) {
		super(MediaPhone.getString(L10nConstants.keys.TITLE_AUDIO));
		mFrameEditorForm = frameEditorForm;

		setLayout(new BorderLayout());
		setScrollable(false);

		mStatusText = new TextArea(MediaPhone.getString(L10nConstants.keys.HINT_START_RECORDING));
		mStatusText.setEditable(false);
		addComponent(BorderLayout.CENTER, mStatusText);

		addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_CANCEL),
				MediaPhone.id.button_cancel_audio));
		addCommandListener(this);

		initialiseRecorder();
	}

	private void createRecorder() {
		try {
			mRecordingPlayer = Manager.createPlayer("capture://audio?encoding=audio/amr");
			mRecordingPlayer.realize();
			mRecordControl = (RecordControl) (mRecordingPlayer.getControl("RecordControl"));
		} catch (Exception e) {
		}
	}

	private void initialiseRecorder() {
		try {
			createRecorder();
			if (mRecordControl != null) {
				addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_START),
						MediaPhone.id.button_start_audio));
				// recordControl.setRecordSizeLimit(300000);
				mRecordingPlayer.start();
			} else {
				mStatusText.setText(MediaPhone.getString(L10nConstants.keys.ERROR_AUDIO_RECORDING_NOT_SUPPORTED));
			}
		} catch (Exception e) {
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.ERROR_AUDIO_RECORDING_NOT_SUPPORTED));
		}
	}

	private void releaseRecorder() {
		if (mRecordingPlayer != null) {
			mRecordingPlayer.close();
			mRecordingPlayer = null;
			mRecordControl = null;
		}
	}

	private void releasePlayer() {
		if (mPlaybackPlayer != null) {
			mPlaybackPlayer.close();
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimer = new Timer();

		switch (event.getCommand().getId()) {
			case MediaPhone.id.button_start_audio:
				try {
					if (mRecordingPlayer == null) {
						createRecorder(); // shouldn't fail, because if we get here recording is supported
					}
					mRecordingOutput = new ByteArrayOutputStream();
					mRecordControl.setRecordStream(mRecordingOutput);
					mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_RECORDING_IN_PROGRESS));
					removeAllCommands();
					addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_CANCEL),
							MediaPhone.id.button_abort_audio));
					addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_STOP),
							MediaPhone.id.button_stop_audio));
					mRecordingPlayer.start();
					mRecordControl.startRecord();

					mTimer.scheduleAtFixedRate(new TimerTask() {
						public void run() {
							mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_RECORDING_IN_PROGRESS)
									+ ": "
									+ StringUtilities.millisecondsToTimeString((int) (mRecordingPlayer.getDuration() / 1000)));
						}
					}, 0, 500);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case MediaPhone.id.button_abort_audio:
				releaseRecorder();
				mFrameEditorForm.show();
				break;
			case MediaPhone.id.button_stop_audio:
				try {
					mRecordingPlayer.stop();
					mRecordControl.stopRecord();
					mRecordControl.commit(); // TODO: can pause/resume here

					// TODO: getDuration doesn't work on all platforms (see Nokia 5800 for example)
					mRecordingDuration = (int) (mRecordingPlayer.getDuration() / 1000);

					releaseRecorder();
					removeAllCommands();
					addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_CANCEL),
							MediaPhone.id.button_cancel_audio));
					addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_PLAY),
							MediaPhone.id.button_play_audio));
					addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_SAVE),
							MediaPhone.id.button_save_audio));
					mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_RECORDING_COMPLETE) + ": "
							+ StringUtilities.millisecondsToTimeString(mRecordingDuration));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case MediaPhone.id.button_play_audio:
				if (mPlaybackPlayer == null || mPlaybackPlayer.getState() != Player.STARTED) {
					releasePlayer();
					ByteArrayInputStream recordedInputStream = null;
					try {
						mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_PLAYBACK_IN_PROGRESS));
						recordedInputStream = new ByteArrayInputStream(mRecordingOutput.toByteArray());
						mPlaybackPlayer = Manager.createPlayer(recordedInputStream, "audio/amr");
						mPlaybackPlayer.prefetch();
						mPlaybackPlayer.addPlayerListener(this);
						mPlaybackPlayer.start();

						mTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_PLAYBACK_IN_PROGRESS)
										+ ": "
										+ StringUtilities.millisecondsToTimeString((int) (mPlaybackPlayer.getDuration() / 1000)));
							}
						}, 0, 500);
					} catch (Exception e) {
						try {
							recordedInputStream.close();
						} catch (Exception e2) {
						}
						mStatusText.setText(MediaPhone.getString(L10nConstants.keys.ERROR_PLAYING_AUDIO));
						if (MediaPhone.DEBUG) {
							e.printStackTrace();
						}
					}
				}
				break;
			case MediaPhone.id.button_save_audio:
				releasePlayer();
				mFrameEditorForm.setCurrentAudio(saveRecording(), mRecordingDuration);
				mFrameEditorForm.show();
				break;
			case MediaPhone.id.button_cancel_audio:
				releasePlayer();
				mFrameEditorForm.show();
				break;
		}
	}

	private String saveRecording() {
		String outputFile = MediaPhone.AUDIO_PATH + MediaPhone.getString(L10nConstants.keys.APP_NAME) + "-"
				+ Integer.toHexString((int) System.currentTimeMillis() & 0xffffffff) + ".amr";
		FileConnection fileConnection = null;
		OutputStream outputStream = null;
		try {
			fileConnection = (FileConnection) Connector.open(outputFile, Connector.READ_WRITE); // *must* be r/w
			if (!fileConnection.exists()) {
				fileConnection.create();
			}
			outputStream = fileConnection.openOutputStream();
			outputStream.write(mRecordingOutput.toByteArray());
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_AUDIO_SAVED));
		} catch (Exception e) {
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.ERROR_SAVING_AUDIO));
			if (MediaPhone.DEBUG) {
				e.printStackTrace();
			}
		} finally {
			try {
				outputStream.close();
			} catch (Exception e) {
			}
			try {
				fileConnection.close();
			} catch (Exception e) {
			}
		}
		return outputFile;
	}

	public void playerUpdate(Player player, String event, Object eventData) {
		if (event.equals(PlayerListener.STARTED)) {
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_PLAYBACK_IN_PROGRESS));
		} else if (event.equals(PlayerListener.END_OF_MEDIA)) {
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_PLAYBACK_COMPLETE) + ": "
					+ StringUtilities.millisecondsToTimeString(mRecordingDuration));
			releasePlayer();
		} else if (event.equals(PlayerListener.STOPPED)) {
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_PLAYBACK_STOPPED) + ": "
					+ StringUtilities.millisecondsToTimeString(mRecordingDuration));
			releasePlayer();
		}
	}
}
