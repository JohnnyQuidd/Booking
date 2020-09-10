package dto;

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
public class NewReservation {
	private String username;
	private Long apartmentId;
	private String date;
	private String message;
	private int numberOfNights;
}
