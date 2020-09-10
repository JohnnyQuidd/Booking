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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import dto.ReservationFilterDTO;
import dto.ReservationSortingDTO;
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
				context.setAttribute("apartmentDAO", apartmentDAO);
				context.setAttribute("reservationDAO", reservationDAO);
				return Response.status(201).entity("Reservation successfully created").build();
			}
			return Response.status(500).entity("An error occurred while persisting apartment state").build();
		}
		
		return Response.status(500).entity("An error occurred while persisting reservation").build();
	}
	
	@Path("/sort")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sortReservations(ReservationSortingDTO reservationDTO) {
		List<Reservation> reservations = reservationDTO.getReservations();
		switch(reservationDTO.getCriteria()) {
		case "priceASC": reservations = sortReservationsByPriceASC(reservationDTO);
			break;
		case "priceDESC" : 	reservations = sortReservationsByPriceDESC(reservationDTO);
	}
		return Response.status(200).entity(reservations).build();
	}
	
	@Path("/filter")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response filterReservationsByStatus(ReservationFilterDTO filterDTO, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		Collection<Reservation> reservations = new ArrayList<>();
		
		switch(role) {
			case "admin" : reservations = getAllReservations(filterDTO.getStatus());
				break;
			case "host" : reservations = getReservationsForHost(username, filterDTO.getStatus());	
		}
		
		return Response.status(200).entity(reservations).build();
	}
	
	public List<Reservation> sortReservationsByPriceASC(ReservationSortingDTO reservationDTO) {
		List<Reservation> reservations = reservationDTO.getReservations();
		return reservations.stream().sorted(Comparator.comparingDouble(Reservation::getPrice)).collect(Collectors.toList());
	}
	
	public List<Reservation> sortReservationsByPriceDESC(ReservationSortingDTO reservationDTO) {
		List<Reservation> reservations = reservationDTO.getReservations();
		return reservations.stream().sorted(Comparator.comparingDouble(Reservation::getPrice).reversed()).collect(Collectors.toList());
	}
	
	public Collection<Reservation> getAllReservations(ReservationStatus status) {
		ReservationDAO reservationDAO = (ReservationDAO) context.getAttribute("reservationDAO");
		Collection<Reservation> reservations = reservationDAO.getReservations().values();
		
		reservations.stream().filter(reservation -> {
			return reservation.getReservationStatus().equals(status);
		});
		
		return reservations;
	}
	
	public Collection<Reservation> getReservationsForHost(String username, ReservationStatus status) {
		Collection<Reservation> reservations = new ArrayList<>();
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		if(host == null)
			return reservations;
		
		Collection<Apartment> apartments = host.getApartmentsForRent();
		for(Apartment apartment : apartments) {
			for(Reservation reservation : apartment.getReservations()) 
				reservations.add(reservation);
		}
		
		reservations.stream().filter(reservation -> {
			return reservation.getReservationStatus().equals(status);
		});
		
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
		reservation.setPrice(apartment.getPricePerNight());
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
	
	
	// I have to check each reservation individually rather than fetching rented dates which is never initialized in the first place
	private boolean apartmentHasOverlapingReservation(Apartment apartment, Reservation reservation) {
		if(apartment.getRentedDates() == null) return false;
		
		LocalDate current = reservation.getRentFrom();
		for(int i=0; i<reservation.getNumberOfNights(); i++) {
			current = current.plusDays(i);
			if(apartment.getRentedDates().contains(current))
				return true;
		}
		return false;
	}
	
	
	
}
