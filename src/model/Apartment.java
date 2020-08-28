package model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
	
	@Builder.Default
	private List<Date> availabeDatesForRenting = new ArrayList<>();
	
	@Builder.Default
	private List<LocalDateTime> rentedDates = new ArrayList<>();
	
	@Builder.Default
	private List<Comment> comments = new ArrayList<>();
	
	@Builder.Default
	private LocalTime checkInTime = LocalTime.of(14, 0);
	
	@Builder.Default
	private LocalTime checkOutTime  = LocalTime.of(10, 0);
	
	@JsonBackReference
	@Builder.Default
	private List<Reservation> reservations = new ArrayList<>();
	
	@Builder.Default
	private List<Amenity> amenities = new ArrayList<>();
	
	@Builder.Default
	private List<String> images = new ArrayList<>();
}
