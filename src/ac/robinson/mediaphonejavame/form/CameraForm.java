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

import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import ac.robinson.mediaphonejavame.MediaPhone;
import ac.robinson.mediaphonejavame.localization.L10nConstants;
import ac.robinson.mediaphonejavame.util.ImageCacheUtilities;

import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.MediaComponent;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;

/**
 * Captures and saves a photo using the phone's camera and viewfinder.
 */
class CameraForm extends Form implements ActionListener {

	private FrameEditorForm mFrameEditorForm;

	private Player mCapturePlayer;
	private VideoControl mVideoControl;
	private MediaComponent mMediaComponent;
	private byte[] mCameraOutput = null;

	private Label mImagePreview = null;
	private TextArea mStatusText;

	// TODO: load previous photo here, allow selecting image from gallery, and allow deletion
	public CameraForm(FrameEditorForm frameEditorForm) {
		super(MediaPhone.getString(L10nConstants.keys.TITLE_CAMERA));
		mFrameEditorForm = frameEditorForm;

		setLayout(new BorderLayout());
		setScrollable(false);

		mStatusText = new TextArea(MediaPhone.getString(L10nConstants.keys.HINT_LOADING));
		mStatusText.setEditable(false);
		addComponent(BorderLayout.CENTER, mStatusText);

		addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_CANCEL),
				MediaPhone.id.button_cancel_picture));
		addCommandListener(this);

		initialiseCamera();
	}

	private void initialiseCamera() {
		try {
			// for a non-deprecated alternative, see: http://stackoverflow.com/questions/7742397
			// or: http://stackoverflow.com/questions/8652269
			try {
				mCapturePlayer = Manager.createPlayer("capture://image");
				mCapturePlayer.realize();
			} catch (Exception e) {
				mCapturePlayer = Manager.createPlayer("capture://video");
				mCapturePlayer.realize();
			}
			mVideoControl = (VideoControl) (mCapturePlayer.getControl("VideoControl"));
			if (mVideoControl != null) {
				mMediaComponent = new MediaComponent(mCapturePlayer);
				mMediaComponent.setFocusable(false);
				addComponent(BorderLayout.CENTER, mMediaComponent);
				addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_CAPTURE),
						MediaPhone.id.button_take_picture));
				mCapturePlayer.start();
				mStatusText.setText("");
			} else {
				mStatusText.setText(MediaPhone.getString(L10nConstants.keys.ERROR_CAMERA_NOT_SUPPORTED));
			}
		} catch (Exception e) {
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.ERROR_CAMERA_NOT_SUPPORTED));
		}
	}

	public void actionPerformed(ActionEvent event) {
		switch (event.getCommand().getId()) {
			case MediaPhone.id.button_take_picture:
				capturePhoto();
				break;
			case MediaPhone.id.button_save_picture:
				String imagePath = savePicture();
				ImageCacheUtilities.cacheImage(imagePath, mImagePreview.getIcon()); // saves a file permissions prompt
				mFrameEditorForm.setCurrentImage(imagePath);
				mImagePreview = null;
				mCameraOutput = null;
				mCapturePlayer.close();
				mCapturePlayer = null;
				mVideoControl = null;
				mMediaComponent = null;
				mFrameEditorForm.show();
				break;
			case MediaPhone.id.button_cancel_picture:
				if (mCapturePlayer != null) {
					mCapturePlayer.close();
					mCapturePlayer = null;
					mVideoControl = null;
					mMediaComponent = null;
				}
				mFrameEditorForm.show();
				break;
			case MediaPhone.id.button_retake_picture:
				removeComponent(mImagePreview);
				mImagePreview = null;
				mCameraOutput = null;
				addComponent(BorderLayout.CENTER, mMediaComponent);
				removeAllCommands();
				addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_CANCEL),
						MediaPhone.id.button_cancel_picture));
				addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_CAPTURE),
						MediaPhone.id.button_take_picture));
				try {
					mCapturePlayer.start();
				} catch (MediaException e) {
					e.printStackTrace();
				}
				mStatusText.setText("");
				break;
		}
	}

	private void capturePhoto() {
		try {
			// TODO: use thread here to speed up?
			try {
				// if we have a capture command, we know videoControl is not null
				mCameraOutput = mVideoControl.getSnapshot("encoding=jpeg&width=320&height=240");
			} catch (Exception e) {
				mCameraOutput = mVideoControl.getSnapshot(null);
			}
			mCapturePlayer.stop();
			removeComponent(mMediaComponent);
			mImagePreview = new Label(Image.createImage(mCameraOutput, 0, mCameraOutput.length));
			addComponent(BorderLayout.CENTER, mImagePreview);
			removeAllCommands();
			addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_RETAKE),
					MediaPhone.id.button_retake_picture));
			addCommand(new Command(MediaPhone.getString(L10nConstants.keys.BUTTON_SAVE),
					MediaPhone.id.button_save_picture));
		} catch (MediaException e) {
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.ERROR_TAKING_PHOTO));
			if (MediaPhone.DEBUG) {
				e.printStackTrace();
			}
		}
	}

	private String savePicture() {
		String outputFile = MediaPhone.IMAGES_PATH + MediaPhone.getString(L10nConstants.keys.APP_NAME) + "-"
				+ Integer.toHexString((int) System.currentTimeMillis() & 0xffffffff) + ".jpg";
		FileConnection fileConnection = null;
		OutputStream outputStream = null;
		try {
			String encodings = System.getProperty("video.snapshot.encodings");
			if (encodings == null || encodings.indexOf("jpg") < 0) {
				throw new Exception("JPEG encoding is not supported on this device");
			}

			fileConnection = (FileConnection) Connector.open(outputFile, Connector.READ_WRITE); // *must* be r/w
			if (!fileConnection.exists()) {
				fileConnection.create();
			}
			outputStream = fileConnection.openOutputStream();
			outputStream.write(mCameraOutput);
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.HINT_PHOTO_SAVED));
		} catch (Exception e) {
			mStatusText.setText(MediaPhone.getString(L10nConstants.keys.ERROR_SAVING_PHOTO));
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
}
