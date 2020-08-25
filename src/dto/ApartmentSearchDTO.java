package dto;

import java.time.LocalDateTime;
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
public class ApartmentSearchDTO {
	private int numberOfRoomsMin;
	private int numberOfRoomsMax;
	private int numberOfGuests;
	private String city;
	private double priceMin;
	private double priceMax;
	
	@Builder.Default
	private List<LocalDateTime> availabeDatesForRenting = new ArrayList<>();

}
