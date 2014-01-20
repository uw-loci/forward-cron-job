/*
 * #%L
 * Cron job for WiscScan/UW-Forward integration.
 * %%
 * Copyright (C) 2011 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.awt.image.BufferedImage;

import loci.formats.MetadataTools;
import loci.formats.gui.AWTImageTools;
import loci.formats.gui.BufferedImageReader;
import loci.formats.gui.BufferedImageWriter;
import loci.formats.meta.IMetadata;
import ome.xml.model.primitives.PositiveInteger;

/**
 * This file is used in the 'publish' cron job. Takes a given image file and
 * creates a thumbnail. Used to create thumbnails for Forward website.
 * 
 * @author Eric Alexander
 */
public class CreateThumbnail {

	public static void main(final String[] args) throws Exception {
		if (args.length == 2) {
			final String imageFile = args[0];
			final String thumbnailPath = args[1];

			readThumbnail(imageFile, thumbnailPath);
		}
		else {
			System.err.println("Error: Improper arguments.");
			System.err.println("Usage: java CreateThumbnail "
				+ "/current/path/to/image_data /desired/path/to/thumbnail");
		}
	}

	/**
	 * Creates a thumbnail for a given image file.
	 * 
	 * @param filePath path to the image file
	 * @param thumbnailPath intended path to thumbnail
	 */
	public static void readThumbnail(final String filePath,
		final String thumbnailPath) throws Exception
	{
		final BufferedImageReader reader = new BufferedImageReader();
		final IMetadata metadata = MetadataTools.createOMEXMLMetadata();
		reader.setMetadataStore(metadata);
		reader.setId(filePath);
		final int sizeZ = reader.getSizeZ();
		// int sizeC = reader.getSizeC();
		// int sizeT = reader.getSizeT();

		// Middle z-slice, laser channel, first timepoint
		final int index = reader.getIndex(sizeZ / 2, 1, 0);

		final BufferedImage image = reader.openImage(index);
		reader.close();

		// Change these lines if we decide to scale the image somehow.
		final int w = image.getWidth();
		final int h = image.getHeight();
		metadata.setPixelsSizeX(new PositiveInteger(w), 0);
		metadata.setPixelsSizeY(new PositiveInteger(h), 0);

		final BufferedImage thumbnail = AWTImageTools.scale(image, w, h, false);

		final BufferedImageWriter writer = new BufferedImageWriter();
		writer.setMetadataRetrieve(metadata);
		writer.setId(thumbnailPath);
		writer.saveImage(0, thumbnail);
		writer.close();
	}
}
