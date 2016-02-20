package computations;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class EdgeDetect implements ComputationCode {

	
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
		
		//processing on image
		
		try {
			ImageIO.write(image, "gif", output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
