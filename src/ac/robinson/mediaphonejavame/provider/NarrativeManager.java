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

package ac.robinson.mediaphonejavame.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import ac.robinson.mediaphonejavame.MediaPhone;

/**
 * Manages the narratives. The narratives are saved to the record store in XML format. The XML is read in at launch and
 * saved after each modification.
 */
public class NarrativeManager {

	private static NarrativeManager mInstance = new NarrativeManager();
	private Vector mNarratives = new Vector();

	private NarrativeManager() {
	}

	public static NarrativeManager getInstance() {
		return mInstance;
	}

	public Vector getNarratives() {
		if (mNarratives.isEmpty()) {
			loadNarratives();
		}
		return mNarratives;
	}

	public void addNarrative(Narrative narrative) {
		getNarratives();
		if (!mNarratives.contains(narrative)) {
			mNarratives.addElement(narrative);
		}
		saveNarratives();
	}

	public void removeNarrative(Narrative narrative) {
		getNarratives();
		if (mNarratives.contains(narrative)) {
			mNarratives.removeElement(narrative);
		}
		saveNarratives();
	}

	private void saveNarratives() {
		// some phones corrupt rewrites of the same data, so delete the existing key rather than replacing
		try {
			RecordStore.deleteRecordStore(MediaPhone.NARRATIVES_KEY);
		} catch (Exception e) {
		}

		MediaPhone.log("Saving narrative XML...");
		RecordStore recordStore = null;
		try {
			recordStore = RecordStore.openRecordStore(MediaPhone.NARRATIVES_KEY, true);
			byte[] narratives = generateNarratives();
			recordStore.addRecord(narratives, 0, narratives.length);
			int size = recordStore.getNumRecords();
			MediaPhone.log("Narratives saved (" + size + " records)");
		} catch (RecordStoreFullException e) {
			MediaPhone.log("Narrative saving failed (RecordStore full)");
			e.printStackTrace();
		} catch (RecordStoreException e) {
			MediaPhone.log("Narrative saving failed (RecordStore)");
			e.printStackTrace();
		} finally {
			try {
				recordStore.closeRecordStore();
			} catch (Exception e) {
			}
		}
	}

	private void loadNarratives() {
		RecordStore recordStore = null;
		try {
			recordStore = RecordStore.openRecordStore(MediaPhone.NARRATIVES_KEY, true);
			int size = recordStore.getNumRecords();
			if (size == 1) {
				parseNarratives(recordStore.getRecord(1));
			} else {
				MediaPhone.log("No narratives found to load (" + size + " records)");
			}
		} catch (RecordStoreException e) {
			MediaPhone.log("Narrative loading failed (RecordStore)");
			e.printStackTrace();
		} finally {
			try {
				recordStore.closeRecordStore();
			} catch (Exception e) {
			}
		}
	}

