/**
 * CreateThumbnail.java
 *
 * Author: Eric Alexander
 * 
 * This file is used in the 'publish' cron job. Takes a given tiff file and creates a thumbnail.
 * Used to create thumbnails for Forward website.
 */

import java.awt.image.BufferedImage;
import loci.formats.meta.IMetadata;
import loci.formats.MetadataTools;
import ome.xml.model.primitives.PositiveInteger;
import loci.formats.gui.BufferedImageReader;
import loci.formats.gui.AWTImageTools;
import loci.formats.gui.BufferedImageWriter;

@SuppressWarnings("unchecked")
public class CreateThumbnail {
    public static void main(String[] args) throws Exception {
        // Usage: java CreateThumbnail /current/path/to/tiff /desired/path/to/thumbnail
        if (args.length == 2) {
            String tiffFile = args[0];
            String thumbnailPath = args[1];
            
            if (!tiffFile.endsWith(".ome.tiff")) {
                System.err.println("Error: " + tiffFile + " does not have the proper extension for OME-TIFF.");
                return;
            }
            
            readThumbnail(tiffFile, thumbnailPath);
        }
        else {
            System.err.println("Error: Improper arguments.");
            System.err.println("Usage: java CreateThumbnail /current/path/to/tiff /desired/path/to/thumbnail");
        }
    }
    
    /**
     * Creates a thumbnail for a given tiff file.
     *
     * @param filePath  path to the tiff file
     * @param thumbnailPath  intended path to thumbnail
     */
    public static void readThumbnail(String filePath, String thumbnailPath) throws Exception {
        BufferedImageReader reader = new BufferedImageReader();
        IMetadata metadata = MetadataTools.createOMEXMLMetadata();
        reader.setMetadataStore(metadata);
        reader.setId(filePath);
        int sizeZ = reader.getSizeZ();
        //int sizeC = reader.getSizeC();
        //int sizeT = reader.getSizeT();
        int index = reader.getIndex(sizeZ / 2, 1, 0); // Middle z-slice, laser channel, first timepoint
        BufferedImage image = reader.openImage(index);
        reader.close();
        
        // Change these lines if we decide to scale the image somehow.
	int w = image.getWidth();
        int h = image.getHeight();
        metadata.setPixelsSizeX(new PositiveInteger(w), 0);
        metadata.setPixelsSizeY(new PositiveInteger(h), 0);
        
        BufferedImage thumbnail = AWTImageTools.scale(image, w, h, false);
        
        BufferedImageWriter writer = new BufferedImageWriter();
        writer.setMetadataRetrieve(metadata);
        writer.setId(thumbnailPath);
        writer.saveImage(0, thumbnail);
        writer.close();
    }
}
