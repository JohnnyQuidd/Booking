package model;

import java.time.LocalDateTime;

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
public class Reservation {
	private Long id;
	private User user;
	private Host host;
	private Apartment apartment;
	private LocalDateTime rentFrom;
	private LocalDateTime rentUntil;
	private LocalDateTime reservationDate;
	private String message;
	private double price;
	private boolean active;
	private int numberOfNights = 1;
	private ReservationStatus reservationStatus;
}
