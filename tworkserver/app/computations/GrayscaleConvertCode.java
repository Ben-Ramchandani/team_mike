package computations;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;


public class GrayscaleConvertCode implements ComputationCode {

	@Override
	public void run(InputStream input, OutputStream output) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(input);
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		for (int i = 0; i<height; i++) {
			for (int j = 0; j < width; j++) {
				Color c = new Color(image.getRGB(j, i));
				int red = (int)(c.getRed() * 0.299);
				int green = (int)(c.getGreen()* 0.587);
				int blue = (int)(c.getBlue()*0.114);
				
				Color gray = new Color(red+green+blue, red+green+blue, red+green+blue);
				image.setRGB(j, i, gray.getRGB());
			}
		}
		
		try {
			ImageIO.write(image, "png", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
