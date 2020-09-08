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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;
import lombok.Setter;
import model.Amenity;

@Getter
@Setter
public class AmenityDAO {
	private String path;
	private Map<Long, Amenity> amenities = new HashMap<>();
	
	public AmenityDAO(String contextPath) {
		path = contextPath;
		loadAmenities();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void loadAmenities() {
		String loadPath = this.path + "amenity.json";
        BufferedReader in = null;
        File file = null;
        try {
            file = new File(loadPath);
            in = new BufferedReader(new FileReader(file));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibilityChecker(
                    VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            TypeFactory factory = TypeFactory.defaultInstance();
            MapType type = factory.constructMapType(HashMap.class, Long.class, Amenity.class);

            objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.registerModule(new JavaTimeModule());
            this.amenities = (Map<Long, Amenity>) objectMapper.readValue(file, type);
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
	
	@SuppressWarnings("deprecation")
	public void saveAmenities() {
		FileWriter fileWriter = null;
		File file = null;
		try {
			file = new File(this.path + "amenity.json");
			file.createNewFile();
			fileWriter = new FileWriter(file);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			String string = objectMapper.writeValueAsString(this.amenities);
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
	
	public boolean addNewAmenity(Amenity amenity) {
		try {
			this.amenities.put(amenity.getId(), amenity);
			saveAmenities();
			return true;
		} catch(Exception e) {
			System.out.println("An error occured while saving amenities");
			return false;
		}
	}
	
	public Amenity findAmenityByName(String amenityName) {
		for(Amenity amenity : this.amenities.values()) {
			if(amenity.getAmenity().equals(amenityName))
				return amenity;
		}
		
		return null;
	}
	
	public Amenity findAmenityById(Long id) {
		for(Amenity amenity : this.amenities.values()) {
			if(amenity.getId().equals(id))
				return amenity;
		}
		
		return null;
	}
	
}
