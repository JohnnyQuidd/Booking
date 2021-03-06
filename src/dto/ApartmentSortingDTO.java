package dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.Apartment;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApartmentSortingDTO {
	private String criteria;
	private String apartmentName;
	private int numberOfRooms;
	private int numberOfGuests;
	@Builder.Default
	private List<Apartment> apartments = new ArrayList<>();
	
}
