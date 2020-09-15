package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import model.Apartment;
import model.Host;

@Getter
@Setter
public class HostDAO {
	private String path;
	private Map<String, Host> hosts = new HashMap<>();
	
	public HostDAO(String contextPath) {
		path = contextPath;
		loadHosts();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void loadHosts() {
		String loadPath = this.path + "host.json";
        BufferedReader in = null;
        File file = null;
        try {
            file = new File(loadPath);
            in = new BufferedReader(new FileReader(file));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibilityChecker(
                    VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            TypeFactory factory = TypeFactory.defaultInstance();
            MapType type = factory.constructMapType(HashMap.class, String.class, Host.class);

            objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.registerModule(new JavaTimeModule());
            this.hosts = (Map<String, Host>) objectMapper.readValue(file, type);
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
	public void saveHosts() {
		FileWriter fileWriter = null;
		File file = null;
		try {
			file = new File(this.path + "host.json");
			file.createNewFile();
			fileWriter = new FileWriter(file);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			String string = objectMapper.writeValueAsString(this.hosts);
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
	
	public boolean addNewHost(Host host) {
		try {
			this.hosts.put(host.getUsername(), host);
			saveHosts();
			return true;
		} catch(Exception e) {
			System.out.println("An error occured while saving hosts");
			return false;
		}
	}
	
	public Host findHostByUsername(String username) {
		for(Host host : this.hosts.values()) {
			if(host.getUsername().equals(username)) {
				return host;
			}
				
		}
		return null;
	}
	
	public void addNewApartmentToHost(Host host, Apartment apartment) {
		if(host.getApartmentsForRent() == null)
			host.setApartmentsForRent(new ArrayList<Apartment>());
		
		host.getApartmentsForRent().add(apartment);
		saveHosts();
	}
	
	public boolean deleteApartmentWithId(Long id) {
		for(Host host : this.hosts.values()) {
			for(Apartment apartment : host.getApartmentsForRent()) {
				if(apartment.getId().equals(id)) {
					apartment.setDeleted(true);
					saveHosts();
					return true;
				}
			}
		}
		
		return false;
	}
	
	public List<Long> getApartmentIDsForHostUsername(String username) {
		List<Long> list = new ArrayList<>();
		Host host = findHostByUsername(username);
		if(host == null) return list;
		
		for(Apartment apartment : host.getApartmentsForRent()) {
			list.add(apartment.getId());
		}
		
		return list;
	}
	
	public void updateHost(Host host) {
		try {
			hosts.remove(host.getUsername());
			hosts.put(host.getUsername(), host);
			saveHosts();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addNewUsername(Host host, String username) {
		if(host.getUsersThatRented() == null)
			host.setUsersThatRented(new ArrayList<>());
		
		host.getUsersThatRented().add(username);
		updateHost(host);
	}
	
	
}
