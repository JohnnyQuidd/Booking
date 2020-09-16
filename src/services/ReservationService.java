package services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import dto.NewReservation;
import dto.ReservationPreviewDTO;
import model.Apartment;
import model.Host;
import model.Reservation;
import model.ReservationStatus;
import model.User;

@Path("/reservation")
public class ReservationService {
	@Context
	ServletContext context;
	
	public ReservationService() {}
	
	@PostConstruct
	public void init() {
		if(context.getAttribute("reservationDAO") == null)
			context.setAttribute("reservationDAO", new ReservationDAO(context.getRealPath("")));
		
		if(context.getAttribute("userDAO") == null)
			context.setAttribute("userDAO", new UserDAO(context.getRealPath("")));
		
		if(context.getAttribute("hostDAO") == null)
			context.setAttribute("hostDAO", new HostDAO(context.getRealPath("")));
		
		if(context.getAttribute("adminDAO") == null)
			context.setAttribute("adminDAO", new AdminDAO(context.getRealPath("")));
		
		if(context.getAttribute("apartmentDAO") == null)
				context.setAttribute("apartmentDAO", new ApartmentDAO(context.getRealPath("")));
		
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllReservations() {
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Collection<Reservation> reservations = reservationDAO.getReservations().values();
		Collection<ReservationPreviewDTO> previewDTOs = formDtosOutOfModel(reservations);
		
		return Response.status(200).entity(previewDTOs).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createNewReservation(NewReservation dto, @Context HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute("username");
		UserDAO userDAO = (UserDAO) context.getAttribute("userDAO");
		User user = userDAO.findUserByUsername(username);
		
		if(user == null)
			return Response.status(403).entity("You have no permission to request a Reservation").build();
		
		Reservation reservation = makeReservationFromDTO(dto);
		if(!apartmentAvailableForReservation(reservation))
			return Response.status(401).entity("Apartment is not available for specified time").build();
		
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		if(reservationDAO.addNewReservation(reservation)) {
			
			ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
			if(apartmentDAO.addNewReservation(reservation)) {
				Apartment apartment = apartmentDAO.findApartmentById(dto.getApartmentId());
				
				HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
				Host host = hostDAO.findHostByUsername(apartment.getHostName());
				hostDAO.addNewUsername(host, dto.getUsername());
				
				context.setAttribute("hostDAO", hostDAO);
				context.setAttribute("apartmentDAO", apartmentDAO);
				context.setAttribute("reservationDAO", reservationDAO);
				return Response.status(201).entity("Reservation successfully created").build();
			}
			return Response.status(500).entity("An error occurred while persisting apartment state").build();
		}
		
		return Response.status(500).entity("An error occurred while persisting reservation").build();
	}
	
	@Path("/sort/{username}/{criteria}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sortReservations(@PathParam("username") String username, @PathParam("criteria") String criteria) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		if(host == null)
			return Response.status(404).entity("Host not found").build();
		
		
		if(host.getApartmentsForRent() != null) {
			Collection<Reservation> relevantReservations = getNonCreatedReservationsForHost(username);
			Collection<ReservationPreviewDTO> dtos = formDtosOutOfModel(relevantReservations);
			
			switch(criteria) {
				case "priceASC": dtos = sortReservationsByPriceASC(dtos);
				break;
				case "priceDESC" : 	dtos = sortReservationsByPriceDESC(dtos);
			}
			
			return Response.status(200).entity(dtos).build();
		}
		
		return Response.status(200).entity("Nothing found").build();
	}
	
	@Path("/sort/all/{criteria}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sortAllReservations(@PathParam("criteria") String criteria, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		if(!role.equals("admin"))
			return Response.status(403).entity("You have no permission to sort all reservations").build();
		
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		
		Collection<Reservation> relevantReservations = reservationDAO.getReservations().values();
		Collection<ReservationPreviewDTO> dtos = formDtosOutOfModel(relevantReservations);
		
		switch(criteria) {
			case "priceASC": dtos = sortReservationsByPriceASC(dtos);
				break;
			case "priceDESC" : 	dtos = sortReservationsByPriceDESC(dtos);
		}
		
		return Response.status(200).entity(dtos).build();
		
	}
	
	@Path("/sort/user/{username}/{criteria}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sortUsersReservations(@PathParam("username")String username, @PathParam("criteria") String criteria) {
		UserDAO userDAO = (UserDAO) context.getAttribute("userDAO");
		User user = userDAO.findUserByUsername(username);
		if(user == null) return Response.status(404).entity("User not found").build();
		
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Collection<Reservation> reservations = reservationDAO.getReservations().values();
		
		reservations = reservations.stream().filter(reservation -> reservation.getUser().getUsername().equals(username)).collect(Collectors.toList());
		Collection<ReservationPreviewDTO> dtos = formDtosOutOfModel(reservations);
		
		switch(criteria) {
			case "priceASC": dtos = sortReservationsByPriceASC(dtos);
				break;
			case "priceDESC" : 	dtos = sortReservationsByPriceDESC(dtos);
	}
		
		return Response.status(200).entity(dtos).build();
		
	}
	
	@Path("/filter/{status}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response filterReservationsByStatus(@PathParam("status") ReservationStatus status, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		Collection<Reservation> reservations = new ArrayList<>();
		
		switch(role) {
			case "admin" : reservations = getAllReservations(status);
				break;
			case "host" : reservations = getReservationsForHostAndStatus(username, status);	
		}
		
		Collection<ReservationPreviewDTO> dtos = formDtosOutOfModel(reservations);
		
		return Response.status(200).entity(dtos).build();
	}
	
	@Path("/{hostUsername}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCreatedReservationsForCertainHost(@PathParam("hostUsername") String username) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		if(host == null)
			return Response.status(404).entity("Host not found").build();
		
		
		if(host.getApartmentsForRent() != null) {
			ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
			List<Long> aprtmentsIdList = hostDAO.getApartmentIDsForHostUsername(username);
			
			Collection<Reservation> allReservations = reservationDAO.getReservations().values();
			Collection<Reservation> relevantReservations = new ArrayList<>();
			
			for(Long id : aprtmentsIdList) {
				for(Reservation r : allReservations) {
					if(r.getApartmentId().equals(id) && r.getReservationStatus().equals(ReservationStatus.CREATED))
						relevantReservations.add(r);
				}
			}
			
			Collection<ReservationPreviewDTO> dtos = formDtosOutOfModel(relevantReservations);
			return Response.status(200).entity(dtos).build();
			
		}
		
		return Response.status(200).build();
	}
	
	@Path("/review/{username}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFinishedAndDeclinedReservations(@PathParam("username") String username) {
		UserDAO userDAO = (UserDAO) context.getAttribute("userDAO");
		User user = userDAO.findUserByUsername(username);
		
		if(user == null) return Response.status(404).entity("User not found").build();
		
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Collection<Reservation> reservations = reservationDAO.getReservations().values();
		
		reservations = reservations.stream().filter(reservation -> {
			return reservation.getUser().getUsername().equals(username) && 
					(reservation.getReservationStatus().equals(ReservationStatus.FINISHED) ||
					 reservation.getReservationStatus().equals(ReservationStatus.DECLINED));
		}).collect(Collectors.toList());
		
		Collection<ReservationPreviewDTO> dtos = formDtosOutOfModel(reservations);
		
		return Response.status(200).entity(dtos).build();
	}
	
	@Path("/other/{hostUsername}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReservationsOtherThanCreatedForCertainHost(@PathParam("hostUsername") String username) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		if(host == null)
			return Response.status(404).entity("Host not found").build();
		
		
		if(host.getApartmentsForRent() != null) {
			ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
			List<Long> aprtmentsIdList = hostDAO.getApartmentIDsForHostUsername(username);
			
			Collection<Reservation> allReservations = reservationDAO.getReservations().values();
			Collection<Reservation> relevantReservations = new ArrayList<>();
			
			for(Long id : aprtmentsIdList) {
				for(Reservation r : allReservations) {
					if(r.getApartmentId().equals(id) && !r.getReservationStatus().equals(ReservationStatus.CREATED))
						relevantReservations.add(r);
				}
			}
			
			Collection<ReservationPreviewDTO> dtos = formDtosOutOfModel(relevantReservations);
			return Response.status(200).entity(dtos).build();
			
		}
		
		return Response.status(200).build();
	}
	
	@Path("/user/{username}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllReservationsForUser(@PathParam("username") String username) {
		UserDAO userDAO = (UserDAO) context.getAttribute("userDAO");
		User user = userDAO.findUserByUsername(username);
		if(user == null) return Response.status(404).entity("User not found").build();
		
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Collection<Reservation> reservations = reservationDAO.getReservations().values();
		
		reservations = reservations.stream().filter(reservation -> reservation.getUser().getUsername().equals(username)).collect(Collectors.toList());
		Collection<ReservationPreviewDTO> previewDTOs = formDtosOutOfModel(reservations);
		
		return Response.status(200).entity(previewDTOs).build();
	}
	
	@Path("/cancel/{id}")
	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	public Response cancelReservationRequest(@PathParam("id") Long id, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		
		if(!role.equals("user")) return Response.status(403).entity("You have no permission to cancel reservation request").build();
		UserDAO userDAO = (UserDAO) context.getAttribute("userDAO");
		User user = userDAO.findUserByUsername(username);
		
		if(user == null) return Response.status(404).entity("User not found").build();
		
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Reservation reservation = reservationDAO.findReservationById(id);
		
		if(reservation == null) return Response.status(404).entity("Reservation not found").build();
		
		reservation.setReservationStatus(ReservationStatus.CANCELED);
		
		if(reservationDAO.updateReservation(reservation)) {
			context.setAttribute("reservationDAO", reservationDAO);
			return Response.status(200).entity("Reservation successfully canceled").build();
		}
			
		
		return Response.status(500).entity("An error occurred while modifying reservation").build();
	}
	
	@Path("/accept/{reservationId}")
	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	public Response acceptReservationWithProvidedId(@PathParam("reservationId") Long id) {
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Reservation reservation = reservationDAO.findReservationById(id);
		
		if(reservation == null) return Response.status(404).entity("Reservation not found").build();
		
		reservation.setReservationStatus(ReservationStatus.ACCEPTED);
		if(reservationDAO.updateReservation(reservation)) {
			context.setAttribute("reservationDAO", reservationDAO);
			return Response.status(200).entity("Reservation accepted").build();
		}
		return Response.status(500).entity("An error occurred while updating reservation").build();
	}
	
	@Path("/decline/{reservationId}")
	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	public Response declineReservationWithProvidedId(@PathParam("reservationId") Long id) {
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Reservation reservation = reservationDAO.findReservationById(id);
		
		if(reservation == null) return Response.status(404).entity("Reservation not found").build();
		
		reservation.setReservationStatus(ReservationStatus.DECLINED);
		if(reservationDAO.updateReservation(reservation)) {
			context.setAttribute("reservationDAO", reservationDAO);
			return Response.status(200).entity("Reservation declined").build();
		}
		return Response.status(500).entity("An error occurred while updating reservation").build();
	}
	
	@Path("/finish/{reservationId}")
	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	public Response changeReservationStatusToFinished(@PathParam("reservationId") Long id) {
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Reservation reservation = reservationDAO.findReservationById(id);
		
		if(reservation == null) return Response.status(404).entity("Reservation not found").build();
		
		if(!reservation.getReservationStatus().equals(ReservationStatus.ACCEPTED))
			return Response.status(403).entity("Reservation is not accepted").build();
		
//		if(reservation.getRentUntil().isAfter(LocalDate.now()))
//			return Response.status(403).entity("Reservation is not finished yet").build();
		
		reservation.setReservationStatus(ReservationStatus.FINISHED);
		
		if(reservationDAO.updateReservation(reservation)) {
			context.setAttribute("reservationDAO", reservationDAO);
			return Response.status(200).entity("Reservation updated").build();
		}
		return Response.status(500).entity("An error occurred while updating reservation").build();
	}
	
	public List<ReservationPreviewDTO> sortReservationsByPriceASC(Collection<ReservationPreviewDTO> reservationDTO) {
		return reservationDTO.stream().sorted(Comparator.comparingDouble(ReservationPreviewDTO::getPrice)).collect(Collectors.toList());
	}
	
	public List<ReservationPreviewDTO> sortReservationsByPriceDESC(Collection<ReservationPreviewDTO> reservationDTO) {
		return reservationDTO.stream().sorted(Comparator.comparingDouble(ReservationPreviewDTO::getPrice).reversed()).collect(Collectors.toList());
	}
	
	public Collection<Reservation> getAllReservations(ReservationStatus status) {
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Collection<Reservation> reservations = reservationDAO.getReservations().values();
		
		reservations = reservations.stream().filter(reservation -> {
			return reservation.getReservationStatus().equals(status);
		}).collect(Collectors.toList());
		
		return reservations;
	}
	
	public Collection<Reservation> getNonCreatedReservationsForHost(String username) {
		Collection<Reservation> reservations = new ArrayList<>();
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		if(host == null)
			return reservations;
		
		Collection<Apartment> apartments = host.getApartmentsForRent();
		if(apartments != null ) {
			for(Apartment apartment : apartments) {
				if(reservationDAO.hasReservationForApartmentAndHost(apartment, username)) {
					reservations.addAll(reservationDAO.reservationForApartmentAndHost(apartment, username));
				}
			}
		}
		
		reservations = reservations.stream().filter(reservation -> !reservation.getReservationStatus().equals(ReservationStatus.CREATED)).collect(Collectors.toList());
		
		return reservations;
	}
	
	public Collection<Reservation> getReservationsForHostAndStatus(String username, ReservationStatus status) {
		Collection<Reservation> reservations = getNonCreatedReservationsForHost(username);
		
		reservations = reservations.stream().filter(reservation -> {
			return reservation.getReservationStatus().equals(status);
		}).collect(Collectors.toList());
		
		return reservations;
	}
	
	private Reservation makeReservationFromDTO(NewReservation dto) {
		Reservation reservation = Reservation.builder().build();
		UserDAO userDAO = (UserDAO) context.getAttribute("userDAO");
		User user = userDAO.findUserByUsername(dto.getUsername());
		reservation.setUser(user);
		reservation.setApartmentId(dto.getApartmentId());
		
		Long id = 0L;
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		while(reservationDAO.getReservations().containsKey(id))
			id = ThreadLocalRandom.current().nextLong(0, 65000);
		
		reservation.setId(id);
		reservation.setMessage(dto.getMessage());
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Apartment apartment = apartmentDAO.findApartmentById(dto.getApartmentId());
		double price = apartment.getPricePerNight() * dto.getNumberOfNights();
		reservation.setPrice(price);
		reservation.setReservationStatus(ReservationStatus.CREATED);
		reservation.setNumberOfNights(dto.getNumberOfNights());
		reservation.setActive(true);
		
		List<String> date = makeStringListOutOfString(dto.getDate());
		LocalDate rentFrom = convertStringToDate(date.get(0));
		LocalDate rentUntil = rentFrom.plusDays(dto.getNumberOfNights());
		
		reservation.setRentFrom(rentFrom);
		reservation.setRentUntil(rentUntil);
		
		return reservation;
	}
	
	private List<String> makeStringListOutOfString(String dateString) {
		List<String> dates = new ArrayList<>();
		String[] array = dateString.split(",");
		
		for(int i = 0; i<array.length; i++) {
			dates.add(array[i].trim());
		}
		
		return dates;
	}
	
	private LocalDate convertStringToDate(String string) {
		string = string.trim();
		String dateArray[] = string.split("/");
		int day = Integer.parseInt(dateArray[0]);
		int month = Integer.parseInt(dateArray[1]) + 1;
		int year = Integer.parseInt(dateArray[2]);
		
		LocalDate date =  LocalDate.of(year, month, day);
		return date;
	}
	
	private boolean apartmentAvailableForReservation(Reservation reservation) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Apartment apartment = apartmentDAO.findApartmentById(reservation.getApartmentId());
		if(apartment == null || apartment.getAvailabeDatesForRenting() == null)
			return false;
		
		LocalDate current = reservation.getRentFrom();
		for(int i=0; i<reservation.getNumberOfNights(); i++) {
			current = current.plusDays(i);
			if(!apartment.getAvailabeDatesForRenting().contains(current))
				return false;
		}
		
		boolean hasOverlapingReservation = apartmentHasOverlapingReservation(apartment, reservation);
		
		return true && !hasOverlapingReservation;
	}
	
	
	private boolean apartmentHasOverlapingReservation(Apartment apartment, Reservation reservation) {
		if(apartment.getReservations() == null) return false;		

		for(Reservation existingReservation : apartment.getReservations()) {
			if(reservation.getRentFrom().isAfter(existingReservation.getRentFrom()) &&
			   reservation.getRentFrom().isBefore(existingReservation.getRentUntil()))
				return true;
			
		}
		return false;
	}
	
	private Collection<ReservationPreviewDTO> formDtosOutOfModel(Collection<Reservation> reservations) {
		Collection<ReservationPreviewDTO> dtos = new ArrayList<>();
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		for(Reservation reservation : reservations) {
			Apartment apartment = apartmentDAO.findApartmentById(reservation.getApartmentId());
			ReservationPreviewDTO dto = ReservationPreviewDTO.builder()
					.username(reservation.getUser().getUsername())
					.rentFrom(reservation.getRentFrom())
					.rentUntil(reservation.getRentUntil())
					.message(reservation.getMessage())
					.reservationStatus(reservation.getReservationStatus())
					.apartmentName(apartment.getApartmentName())
					.id(reservation.getId())
					.apartmentId(apartment.getId())
					.price(reservation.getPrice())
					.build();
			
			dtos.add(dto);
		}
		
		return dtos;
	}
	
	
}
