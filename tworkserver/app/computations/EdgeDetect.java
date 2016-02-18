package computations;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.imageio.ImageIO;

public class EdgeDetect implements ComputationCode{

	String host = "ec2-52-36-182-104.us-west-2.compute.amazonaws.com:9000/";
	
	@Override
	public void run(InputStream input, OutputStream output) {
	
		BufferedImage image = null;
		try {
			image = ImageIO.read(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//processing on image
		
		try {
			ImageIO.write(image, "jpeg", output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
