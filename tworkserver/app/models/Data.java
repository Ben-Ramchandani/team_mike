package models;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.Logger;
import play.data.validation.Constraints;
import twork.MyLogger;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;

@Entity
@Table(name = "all_data")
public class Data extends Model{

	//Constants
	@Transient
	public static final String TYPE_UTF8_FILE = "file"; //extend on this to allow more types
	@Transient
	public static final String TYPE_UTF8_IMMEDIATE = "imediate";
	@Transient
	public static final String TYPE_RAW_IMMEDIATE = "raw_i";
	@Transient
	public static final String TYPE_RAW_FILE = "raw_f";
	@Transient
	public static final int    MAX_IMMEDIATE_LENGTH = 512;

	
	@Id
	public UUID dataID;


	@Constraints.Required
	public String type;

	
	//We just use byte[]. Strings are UTF-8 encoded.
	@Constraints.Required
	public byte[] data;
	

	public String getStringContent() {
		if (type.equals(TYPE_UTF8_IMMEDIATE)) {
			return new String(data, StandardCharsets.UTF_8);
		} 
		else if (type.equals(TYPE_UTF8_FILE)) {
			try {
				
				byte[] encoded = Files.readAllBytes(Paths.get(new String(data, StandardCharsets.UTF_8)));
				return new String(encoded, StandardCharsets.UTF_8);
			}
			catch (Exception e) {
				MyLogger.warn("Exception reading data file.");
				e.printStackTrace();
				return null;
			}
			
		} else {
			MyLogger.warn("Tried to read raw data as string. Returning null.");
			return null;
		}
	}

	public byte[] getRawContent() {
		if (type.equals(TYPE_RAW_IMMEDIATE)) {
			return data;
		} else if(type.equals(TYPE_RAW_FILE)){
			try {
				return Files.readAllBytes(Paths.get(new String(data, StandardCharsets.UTF_8)));
			} catch(IOException e) {
				MyLogger.warn("Exception reading data file.");
				e.printStackTrace();
				return null;
			}
		} else {
			MyLogger.warn("Tried to read string data as raw. Returning null.");
			return null;
		}
	}


	//This needs to return the Data instance it makes
	//Does the data actually need an ID, or can we just reference it?
	public static Data storeString(String s, UUID dataID, UUID computationID) throws IOException {


		/* Try to store string s at location dataID.
		 * If s is small enough, will be immediate data; otherwise a file is created;
		 * If the file cannot be created it raises and exception.
		 * 
		 * If the dataID is already taken, returns false
		 * If it succeeds, returns true
		 */
		Data d;
		if (Ebean.find(Data.class, dataID) != null) {
			return null;
		} else {
			d = new Data();
			d.dataID = dataID;
			if (s.length() < MAX_IMMEDIATE_LENGTH) {
				d.type = TYPE_UTF8_IMMEDIATE;
				d.data = s.getBytes(StandardCharsets.UTF_8);
			} else {
				
				Path file = Paths.get("data/" + "s/" + dataID.toString());

				try {
					Files.createDirectories(file.getParent());
					Files.write(file, s.getBytes());
				}

				catch(IOException e) {
					e.printStackTrace();
				}
				d.type = TYPE_UTF8_FILE;
				d.data = file.toString().getBytes(StandardCharsets.UTF_8);
			}
		}
		d.save();
		return d;
	}

	public static Data store(File file, UUID dataID, UUID computationID) {
		Data d = new Data();
		d.dataID = dataID;
		d.type = TYPE_UTF8_FILE;

		//Path dest = Paths.get("data/" + computationID.toString() + "/f" + dataID.toString() + ".png");
		Path dest = Paths.get("data/" + "f/" + dataID.toString());

		try {
			Files.createDirectories(dest.getParent());
			Files.move(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		d.data = dest.toString().getBytes(StandardCharsets.UTF_8);

		d.save();
		System.out.printf("File Location: %s\n",d.data);
		return d;
	}

	public static UUID storeRaw(File file) {
		Data d = new Data();
		d.save();
		
		//Move the file to a new location
		Path dest = Paths.get("data/" + "f/" + d.dataID.toString());

		try {
			Files.createDirectories(dest.getParent());
			Files.move(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			Logger.warn("Failed to raw write file");
			e.printStackTrace();
			return null;
		}
		d.type = TYPE_RAW_FILE;
		d.data = dest.toString().getBytes(StandardCharsets.UTF_8);

		d.update();
		System.out.printf("Raw file Location: %s\n",d.data);
		return d.dataID;
	}

}
