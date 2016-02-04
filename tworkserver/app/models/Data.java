package models;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

@Entity
@Table(name = "all_data")
public class Data extends Model{


	//TODO:

	/*
	 * This needs serious refactoring.
	 * Maybe getting rid of it altogether
	 * Better to use a file system and find a smart way to reference and move data around
	 */
	
	public static final String TYPE_FILE = "file"; //extend on this to allow more types
	public static final String TYPE_IMMEDIATE = "imediate";
	public static final int    MAX_IMMEDIATE_LENGTH = 512;
	@Id
	public Long dataID;



	@Constraints.Required
	public String type;
	//Can either be immediate or file.
	//If it is file, will open the file on the file system and send that

	@Constraints.Required
	public String data;

	public String getContent() {
		if (type.equals(TYPE_IMMEDIATE)) {
			return data;
		} 
		else if (type.equals(TYPE_FILE)) {
			try {
				//here I assume the files have default encoding.
				byte[] encoded = Files.readAllBytes(Paths.get(data));
				return new String(encoded);
			}
			catch (IOException e) {
				//arbitrary choice of returning empty string.
				//this is actually a very big problem.
				return new String("");
			}
		}

		//I don't see why it needs this to compile, all the branches return something.
		return null;
	}

	public static boolean store(String s, Long dataID, Long computationID) throws IOException {
		/* Try to store string s at location dataID.
		 * If s is small enough, will be immediate data; otherwise a file is created;
		 * If the file cannot be created it raises and exception.
		 * 
		 * If the dataID is already taken, returns false
		 * If it succeeds, returns true
		 */
		
		if (Ebean.find(Data.class, dataID) != null) {
			return false;
		}

		else {
			Data d = new Data();
			d.dataID = dataID;
			if (s.length() < MAX_IMMEDIATE_LENGTH) {
				d.type = TYPE_IMMEDIATE;
				d.data = s;
			} else {
				Path file = Paths.get(computationID.toString() + '/' + dataID.toString());
				Files.write(file, s.getBytes());
				d.type = TYPE_FILE;
				d.data = file.toString(); //TODO test this
			}
		}
		return true;
	}


}


