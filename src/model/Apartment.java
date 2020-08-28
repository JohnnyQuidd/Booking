package model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
	
	private List<Date> availabeDatesForRenting;
	
	private List<LocalDateTime> rentedDates;
	
	private List<Comment> comments;
	
	private LocalTime checkInTim;

	private LocalTime checkOutTime;
	
	@JsonBackReference
	private List<Reservation> reservations;
	
	private List<Amenity> amenities;
	
	private List<String> images;
	
}
