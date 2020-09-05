package services;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import dao.AmenityDAO;
import dao.ApartmentDAO;
import dto.AmenityDTO;
import model.Admin;
import model.Amenity;

@Path("/amenity")
public class AmenityService {
	@Context
	ServletContext context;
	
	public AmenityService() {}
	
	@PostConstruct
	public void init() {
		if(context.getAttribute("amenityDAO") == null) {
			context.setAttribute("amenityDAO", new AmenityDAO(context.getRealPath("")));
		}
		
		if(context.getAttribute("adminDAO") == null) {
			context.setAttribute("adminDAO", new AdminDAO(context.getRealPath("")));
		}
		
		if(context.getAttribute("apartmentDAO") == null) {
			context.setAttribute("apartmentDAO", new ApartmentDAO(context.getRealPath("")));
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActiveAmenities() {
		AmenityDAO amenityDAO = (AmenityDAO) context.getAttribute("amenityDAO");
		Collection<Amenity> amenities = amenityDAO.getAmenities().values();
		
		amenities = amenities.stream().filter(Amenity::isActive).collect(Collectors.toList());
		
		return Response.status(200).entity(amenities).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addNewAmenity(AmenityDTO amenityDTO, @Context HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute("username");
		AmenityDAO amenityDAO = (AmenityDAO) context.getAttribute("amenityDAO");	
		AdminDAO adminDAO = (AdminDAO) context.getAttribute("adminDAO");
		
		Admin admin = adminDAO.findAdminByUsername(username);
		if(admin != null) {
			Amenity amenity = generateAmenityFromDTO(amenityDTO.getAmenityName(), amenityDAO);
			
			if(amenityDAO.addNewAmenity(amenity))
				return Response.status(201).entity("Amenity successfully created").build(); 
			
			return Response.status(500).entity("An Error occurred while saving amenity").build();
		}
		return Response.status(403).entity("You have no permission for adding amenities").build();
	}
	
	private Amenity generateAmenityFromDTO(String amenityName, AmenityDAO amenityDAO) {
		Long id = 0L;

		while (amenityDAO.getAmenities().containsKey(id)) {
			id = ThreadLocalRandom.current().nextLong(0, 10000);
		}
				
		Amenity amenity = Amenity.builder()
				.id(id)
				.amenity(amenityName)
				.active(true)
				.build();
		
		return amenity;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAmenity(Amenity amenityDTO) {
		AmenityDAO amenityDAO = (AmenityDAO) context.getAttribute("amenityDAO");
		Amenity amenity = amenityDAO.findAmenityById(amenityDTO.getId());
		
		amenity.setAmenity(amenityDTO.getAmenity());
		amenityDAO.saveAmenities();
		
		return Response.status(200).entity("OK").build();
	}
	
	@Path("/{id}")
	@DELETE
	public Response deleteAmenity(@PathParam("id") Long id){
		AmenityDAO amenityDAO = (AmenityDAO) context.getAttribute("amenityDAO");
		Amenity amenity = amenityDAO.findAmenityById(id);
		
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		
		// Removing amenity from other apartments prior to saving state of amenity because flag of object would be changed and
		// apartment would't be able to remove provided object from list of amenities
		apartmentDAO.removeDeletedAmenityFromEveryApartment(amenity);
		
		amenity.setActive(false);
		amenityDAO.saveAmenities();
		
		return Response.status(204).entity("No content").build();
	}
	
}
