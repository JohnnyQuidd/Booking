package dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.Amenity;
import model.ApartmentStatus;
import model.ApartmentType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentModifyDTO {
	private Long id;
	private String apartmentName;
	private ApartmentType apartmentType;
	private int numberOfRooms;
	private int numberOfGuests;
	private ApartmentStatus status;
	private double pricePerNight;
	private String availableDatesForRenting;
	
	// Address
	private String street;
	private int number;
	private String city;
	private int zipCode;
	// Other location related fields
	private double latitude;
	private double longitude;
	
	
	@Builder.Default
	private List<Amenity> amenities = new ArrayList<>();
	
	@Builder.Default
	private List<String> images = new ArrayList<>();
}
