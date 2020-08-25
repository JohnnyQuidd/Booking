package services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
import dao.HostDAO;
import dao.ReservationDAO;
import dao.UserDAO;
import dto.ReservationFilterDTO;
import dto.ReservationSortingDTO;
import model.Apartment;
import model.Host;
import model.Reservation;
import model.ReservationStatus;

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
	
	
	
}
