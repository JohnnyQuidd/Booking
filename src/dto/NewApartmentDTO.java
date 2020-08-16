package dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewApartmentDTO {
	private String apartmentName;
	private String apartmentType;
	private int numberOfRooms;
	private int numberOfGuests;
	private double pricePerNight;
	// Address
	private String street;
	private int number;
	private String city;
	private int zipCode;
	// Other location related fields
	private double lattitude;
	private double longitude;
	
	private List<LocalDateTime> availabeDatesForRenting = new ArrayList<>();
	private List<String> amenities = new ArrayList<>();
	private List<String> images = new ArrayList<>();	
}
