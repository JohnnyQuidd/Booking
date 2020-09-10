package dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
	private double latitude;
	private double longitude;
	
	// day/month/year, day/month/year
	// in JS January is 1, in Java January is 0
	private String availableDatesForRenting;
	private List<String> amenities = new ArrayList<>();
	private List<String> images = new ArrayList<>();	
}
