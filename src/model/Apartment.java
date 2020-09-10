package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Apartment {
	private Long id;
	private String apartmentName;
	private ApartmentType apartmentType;
	private int numberOfRooms;
	private int numberOfGuests;
	private Location location;
	private ApartmentStatus status;
	private double pricePerNight;
	private String hostName;
	private boolean deleted;
	
	private List<LocalDate> availabeDatesForRenting;
	
	private List<LocalDate> rentedDates;
	
	private List<Comment> comments;
	
	private LocalTime checkInTime;

	private LocalTime checkOutTime;
	
	private List<Reservation> reservations;
	
	private List<Amenity> amenities;
	
	private List<String> images;
	
}
