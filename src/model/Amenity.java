package model;

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
public class Amenity {
	private Long id;
	private String amenity;
	private boolean active;
	
	public Amenity(String amenity) {
		this.amenity = amenity;
		this.active = true;
	}
}
