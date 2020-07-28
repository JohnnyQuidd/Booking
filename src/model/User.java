package model;

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
@SuppressWarnings("unused")
public class User {
	private Long id;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private String gender;
	private List<Apartment> rentedApartments = new ArrayList<>();
	private List<Reservation> reservations = new ArrayList<>();
	private boolean active;
}
