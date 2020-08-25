package dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.ApartmentStatus;
import model.ApartmentType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentFilterDTO {
	private ApartmentStatus status;
	private ApartmentType type;
	
	@Builder.Default
	private List<String> amenities = new ArrayList<>();
}
