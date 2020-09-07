package dto;

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
public class ApartmentSearchDTO {
	private int numberOfRoomsMin;
	private int numberOfRoomsMax;
	private int numberOfGuests;
	private String city;
	private double priceMin;
	private double priceMax;
	private String availableDatesForRenting;
}
