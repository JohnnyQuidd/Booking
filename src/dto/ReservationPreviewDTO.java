package dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import model.ReservationStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReservationPreviewDTO {
	private Long id;
	private String username;
	private String apartmentName;
	private LocalDate rentFrom;
	private LocalDate rentUntil;
	private String message;
	private Long apartmentId;
	private ReservationStatus reservationStatus;
}