	private void parseNarratives(byte[] narrativesData) {
		ByteArrayInputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(narrativesData);
			KXmlParser xmlParser = new KXmlParser();
			xmlParser.setInput(inputStream, "ISO-8859-1");
			xmlParser.nextTag();
			xmlParser.require(KXmlParser.START_TAG, null, "narratives");

			while (xmlParser.nextTag() == KXmlParser.START_TAG) {

				xmlParser.require(KXmlParser.START_TAG, null, "narrative");
				Narrative currentNarrative = new Narrative();
				currentNarrative.setGUID(xmlParser.getAttributeValue(null, "guid"));

				while (xmlParser.nextTag() == KXmlParser.START_TAG) {

					xmlParser.require(KXmlParser.START_TAG, null, "frame");
					Frame currentFrame = new Frame();

					while (xmlParser.nextTag() == KXmlParser.START_TAG) {
						if (xmlParser.getName().equals("image")) {
							String imagePath = xmlParser.getAttributeValue(null, "path");
							currentFrame.setImage(imagePath.length() > 0 ? imagePath : null);
							xmlParser.nextTag();
							xmlParser.require(KXmlParser.END_TAG, null, "image");
						} else if (xmlParser.getName().equals("audio")) {
							String audioPath = xmlParser.getAttributeValue(null, "path");
							currentFrame.setAudio(audioPath.length() > 0 ? audioPath : null);
							String audioDuration = xmlParser.getAttributeValue(null, "duration");
							if (audioDuration.length() > 0) {
								int audioDurationMilliseconds = 0;
								try {
									audioDurationMilliseconds = Integer.parseInt(audioDuration);
								} catch (NumberFormatException e) {
								}
								currentFrame.setAudioDurationMilliseconds(audioDurationMilliseconds);
							}
							xmlParser.nextTag();
							xmlParser.require(KXmlParser.END_TAG, null, "audio");
						} else if (xmlParser.getName().equals("text")) {
							String textContent = xmlParser.getAttributeValue(null, "content");
							currentFrame.setText(textContent.length() > 0 ? textContent : null);
							xmlParser.nextTag();
							xmlParser.require(KXmlParser.END_TAG, null, "text");
						}
					}

					currentNarrative.addFrame(currentFrame);
					xmlParser.require(KXmlParser.END_TAG, null, "frame");
				}

				xmlParser.require(KXmlParser.END_TAG, null, "narrative");
				mNarratives.addElement(currentNarrative);
			}

			xmlParser.require(KXmlParser.END_TAG, null, "narratives");
			MediaPhone.log("Loaded " + mNarratives.size() + " narratives");

		} catch (Exception e) {
			MediaPhone.log("Narrative loading failed (XML)");
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
			}
		}
	}

	private byte[] generateNarratives() {
		Document xmlDocument = new Document();
		xmlDocument.setEncoding("ISO-8859-1");
		Element rootElement = xmlDocument.createElement(null, "narratives");
		xmlDocument.addChild(Node.ELEMENT, rootElement);
		for (int i = 0, n = mNarratives.size(); i < n; i++) {
			Narrative currentNarrative = (Narrative) mNarratives.elementAt(i);
			Element narrativeElement = rootElement.createElement(null, "narrative");
			narrativeElement.setAttribute(null, "guid", currentNarrative.getGUID());
			rootElement.addChild(Node.ELEMENT, narrativeElement);

			Vector currentFrames = currentNarrative.getFrames();
			for (int j = 0, p = currentFrames.size(); j < p; j++) {
				Frame frame = (Frame) currentFrames.elementAt(j);

				Element frameElement = narrativeElement.createElement(null, "frame");
				narrativeElement.addChild(Node.ELEMENT, frameElement);

				if (frame.getImage() != null) {
					Element imageElement = frameElement.createElement(null, "image");
					imageElement.setAttribute(null, "path", frame.getImage());
					frameElement.addChild(Node.ELEMENT, imageElement);
				}

				if (frame.getAudio() != null) {
					Element audioElement = frameElement.createElement(null, "audio");
					audioElement.setAttribute(null, "path", frame.getAudio());
					audioElement.setAttribute(null, "duration", Integer.toString(frame.getAudioDurationMilliseconds()));
					frameElement.addChild(Node.ELEMENT, audioElement);
				}

				if (frame.getText() != null) {
					Element textElement = frameElement.createElement(null, "text");
					textElement.setAttribute(null, "content", frame.getText());
					frameElement.addChild(Node.ELEMENT, textElement);
				}
			}
		}

		ByteArrayOutputStream outputStream = null;
		OutputStreamWriter streamWriter = null;
		KXmlSerializer xmlSerializer = new KXmlSerializer();
		try {
			outputStream = new ByteArrayOutputStream();
			streamWriter = new OutputStreamWriter(outputStream);
			xmlSerializer.setOutput(streamWriter);
			xmlDocument.write(xmlSerializer);
			MediaPhone.log("Saved " + mNarratives.size() + " narratives");
		} catch (IOException e) {
			MediaPhone.log("Narrative saving failed (ByteStream)");
			e.printStackTrace();
		} finally {
			try {
				streamWriter.close();
			} catch (Exception e) {
			}
		}

		return outputStream.toByteArray();
	}
}
