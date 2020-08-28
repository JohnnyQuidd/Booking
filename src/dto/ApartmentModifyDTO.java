package dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.Amenity;
import model.ApartmentStatus;
import model.ApartmentType;
import model.Location;
import model.Reservation;

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
	private Location location;
	private ApartmentStatus status;
	private double pricePerNight;
	
	@Builder.Default
	private List<Date> availabeDatesForRenting = new ArrayList<>();
	
	@Builder.Default
	private List<LocalDateTime> rentedDates = new ArrayList<>();

	
	@Builder.Default
	private List<Reservation> reservations = new ArrayList<>();
	
	@Builder.Default
	private List<Amenity> amenities = new ArrayList<>();
	
	@Builder.Default
	private List<String> images = new ArrayList<>();
}
