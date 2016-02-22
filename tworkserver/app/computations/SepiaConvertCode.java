package computations;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class SepiaConvertCode implements ComputationCode {

	@Override
	public void run(InputStream input, OutputStream output) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(input);
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int width = image.getWidth();
		int height = image.getHeight();
		int sepiaDepth = 20;
		int sepiaIntensity = 80;
		
		for (int i = 0; i<height; i++) {
			for (int j = 0; j < width; j++) {
				Color c = new Color(image.getRGB(j, i));
				int red = (int)(c.getRed());
				int green = (int)(c.getGreen());
				int blue = (int)(c.getBlue());
				
				int gry = (red + green + blue) / 3;
	            red = green = blue = gry;
	            red = red + (sepiaDepth * 2);
	            green = green + sepiaDepth;

	            if (red > 255) {
	                red = 255;
	            }
	            if (green > 255) {
	                green = 255;
	            }
	            if (blue > 255) {
	                blue = 255;
	            }

	            // Darken blue color to increase sepia effect
	            blue -= sepiaIntensity;

	            // normalize if out of bounds
	            if (blue < 0) {
	                blue = 0;
	            }
	            if (blue > 255) {
	                blue = 255;
	            }
				image.setRGB(j, i, new Color(red,green,blue).getRGB());
			}
		}
		
		try {
			ImageIO.write(image, "gif", output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}