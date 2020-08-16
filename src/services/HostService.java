package services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dao.ApartmentDAO;
import dao.HostDAO;
import dao.ReservationDAO;
import dao.UserDAO;
import dto.UserPreviewDTO;
import model.Apartment;
import model.Host;
import model.Reservation;
import model.User;

@Path("/host")
public class HostService {
	@Context
	ServletContext context;
	
	public HostService() {}
	
	@PostConstruct
	public void init() {
		if(context.getAttribute("userDAO") == null) {
			context.setAttribute("userDAO", new UserDAO(context.getRealPath("")));
		}
		
		if(context.getAttribute("hostDAO") == null) {
			context.setAttribute("hostDAO", new HostDAO(context.getRealPath("")));
		}
		
		if(context.getAttribute("apartmentDAO") == null) {
			context.setAttribute("apartmentDAO", new ApartmentDAO(context.getRealPath("")));
		}
		
		if(context.getAttribute("reservationDAO") == null) {
			context.setAttribute("reservationDAO", new ReservationDAO(context.getRealPath("")));
		}
	}
	
	@Path("/{hostUsername}/users")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsersThatReservedApartmentForGivenHost(@PathParam("hostUsername") String username) {
		if(!validHostUsername(username))
			return Response.status(404).entity("Host not found").build();
		
		
		List<UserPreviewDTO> users = getUsersThatReservedApartment(username);
		return Response.status(200).entity(users).build();
	}
	
	private boolean validHostUsername(String username) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		
		return hostDAO.findHostByUsername(username) != null ? true : false;
	}
	
	@Path("/{hostUsername}/{username}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSpecificUserThatRentedApartment(@PathParam("hostUsername") String hostUsername,
														@PathParam("username") String username) {
		List<UserPreviewDTO> users = getUsersThatReservedApartment(hostUsername);
		
		users.stream().filter(userDTO -> userDTO.getUsername().equals(username));
		
		if(users.size() > 0)
			return Response.status(200).entity(users).build();
		
		return Response.status(404).entity("User with username " + username + " hasn't reserved any of your apartments").build();
	}
	
	private List<UserPreviewDTO> getUsersThatReservedApartment(String username) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		List<Apartment> apartments = host.getApartmentsForRent();
		
		Map<String, User> users = new HashMap<>();
		for(Apartment apartment : apartments) {
			addUserFromEachReservation(apartment.getReservations(), users);
		}
		
		List<UserPreviewDTO> usersDTO = makeUserDTO(users.values());
		return usersDTO;
	}
	
	private void addUserFromEachReservation(List<Reservation> reservations, Map<String, User> users) {
		for(Reservation reservation : reservations) {
			if(!users.containsKey(reservation.getUser().getUsername())) {
				users.put(reservation.getUser().getUsername(), reservation.getUser());
			}
		}
	}
	
	private List<UserPreviewDTO> makeUserDTO(Collection<User> users) {
		List<UserPreviewDTO> userDTOs = new ArrayList<>();
		users.stream().forEach(user -> {
			UserPreviewDTO dto = UserPreviewDTO.builder()
					.username(user.getUsername())
					.firstName(user.getFirstName())
					.lastName(user.getLastName())
					.gender(user.getGender())
					.active(user.isActive())
					.build();
			
			userDTOs.add(dto);
		});
		
		return userDTOs;
	}
}
