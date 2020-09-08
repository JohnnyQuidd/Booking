package model;

import java.util.ArrayList;
import java.util.List;

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
public class Host {
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private String gender;
	private boolean active;
	@Builder.Default 
	private List<Apartment> apartmentsForRent = new ArrayList<>();
}
