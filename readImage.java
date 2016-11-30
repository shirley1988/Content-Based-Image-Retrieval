
/* 
 * class readImage: retrieve image from file, get intensity and colorCode data from the image file, initiate
 * both intensityMatrix and colorCodeMatrix.
*/

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;

import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

@SuppressWarnings("unused")
public class readImage {
	int imageCount = 0;
	// store intensity distribution of a specific image
	double intensityBins[] = new double[26];
	// store intensity distribution of 100 images from database
	double intensityMatrix[][] = new double[101][26];
	// store colorCode distribution of a specific image
	double colorCodeBins[] = new double[64];
	// store colorCode distribution of 100 images from database
	double colorCodeMatrix[][] = new double[101][64];

	/*
	 * Each image is retrieved from the file. The height and width are found for
	 * the image and the getIntensity and getColorCode methods are called.
	 */
	public readImage() {
		while (imageCount < 100) {
			// instantiate an bufferedImage object which is used to handle and
			// manipulate image data
			BufferedImage img = null;
			try {
				File f = null;
				//String curdir = Paths.get(".").toAbsolutePath().normalize().toString();
				f = new File("src/images/" + (imageCount + 1) + ".jpg");
				img = ImageIO.read(f);
			} catch (IOException e) {
				System.out.println("Error occurred when reading the file");
			}
			int height = img.getHeight();
			int width = img.getWidth();
			intensityBins = new double[26];
			// the first element in intensityBins is used ot store image size
			intensityBins[0] = height * width;
			getIntensity(img, height, width); // get intensityBins of an image
			getColorCode(img, height, width); // get colorCodeBins of an image
			// copy intensityBins and colorCodeBins data to intensityMatrix and
			// colorCodeMatrix
			intensityMatrix[imageCount] = Arrays.copyOf(intensityBins, 26);
			colorCodeMatrix[imageCount] = Arrays.copyOf(colorCodeBins, 64);
			imageCount++;
		}

		writeIntensity(); // write data in intensityMatrix to intensity.txt file
		writeColorCode(); // write data in colorCodeMatrix to colorcode.txt file
	}

	/*
	 * getIntensity: calculate intensity distribution of a bufferedImage object,
	 * and store intensity distribution to intensityBins.
	 */
	public void getIntensity(BufferedImage image, int height, int width) {

		int red, green, blue;
		int rgb;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Color c = new Color(image.getRGB(j, i));
				red = c.getRed();
				green = c.getGreen();
				blue = c.getBlue();
				rgb = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
				if (rgb > 240)
					rgb = 240;
				intensityBins[rgb / 10 + 1]++;
			}
		}
	}

	/*
	 * getColorCode: calculate colorCode distribution of a bufferedImage object
	 * and store it to colorCodeBins
	 */
	public void getColorCode(BufferedImage image, int height, int width) {
		colorCodeBins = new double[64];
		int red, green, blue;
		int cc;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Color c = new Color(image.getRGB(j, i));
				red = sigTwo(c.getRed());
				green = sigTwo(c.getGreen());
				blue = sigTwo(c.getBlue());
				cc = calColorCode(red, green, blue);
				colorCodeBins[cc]++;
			}
		}
	}

	/*
	 * sigTwo: return the most significant two digits of an integer
	 */
	private int sigTwo(int color) {
		return (color >> 6);
	}

	/*
	 * calColorCode: combine the most significant two bits of the three colors
	 */
	private int calColorCode(int red, int green, int blue) {
		red = red << 4;
		green = green << 2;
		int res = (red | green | blue);
		return res;
	}

	/*
	 * This method writes the contents of the intensity matrix to a file called
	 * intensity.txt
	 */
	public void writeIntensity() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("intensity.txt"), "utf-8"));
			for (int i = 0; i < 100; i++) {
				// write one row of intensityMatrix to file
				writeArrayToLine(writer, intensityMatrix[i]);
			}
			writer.flush(); // flushes the buffer
			writer.close(); // close the file
		} catch (IOException e) {
			System.out.println("Creating intensity.txt failed");
		}
		try {
			writer.close();
		} catch (Exception e) {
			System.out.println("close file intensity.txt failed");
		}
		return;
	}

	/*
	 * writeArrayToLine: helper to write a double array into file.
	 */
	private void writeArrayToLine(BufferedWriter writer, double[] array) {
		try {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					writer.write(" ");
				}
				writer.write(Double.toString(array[i]));
			}
			writer.newLine();
		} catch (IOException e) {
			System.out.println("Error write array to line");
		}
	}

	/*
	 * This method writes the contents of the colorCode matrix to a file named
	 * colorCodes.txt.
	 */
	public void writeColorCode() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("colorcode.txt"), "utf-8"));
			for (int i = 0; i < 100; i++) {
				// write one row of colorCodeMatrix to file
				writeArrayToLine(writer, colorCodeMatrix[i]);
			}
			writer.flush(); // flush the buffer
			writer.close(); // close the file
		} catch (IOException e) {
			System.out.println("Creating colorcode.txt failed");
		}
		try {
			writer.close();
		} catch (Exception e) {
			System.out.println("Close file colorcode.txt failed");
		}
		return;
	}

	/*
	 * main function
	 */
	public static void main(String[] args) {
		new readImage();
	}

}
