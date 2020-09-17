package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import model.Apartment;
import model.Reservation;

@Getter
@Setter
public class ReservationDAO {
	private String path;
	private Map<Long, Reservation> reservations = new HashMap<>();
	
	public ReservationDAO(String contextPath) {
		path = contextPath;
		loadReservations();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void loadReservations() {
		String loadPath = this.path + "reservation.json";
        BufferedReader in = null;
        File file = null;
        try {
            file = new File(loadPath);
            if(!file.exists())
            	file.createNewFile();
            in = new BufferedReader(new FileReader(file));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibilityChecker(
                    VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            TypeFactory factory = TypeFactory.defaultInstance();
            MapType type = factory.constructMapType(HashMap.class, Long.class, Reservation.class);

            objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            this.reservations = (Map<Long, Reservation>) objectMapper.readValue(file, type);
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
	public void saveReservations() {
		FileWriter fileWriter = null;
		File file = null;
		try {
			file = new File(this.path + "reservation.json");
			file.createNewFile();
			fileWriter = new FileWriter(file);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			String string = objectMapper.writeValueAsString(this.reservations);
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
	
	public boolean addNewReservation(Reservation reservation) {
		try {
			this.reservations.put(reservation.getId(), reservation);
			saveReservations();
			return true;
		} catch(Exception e) {
			System.out.println("An error occured while saving reservations");
			return false;
		}
	}
	
	public Reservation findReservationById(Long id) {
		for(Reservation reservation : this.reservations.values()) {
			if(reservation.getId().equals(id))
				return reservation;
		}
		
		return null;
	}
	
	public boolean updateReservation(Reservation reservation) {
		try {
			Reservation old = this.findReservationById(reservation.getId());
			this.reservations.remove(old.getId());
			this.reservations.put(reservation.getId(), reservation);
			saveReservations();
			return true;
		} catch(Exception e) {
			System.out.println("Couldn't save reservations");
			return false;
		}
	}
	

	
	public boolean hasReservationForApartmentAndHost(Apartment apartment, String hostName) {
		for(Reservation reservation : reservations.values()) {
			if(reservation.getApartmentId().equals(apartment.getId()) && apartment.getHostName().equals(hostName))
				return true;
		}
		return false;
	}
	
	public Collection<Reservation> reservationForApartmentAndHost(Apartment apartment, String hostName) {
		Collection<Reservation> reservationsList = new ArrayList<>();
		
		for(Reservation reservation : reservations.values()) {
			if(reservation.getApartmentId().equals(apartment.getId()) && apartment.getHostName().equals(hostName)) {
				reservationsList.add(reservation);
			}
		}
		
		return reservationsList;
	}
	
}
