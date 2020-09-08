package services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dao.AdminDAO;
import dao.ApartmentDAO;
import dao.HostDAO;
import dao.ReservationDAO;
import dao.UserDAO;
import dto.HostDTO;
import dto.UserPreviewDTO;
import model.Apartment;
import model.Host;
import model.Reservation;
import model.User;
import util.UsernameUniqueness;

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
		
		if(context.getAttribute("adminDAO") == null) {
			context.setAttribute("adminDAO", new AdminDAO(context.getRealPath("")));
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
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response registerNewHost(HostDTO hostDTO) {
		
		if(!UsernameUniqueness.isUsernameUnique(hostDTO.getUsername(), context)) {
			return Response.status(403).entity("Host name is not unique, try another one").build();
		}
		
		Host host = Host.builder()
				.username(hostDTO.getUsername())
				.password(hostDTO.getPassword())
				.firstName(hostDTO.getFirstName())
				.lastName(hostDTO.getLastName())
				.gender(hostDTO.getGender())
				.active(true)
				.build();
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		if(hostDAO.addNewHost(host)) {
			context.setAttribute("hostDAO", hostDAO);
			return Response.status(201).entity("Created").build();
		}
		
		return Response.status(500).entity("An error occurred while persisting a host").build();
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
		List<UserPreviewDTO> usersDTO = new ArrayList<>();
		List<Apartment> apartments = host.getApartmentsForRent();
		
		if(apartments != null) {
			Map<String, User> users = new HashMap<>();
			for(Apartment apartment : apartments) {
				addUserFromEachReservation(apartment.getReservations(), users);
			}
			
			usersDTO = makeUserDTO(users.values());
		}

		return usersDTO;
	}
	
	private void addUserFromEachReservation(List<Reservation> reservations, Map<String, User> users) {
		if(reservations != null && users != null) {
			for(Reservation reservation : reservations) {
				if(!users.containsKey(reservation.getUser().getUsername())) {
					users.put(reservation.getUser().getUsername(), reservation.getUser());
				}
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
