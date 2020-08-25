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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dao.ApartmentDAO;
import dao.CommentDAO;
import dao.HostDAO;
import dto.ApartmentFilterDTO;
import dto.ApartmentModifyDTO;
import dto.ApartmentSearchDTO;
import dto.ApartmentSortingDTO;
import model.Amenity;
import model.Apartment;
import model.ApartmentStatus;
import model.ApartmentType;
import model.Host;

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
		
		if(context.getAttribute("hostDAO") == null) {
			context.setAttribute("hostDAO", new HostDAO(context.getRealPath("")));
		}
	}
	
	@Path("/all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAparments() {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		return Response.status(200).entity(apartments).build();
	}
	
	
	@Path("/active")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllActiveApartments() {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = getAllActiveApartments(apartmentDAO);
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/{apartmentName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActiveApartmentForProidedName(@PathParam("apartmentName") String apartmentName) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = getAllActiveApartments(apartmentDAO);
		
		apartments.stream().filter(apartment -> {
			return apartment.getApartmentName().equals(apartmentName);
		});
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/active/{hostName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActiveApartmentsForHost(@PathParam("hostName") String username) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		
		if(host == null)
			return Response.status(404).entity("Host not found").build();
		
		Collection<Apartment> apartments = host.getApartmentsForRent();
		apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.ACTIVE);
		});
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/inactive/{hostName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInactiveApartmentsForHost(@PathParam("hostName") String username) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		
		if(host == null)
			return Response.status(404).entity("Host not found").build();
		
		Collection<Apartment> apartments = host.getApartmentsForRent();
		apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.INACTIVE);
		});
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/{aparmentID}")
	@DELETE
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
	@POST
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
	
	@Path("/advandedSearch")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchApartments(ApartmentSearchDTO searchDTO) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = getAllActiveApartments(apartmentDAO);
		apartments = applySearchToCollection(apartments, searchDTO);
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/sort")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sortApartments(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> sortedApartments = apartmentsDTO.getApartments();
		switch(apartmentsDTO.getCriteria()) {
			case "priceASC": sortedApartments = sortApartmentsByPriceASC(apartmentsDTO);
				break;
			case "priceDESC" : 	sortedApartments = sortApartmentsByPriceDESC(apartmentsDTO);
		}
		return Response.status(200).entity(sortedApartments).build();
	}
	
	@Path("/filter")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response filterApartments(ApartmentFilterDTO apartmentDTO, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		Collection<Apartment> apartments = new ArrayList<>();
		
		
		switch(role) {
			case "admin" : apartments = getAllApartments();
				break;
			case "host" : apartments = getAllApartmentsForProvidedHost(username);	
		}
		
		if(apartmentDTO.getAmenities() != null)
			apartments = filterApartmentsByAmenities(apartments, apartmentDTO.getAmenities());
		
		if(apartmentDTO.getStatus() != null)
			apartments = filterApartmentsByStatus(apartments, apartmentDTO.getStatus());
		
		if(apartmentDTO.getType() != null)
			apartments = filterApartmentsByType(apartments, apartmentDTO.getType());
		
		return Response.status(200).entity(apartments).build();
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Apartment> getAllActiveApartments(ApartmentDAO apartmentDAO) {
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		apartments = (Collection<Apartment>) apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.ACTIVE);
		});
		
		return apartments;
	}
	
	private Collection<Apartment> getAllApartments() {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
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
	
	private Collection<Apartment> applySearchToCollection(Collection<Apartment> apartments, ApartmentSearchDTO dto) {
		if(dto.getNumberOfRoomsMin() >= 0 && dto.getNumberOfRoomsMax() >= dto.getNumberOfRoomsMin())
			apartments.stream().filter(apartment -> {
				return apartment.getNumberOfRooms() >= dto.getNumberOfRoomsMin() &&
						apartment.getNumberOfRooms() <= dto.getNumberOfRoomsMax();
			}).collect(Collectors.toList());
		
		if(dto.getPriceMin() >=0 && dto.getPriceMax() >= dto.getPriceMin()) {
			apartments.stream().filter(apartment -> {
				return apartment.getPricePerNight() >= dto.getPriceMin() &&
						apartment.getPricePerNight() <= dto.getPriceMax();
			}).collect(Collectors.toList());
		}
		
		if(dto.getNumberOfGuests() > 0) 
			apartments.stream().filter(apartment -> {
				return apartment.getNumberOfGuests() == dto.getNumberOfGuests();
			}).collect(Collectors.toList());
		
		if(!dto.getCity().equals(""))
			apartments.stream().filter(apartment -> {
				return apartment.getLocation().getAddress().getCity().equals(dto.getCity());
			}).collect(Collectors.toList());
		
		return apartments;
	}
	
	private List<Apartment> sortApartmentsByPriceASC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		return apartments.stream().sorted(Comparator.comparingDouble(Apartment::getPricePerNight)).collect(Collectors.toList());
	}
	
	
	private List<Apartment> sortApartmentsByPriceDESC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		return apartments.stream().sorted(Comparator.comparingDouble(Apartment::getPricePerNight).reversed()).collect(Collectors.toList());
	}
	
	private Collection<Apartment> getAllApartmentsForProvidedHost(String username) {
		Collection<Apartment> apartments = new ArrayList<>();
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		
		if(host != null)
			apartments = host.getApartmentsForRent();
		
		
		return apartments;
	}
	
	private Collection<Apartment> filterApartmentsByAmenities(Collection<Apartment> apartments, List<String> amenities) {
		apartments.stream().filter(apartment -> {
			List<Amenity> apertmentAmenities = apartment.getAmenities();
			return containsAllAmenities(apertmentAmenities, amenities);
		});
		return apartments;
	}
	
	public Collection<Apartment> filterApartmentsByStatus(Collection<Apartment> apartments, ApartmentStatus status) {
		return apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(status);
		}).collect(Collectors.toList());
	}
	
	public Collection<Apartment> filterApartmentsByType(Collection<Apartment> apartments, ApartmentType type) {
		return apartments.stream().filter(apartment -> {
			return apartment.getApartmentType().equals(type);
		}).collect(Collectors.toList());
	}
	
	private boolean containsAllAmenities(List<Amenity> apartmentAmenities, List<String> amenities) {
		for(String amenity : amenities) {
			boolean doesContainAmenity = false;
			for(Amenity apartmentAmenity : apartmentAmenities) {
				if(apartmentAmenity.getAmenity().equals(amenity))
					doesContainAmenity = true;
			}
			// stop searching for other amenities if current amenity is not present in apartment
			if(!doesContainAmenity)
				return doesContainAmenity;
		}
		
		// All amenities are present in apartment
		return true;
	}
	
	
	
}
