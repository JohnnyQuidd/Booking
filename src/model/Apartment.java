package model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
	private List<LocalDateTime> availabeDatesForRenting = new ArrayList<>();
	private List<LocalDateTime> rentedDates = new ArrayList<>();
	private Host host;
	private List<Comment> comments = new ArrayList<>();
	private double pricePerNight;
	private LocalTime checkInTime = LocalTime.of(14, 0);
	private LocalTime checkOutTime  = LocalTime.of(10, 0);
	private ApartmentStatus status;
	private List<Reservation> reservations = new ArrayList<>();
	private List<Amenity> amenities = new ArrayList<>();
	private List<String> images = new ArrayList<>();
}
