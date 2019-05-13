package barCode;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Plugin(type = Command.class, menuPath = "Plugins>Bar code detection>Binarisation et redimension")
public class BinarisationEtRedimension<T extends RealType<T>> implements Command {

	@Parameter(persist = false)
	ImgPlus<T> inputImage;

	@Parameter(required = false)
	int threshold = 127;
	
	@Parameter(required = false)
	double angle = 0.0;
	
	@Parameter(required = false)
	int resizeW = 320;
	
	@Parameter(required = false)
	int resizeH = 239;
	
	@Parameter(required = false)
	int cropW = 320;
	
	@Parameter(required = false)
	int cropH = 100;

	@Parameter(type = ItemIO.OUTPUT)
	ImgPlus<UnsignedByteType> mask;

	@Parameter(type = ItemIO.OUTPUT)
	ImagePlus result;
	
	@Parameter(type = ItemIO.OUTPUT)
	ImagePlus result2;
	
	@Override
	public void run() {
		// dimensions holds the size of the input image in x and y
		long[] imageDimensions = new long[inputImage.numDimensions()];
		long[] maskDimensions = new long[inputImage.numDimensions()];
		inputImage.dimensions(imageDimensions);
		inputImage.dimensions(maskDimensions);
		maskDimensions[2] = 1;
		// Creation of the resulting image with the same size as the input image.

		mask = ImgPlus.wrap(ArrayImgs.unsignedBytes(maskDimensions));
		mask.setName(inputImage.getName() + "_Mask");

		// Two random cursor to visit all pixels in the input and output images.
		RandomAccess<T> inputImageCursor = inputImage.randomAccess();
		RandomAccess<UnsignedByteType> maskCursor = mask.randomAccess();

		long[] currentPosition = new long[imageDimensions.length];
		// 1. Parcourir toutes les lignes de l'image
		for (int i = 0; i < imageDimensions[0]; i++) {
			// 2. Parcourir toutes les colonnes de l'image
			for (int j = 0; j < imageDimensions[1]; j++) {
				// 3. affecter la position aux curseurs
				currentPosition[0] = i;
				currentPosition[1] = j;
				inputImageCursor.setPosition(currentPosition);
				maskCursor.setPosition(currentPosition);

				// 4. obtenir intensite de l'image a la position currentPosition
				if (inputImageCursor.get().getRealDouble() > threshold) {
					// 5. affecter pixel de l'image de sortie
					maskCursor.get().set(255);
				}
			}
		}		
		//Convertion ImgPlus en ImagePlus
		ImagePlus imagePlus = ImageJFunctions.wrap(mask, "imagePlus");
		//Convertion ImagePlus en ImageProcessor
		ImageProcessor imageProcessor = imagePlus.getProcessor();
		//set du processor imagePlus.setProcessor((imageProcessor.resize(320,190)));
		
		//Rotation de l'image
		imageProcessor.rotate(angle);
		
		//Redimension de l'image (calqué sur la taille de l'image 1)
		ImagePlus result = new ImagePlus("resizeImage", (imageProcessor.resize(resizeW,resizeH)));
		result.show();
		ImageProcessor imageProcessorResize = imageProcessor.resize(resizeW,resizeH);
		//Création du rectangle pour le découpage
		imageProcessorResize.setRoi(0,0,cropW,cropH);
		//Découpage de sorte à faciliter la projection
		ImagePlus result2 = new ImagePlus("cropImage", imageProcessorResize.crop());
		result2.show();
		
		
		
		
		
	}
	


}
