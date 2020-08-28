package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.Getter;
import lombok.Setter;
import model.Apartment;

@Getter
@Setter
public class ApartmentDAO {
	private Map<Long, Apartment> apartments = new HashMap<>();
	private String path;
	
	public ApartmentDAO(String contextPaht) {
		this.path = contextPaht;
		loadApartments();
	}
	
	@SuppressWarnings("unchecked")
	public void loadApartments() {
		String loadPath = this.path + "\\apartment.json";
        BufferedReader in = null;
        File file = null;
        try {
            file = new File(loadPath);
            in = new BufferedReader(new FileReader(file));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibilityChecker(
                    VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            TypeFactory factory = TypeFactory.defaultInstance();
            MapType type = factory.constructMapType(HashMap.class, Long.class, Apartment.class);

            objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            this.apartments = (Map<Long, Apartment>) objectMapper.readValue(file, type);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	public void saveApartments() {
		FileWriter fileWriter = null;
		File file = null;
		try {
			file = new File(this.path + "\\apartment.json");
			file.createNewFile();
			fileWriter = new FileWriter(file);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			//objectMapper.registerModule(new JavaTimeModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			String string = objectMapper.writeValueAsString(this.apartments);
			fileWriter.write(string);
		} catch (IOException eeee) {
			eeee.printStackTrace();
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
		}
	}
	
	public boolean addNewApartment(Apartment apartment) {
		try {
			this.apartments.put(apartment.getId(), apartment);
			saveApartments();
			return true;
		} catch(Exception e) {
			System.out.println("An error occured while saving apartments");
			return false;
		}
	}
	
	public boolean deleteApartment(Long id) {
		if(!this.apartments.containsKey(id))
			return false;
		try {
			Apartment apartment = this.apartments.get(id);
			apartment.setDeleted(true);
			saveApartments();
			return true;
		} catch(Exception e) {
			System.out.println("An error occurred while deliting apartment with ID: " + id);
			return false;
		}
	}
	
	public boolean modifyApartment(Apartment apartment) {
		try {
			saveApartments();
			return true;
		} catch(Exception e) {
			System.out.println("An error occurred while updating apartments");
			return false;
		}
	}
	
	public Apartment findApartmentById(Long id) {
		return this.apartments.get(id);
	}
	
}
