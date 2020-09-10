package model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Reservation {
	private Long id;
	private User user;
	private Long apartmentId;
	private LocalDate rentFrom;
	private LocalDate rentUntil;
	private String message;
	private double price;
	private boolean active;
	private ReservationStatus reservationStatus;
	
	@Builder.Default
	private int numberOfNights = 1;
}
