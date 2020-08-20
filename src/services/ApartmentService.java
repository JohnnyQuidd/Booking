package services;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dao.ApartmentDAO;
import dao.CommentDAO;
import dto.ApartmentModifyDTO;
import model.Apartment;
import model.ApartmentStatus;

@Path("/apartment")
public class ApartmentService {
	@Context
	ServletContext context;
	
	public ApartmentService() {}
	
	@PostConstruct
	public void init() {
		if(context.getAttribute("apartmentDAO") == null) {
			context.setAttribute("apartmentDAO", new ApartmentDAO(context.getRealPath("")));
		}
		
		if(context.getAttribute("commentDAO") == null) {
			context.setAttribute("commentDAO", new CommentDAO(context.getRealPath("")));
		}
	}
	
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAparments() {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		return Response.status(200).entity(apartments).build();
	}
	
	
	@Path("/active")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllActiveApartments() {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = getAllActiveApartments(apartmentDAO);
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/delete/{aparmentID}")
	public Response deleteApartmentById(@PathParam("apartmentID") Long id, @Context HttpServletRequest request) {
		if(request.getSession().getAttribute("role").equals("admin")) {
			ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
			if(apartmentDAO.deleteApartment(id)) {
				return Response.status(200).entity("OK").build();
			}
			return Response.status(404).entity("Apartment not found").build();
		}
		return Response.status(403).entity("You have no permission to delete apartments").build();
	}
	
	@Path("/modify")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyExistingApartment(ApartmentModifyDTO apartmentDTO) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Apartment apartment = apartmentDAO.findApartmentById(apartmentDTO.getId());
		if(apartment !=null) {
			modifyApartment(apartment, apartmentDTO);
			if(apartmentDAO.modifyApartment(apartment))
				return Response.status(200).entity("OK").build();
			
			return Response.status(500).entity("Server error has occurred").build();
		}
		return Response.status(404).entity("Can't modify apartment with provided ID").build();
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Apartment> getAllActiveApartments(ApartmentDAO apartmentDAO) {
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		apartments = (Collection<Apartment>) apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.ACTIVE);
		});
		
		return apartments;
	}
	
	private void modifyApartment(Apartment apartment, ApartmentModifyDTO dto) {
		if(!dto.getApartmentName().equals(""))
			apartment.setApartmentName(dto.getApartmentName());
		
		if(dto.getApartmentType() != null && dto.getApartmentType() != apartment.getApartmentType()) 
			apartment.setApartmentType(dto.getApartmentType());
		
		if(dto.getNumberOfRooms() != apartment.getNumberOfRooms())
			apartment.setNumberOfRooms(dto.getNumberOfRooms());
		
		if(dto.getNumberOfGuests() != apartment.getNumberOfGuests())
			apartment.setNumberOfGuests(dto.getNumberOfGuests());
		
		if(dto.getLocation() != null && dto.getLocation() != apartment.getLocation()) 
			apartment.setLocation(dto.getLocation());
		
		if(dto.getStatus() != null && dto.getStatus() != apartment.getStatus())
			apartment.setStatus(dto.getStatus());
		
		if(dto.getPricePerNight() != apartment.getPricePerNight())
			apartment.setPricePerNight(dto.getPricePerNight());
		
		if(dto.getAvailabeDatesForRenting() != null) 
			apartment.setAvailabeDatesForRenting(dto.getAvailabeDatesForRenting());
		
		if(dto.getRentedDates() != null) 
			apartment.setRentedDates(dto.getRentedDates());
		
		if(dto.getReservations() != null)
			apartment.setReservations(dto.getReservations());
		
		if(dto.getAmenities() != null)
			apartment.setAmenities(dto.getAmenities());
		
		if(dto.getImages() != null)
			apartment.setImages(dto.getImages());
	}
	
	
	
}
