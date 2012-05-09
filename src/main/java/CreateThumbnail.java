import java.awt.image.BufferedImage;

import loci.formats.MetadataTools;
import loci.formats.gui.AWTImageTools;
import loci.formats.gui.BufferedImageReader;
import loci.formats.gui.BufferedImageWriter;
import loci.formats.meta.IMetadata;
import ome.xml.model.primitives.PositiveInteger;

/**
 * This file is used in the 'publish' cron job. Takes a given tiff file and
 * creates a thumbnail. Used to create thumbnails for Forward website.
 * 
 * @author Eric Alexander
 */
public class CreateThumbnail {

	public static void main(final String[] args) throws Exception {
		// Usage: java CreateThumbnail /current/path/to/tiff
		// /desired/path/to/thumbnail
		if (args.length == 2) {
			final String tiffFile = args[0];
			final String thumbnailPath = args[1];

			if (!tiffFile.endsWith(".ome.tiff")) {
				System.err.println("Error: " + tiffFile +
					" does not have the proper extension for OME-TIFF.");
				return;
			}

			readThumbnail(tiffFile, thumbnailPath);
		}
		else {
			System.err.println("Error: Improper arguments.");
			System.err.println("Usage: java CreateThumbnail "
				+ "/current/path/to/tiff /desired/path/to/thumbnail");
		}
	}

	/**
	 * Creates a thumbnail for a given tiff file.
	 * 
	 * @param filePath path to the tiff file
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
