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

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import ac.robinson.mediaphonejavame.MediaPhone;
import ac.robinson.mediaphonejavame.MediaPhone.drawable;

import com.sun.lwuit.Image;

public class ImageCacheUtilities {
	private static final Hashtable cached_icons = new Hashtable();

	public static Image getScaledCachedSquareImage(String drawableId, int size) {
		Image cachedImage = getCachedImage(drawableId);
		if (cachedImage != null) {
			if (cachedImage.getWidth() >= size || cachedImage.getHeight() >= size) { // scale down a larger version
				return cachedImage.scaled(size, size);
			} else {
				cached_icons.remove(drawableId); // if we've got a smaller version cached, reload
				cachedImage = null;
			}
		}

		Image newImage = loadImage(drawableId);
		if (newImage != null) {
			cacheImage(drawableId, newImage.scaled(size, size));
			newImage = null;
			return getCachedImage(drawableId);
		}

		return null;
	}

	public static Image getScaledCachedImage(String drawableId, int width, int height) {
		Image cachedImage = getCachedImage(drawableId);
		if (cachedImage != null) {
			if (cachedImage.getWidth() >= width || cachedImage.getHeight() >= height) {
				return cachedImage.scaledSmallerRatio(width, height);
			} else {
				cached_icons.remove(drawableId);
				cachedImage = null;
			}
		}

		Image newImage = loadImage(drawableId);
		if (newImage != null) {
			cacheImage(drawableId, newImage.scaledSmallerRatio(width, height));
			newImage = null;
			return getCachedImage(drawableId);
		}

		return null;
	}

	public static void cacheImage(String drawableId, Image image) {
		if (image != null) {
			cached_icons.put(drawableId, new WeakReference(image));
		}
	}

	private static Image getCachedImage(String drawableId) {
		if (drawableId == null) {
			return null;
		}

		if (cached_icons.containsKey(drawableId)) {
			WeakReference imageReference = (WeakReference) cached_icons.get(drawableId);
			final Object imageObject = imageReference.get();
			if (imageObject != null) {
				return (Image) imageObject;
			} else {
				cached_icons.remove(drawableId);
			}
		}

		return null;
	}

	private static Image loadImage(String drawableId) {
		if (drawableId == null) {
			return null;
		}

		if (drawableId.startsWith(drawable.DRAWABLE_ROOT)) {
			try {
				return Image.createImage(drawableId);
			} catch (Exception e) {
				if (MediaPhone.DEBUG) {
					MediaPhone.log("Error loading image from " + drawableId);
					e.printStackTrace();
				}
			}
		} else {
			FileConnection fileConnection = null;
			InputStream inputStream = null;
			try {
				fileConnection = (FileConnection) Connector.open(drawableId, Connector.READ);
				inputStream = fileConnection.openInputStream();

				byte[] imageBytes = new byte[(int) fileConnection.fileSize()];
				inputStream.read(imageBytes, 0, imageBytes.length);

				return Image.createImage(imageBytes, 0, imageBytes.length);
			} catch (Exception e) {
				if (MediaPhone.DEBUG) {
					MediaPhone.log("Error opening image from " + drawableId);
					e.printStackTrace();
				}
			} finally {
				try {
					inputStream.close();
				} catch (Exception e) {
				}
				try {
					fileConnection.close();
				} catch (Exception e) {
				}
			}
		}

		return null;
	}
}
