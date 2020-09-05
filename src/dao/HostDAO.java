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
	
	@SuppressWarnings("unchecked")
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
			//objectMapper.registerModule(new JavaTimeModule());
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
}
