package test;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;


public class ShittyURLClassLoader extends ClassLoader {

	
	private URL url;

	public ShittyURLClassLoader(URL u) {
		url = u;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		Class<?> result;

		try {
			URL codeURL = new URL(url + name.replace('.', '/') + ".class");
			HttpURLConnection codeCon = (HttpURLConnection) codeURL.openConnection();

			//codeCon.setRequestProperty("Cookie", cookie);
			codeCon.connect();

			InputStream classDefinition = codeCon.getInputStream();
			byte[] bytes = IOUtils.toByteArray(classDefinition);

			result = defineClass(name, bytes, 0, bytes.length);
		} catch(IOException e) {
			throw new ClassNotFoundException("IOException when trying to fetch code.");
		}

		return result;
	}
}
