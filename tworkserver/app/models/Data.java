package models;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import play.Logger;
import play.data.validation.Constraints;
import twork.MyLogger;

import com.avaje.ebean.Model;

@Entity
@Table(name = "all_data")
public class Data extends Model{

	@Transient
	public static final int    MAX_IMMEDIATE_LENGTH = 512;


	@Id
	public UUID dataID;


	@Constraints.Required
	public Boolean isFile;


	//We just use byte[]. Strings are UTF-8 encoded.
	//Files have their name UTF-8 encoded here.
	@Constraints.Required
	@Column(length=512)
	public byte[] data;


	public String getContentAsString() {
		return new String(getContent(), StandardCharsets.UTF_8);
	}

	public byte[] getContent() {
		if (!isFile) {
			return data;
		} else {
			try {
				return Files.readAllBytes(Paths.get(new String(data, StandardCharsets.UTF_8)));
			} catch(IOException e) {
				MyLogger.warn("Exception reading data file.");
				e.printStackTrace();
				return null;
			}
		}
	}


	public static UUID storeString(String s) {
		return store(s.getBytes(StandardCharsets.UTF_8));
	}


	public static UUID store(byte[] data) {
		Data d = new Data();
		d.save();

		if(data.length < MAX_IMMEDIATE_LENGTH) {
			d.data = data;
			d.isFile = false;
		} else {
			try {
				MyLogger.log("New file Location (due to immediate overflow)");
				Path dest = Paths.get("data/" + d.dataID.toString());
				Files.createDirectories(dest.getParent());
				Files.write(dest, data);

				d.data = dest.toString().getBytes(StandardCharsets.UTF_8);
				d.isFile = true;

			} catch (IOException e) {
				Logger.warn("Failed to raw write file");
				e.printStackTrace();
				return null;
			}
		}

		d.update();
		return d.dataID;
	}

	public static UUID store(File file) {
		Data d = new Data();
		d.save();

		//Move the file to a new location
		Path dest = Paths.get("data/" + d.dataID.toString());

		try {
			Files.createDirectories(dest.getParent());
			Files.move(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			Logger.warn("Failed to raw write file");
			e.printStackTrace();
			return null;
		}
		d.isFile = true;
		d.data = dest.toString().getBytes(StandardCharsets.UTF_8);

		d.update();
		MyLogger.log("New file Location (file store requested): " + new String(d.data, StandardCharsets.UTF_8));
		return d.dataID;
	}

}
