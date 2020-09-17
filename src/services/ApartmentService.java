package services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

import dao.AmenityDAO;
import dao.ApartmentDAO;
import dao.CommentDAO;
import dao.HostDAO;
import dto.ApartmentFilterDTO;
import dto.ApartmentModifyDTO;
import dto.ApartmentSearchDTO;
import dto.ApartmentSortIDS;
import dto.NewApartmentDTO;
import model.Address;
import model.Amenity;
import model.Apartment;
import model.ApartmentStatus;
import model.ApartmentType;
import model.Host;
import model.Location;

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
		
		if(context.getAttribute("amenityDAO") == null) {
			context.setAttribute("amenityDAO", new AmenityDAO(context.getRealPath("")));
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postNewApartment(NewApartmentDTO apartmentDTO, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		
		if(!role.equals("host"))
			return Response.status(403).entity("You have no permission for adding apartments").build();
		
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		
		if(host == null)
			return Response.status(404).entity("Host is not found").build();
		
		Apartment apartment = makeApartmentOutOfDTO(apartmentDTO);
		
		Long id = 0L;
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Map<Long, Apartment> map = apartmentDAO.getApartments();
		
		while (map.containsKey(id)) {
			id = ThreadLocalRandom.current().nextLong(0, 65000);
		}
		
		apartment.setHostName(host.getUsername());
		apartment.setStatus(ApartmentStatus.INACTIVE);
		apartment.setDeleted(false);
		apartment.setId(id);
		
		hostDAO.addNewApartmentToHost(host, apartment);
		
		if(apartmentDAO.addNewApartment(apartment)) {
			context.setAttribute("apartmentDAO", apartmentDAO);
			return Response.status(201).entity("Apartment successfully added").build();
		}
		
		return Response.status(500).entity("An error occurred while saving apartment").build();
	}
	
	@Path("/activate/{apartmentId}")
	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	public Response activateApartment(@PathParam("apartmentId") Long id, @Context HttpServletRequest request) {
		if(!request.getSession().getAttribute("role").equals("admin"))
			return Response.status(403).entity("You have no permission to modify apartment's status").build();
		
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Apartment apartment = apartmentDAO.findApartmentById(id);
		
		if(apartment == null || apartment.isDeleted())
			return Response.status(404).entity("Apartment not found").build();
		
		if(apartmentDAO.activateApartment(apartment)) {
			context.setAttribute("apartmentDAO", apartmentDAO);
			return Response.status(200).entity("Apartment activated").build();
		}
			
		
		return Response.status(500).entity("Apartment couldn't be persisted").build();
	}
	


	@Path("/all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAparments() {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		apartments = apartments.stream().filter(apartment -> !apartment.isDeleted()).collect(Collectors.toList());
		
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
	
	@Path("/inactive")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllInactiveApartments(@Context HttpServletRequest request) {
		if(!request.getSession().getAttribute("role").equals("admin"))
			return Response.status(403).entity("You have no permission to preview inactive apartments").build();
		
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		apartments = apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.INACTIVE) && !apartment.isDeleted();
		}).collect(Collectors.toList());
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/{apartmentID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getApartmentForProidedId(@PathParam("apartmentID") Long apartmentID) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		
		apartments = apartments.stream().filter(apartment -> {
			return apartment.getId().equals(apartmentID);
		}).collect(Collectors.toList());
		
		
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
		apartments = apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.ACTIVE) && !apartment.isDeleted();
		}).collect(Collectors.toList());
		
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
		apartments = apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.INACTIVE) && !apartment.isDeleted();
		}).collect(Collectors.toList());
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/{apartmentID}")
	@DELETE
	public Response deleteApartmentById(@PathParam("apartmentID") Long id, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		
		if(role.equals("admin") || isHostOfAnApartment(username, id)) {
			ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
			if(apartmentDAO.deleteApartment(id)) {
				if(hostDAO.deleteApartmentWithId(id)) {
					context.setAttribute("apartmentDAO", apartmentDAO);
					return Response.status(203).entity("Apartment deleted successfully").build();
				}
				return Response.status(500).entity("Error occurred while deleting apartments").build();
			}
			return Response.status(404).entity("Apartment not found").build();
		}
		return Response.status(403).entity("You have no permission to delete apartments").build();
	}

	@Path("/modify")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyExistingApartment(ApartmentModifyDTO apartmentDTO, @Context HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute("username");
		String role = (String) request.getSession().getAttribute("role");
		Long apartmentID = apartmentDTO.getId();
		
		if(!role.equals("admin") && !isHostOfAnApartment(username, apartmentID))
			return Response.status(403).entity("You have no permission to modify apartment").build();
		
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Apartment apartment = apartmentDAO.findApartmentById(apartmentDTO.getId());
		if(apartment !=null && !apartment.isDeleted()) {
			apartment = modifyApartment(apartment, apartmentDTO);
			if(apartmentDAO.modifyApartment(apartment)) {
				return Response.status(200).entity("OK").build();
			}
				
			
			return Response.status(500).entity("Server error has occurred").build();
		}
		return Response.status(404).entity("Can't modify apartment with provided ID").build();
	}
	
	@Path("/advancedSearch")
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
	public Response sortApartments(ApartmentSortIDS apartmentSortIDs) {
		List<Apartment> sortedApartments = fetchApartmentForProvidedIds(apartmentSortIDs.getApartmentsIds());
		
		switch(apartmentSortIDs.getCriteria()) {
			case "priceASC": sortedApartments = sortApartmentsByPriceASC(sortedApartments);
				break;
			case "priceDESC" : 	sortedApartments = sortApartmentsByPriceDESC(sortedApartments);
				break;
			case "nameASC" : sortedApartments = sortApartmentsByNameASC(sortedApartments);
				break;
			case "nameDESC" : sortedApartments = sortApartmentsByNameDESC(sortedApartments);
				break;
			case "roomsASC" : sortedApartments = sortApartmentsByRoomsASC(sortedApartments);
				break;
			case "roomsDESC" : sortedApartments = sortApartmentsByRoomsDESC(sortedApartments);
				break;
			case "guestsASC" : sortedApartments = sortApartmentsByGuestsASC(sortedApartments);
				break;
			case "guestsDESC" : sortedApartments = sortApartmentsByGuestsDESC(sortedApartments);
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
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		
		switch(role) {
			case "admin" : apartments = getAllApartments();
				break;
			case "host" : apartments = getAllActiveApartmentsForProvidedHost(username);
				break;
			default : apartments = getAllActiveApartments(apartmentDAO);
		}
		
		if(apartmentDTO.getAmenities() != null)
			apartments = filterApartmentsByAmenities(apartments, apartmentDTO.getAmenities());
		
		if(apartmentDTO.getStatus() != null)
			apartments = filterApartmentsByStatus(apartments, apartmentDTO.getStatus());
		
		if(apartmentDTO.getType() != null)
			apartments = filterApartmentsByType(apartments, apartmentDTO.getType());
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/filter/active")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response filterActiveApartments(ApartmentFilterDTO apartmentDTO) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = new ArrayList<>();
		
		apartments = apartmentDAO.getApartments().values();
		apartments = apartments.stream().filter(apartment -> apartment.getStatus().equals(ApartmentStatus.ACTIVE) && !apartment.isDeleted()).collect(Collectors.toList());
		
		
		if(apartmentDTO.getAmenities() != null)
			apartments = filterApartmentsByAmenities(apartments, apartmentDTO.getAmenities());
		
		if(apartmentDTO.getStatus() != null)
			apartments = filterApartmentsByStatus(apartments, apartmentDTO.getStatus());
		
		if(apartmentDTO.getType() != null)
			apartments = filterApartmentsByType(apartments, apartmentDTO.getType());
		
		
		return Response.status(200).entity(apartments).build();
	}
	
	private Apartment makeApartmentOutOfDTO(NewApartmentDTO dto) {
		ApartmentType type;
		if(dto.getApartmentType().equals("Room")) {
			type = ApartmentType.ROOM;
		} else {
			type = ApartmentType.FULL_APARTMENT;
		}
		Address address = Address.builder()
				.street(dto.getStreet())
				.number(dto.getNumber())
				.city(dto.getCity())
				.zipCode(dto.getZipCode())
				
				.build();
		
		Location location = Location.builder()
				.address(address)
				.lattitude(dto.getLatitude())
				.longitude(dto.getLongitude())
				.build();
			
		Apartment apartment = Apartment.builder()
				.apartmentName(dto.getApartmentName())
				.apartmentType(type)
				.numberOfRooms(dto.getNumberOfRooms())
				.numberOfGuests(dto.getNumberOfGuests())
				.pricePerNight(dto.getPricePerNight())
				.location(location)
				.images(dto.getImages())
				.build();
		
		apartment = addAmenitiesToApartment(apartment, dto.getAmenities());
		List<String> dates = makeStringListOutOfString(dto.getAvailableDatesForRenting());
		apartment = addADatesForRentingToApartment(apartment, dates);
		
		return apartment;
	}
	
	private Apartment addAmenitiesToApartment(Apartment apartment, List<String> amenities) {
		AmenityDAO amenityDAO = (AmenityDAO) context.getAttribute("amenityDAO");
		
		List<Amenity> realAmenities = new ArrayList<>();
		
		for(String amenity : amenities) {
			Amenity amenityObject =  amenityDAO.findAmenityByName(amenity);
			if(amenityObject != null)
				realAmenities.add(amenityObject);
		}
		
		apartment.setAmenities(realAmenities);
		return apartment;
	}
	
	private List<String> makeStringListOutOfString(String dateString) {
		List<String> dates = new ArrayList<>();
		String[] array = dateString.split(",");
		
		for(int i = 0; i<array.length; i++) {
			dates.add(array[i].trim());
		}
		
		return dates;
	}
	
	private Apartment addADatesForRentingToApartment(Apartment apartment, List<String> dates) {
		List<LocalDate> dateList = new ArrayList<>();
		
		for(String dateString : dates) {
			String dateArray[] = dateString.split("/");
			int day = Integer.parseInt(dateArray[0]);
			int month = Integer.parseInt(dateArray[1]);
			int year = Integer.parseInt(dateArray[2]);
			
			LocalDate date =  LocalDate.of(year, month, day);
		
			dateList.add(date);
			
		}
		
		apartment.setAvailabeDatesForRenting(dateList);
		
		return apartment;
	}
	
	private boolean isHostOfAnApartment(String username, Long id) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		
		if(host == null || host.getApartmentsForRent() == null) return false;

		for(Apartment apartment : host.getApartmentsForRent()) {
			if(apartment.getId().equals(id))
				return true;
		}
		
		return false;
	}
	
	private Collection<Apartment> getAllActiveApartments(ApartmentDAO apartmentDAO) {
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		apartments = apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.ACTIVE) && !apartment.isDeleted();
		}).collect(Collectors.toList());
		
		return apartments;
	}
	
	private Collection<Apartment> getAllApartments() {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		apartments = apartments.stream().filter(apartment -> !apartment.isDeleted()).collect(Collectors.toList());
		
		return apartments;
	}
	
	private Apartment modifyApartment(Apartment apartment, ApartmentModifyDTO dto) {
		if(!dto.getApartmentName().equals(""))
			apartment.setApartmentName(dto.getApartmentName());
		
		if(dto.getApartmentType() != null && dto.getApartmentType() != apartment.getApartmentType()) 
			apartment.setApartmentType(dto.getApartmentType());
		
		if(dto.getNumberOfRooms() != apartment.getNumberOfRooms())
			apartment.setNumberOfRooms(dto.getNumberOfRooms());
		
		if(dto.getNumberOfGuests() != apartment.getNumberOfGuests())
			apartment.setNumberOfGuests(dto.getNumberOfGuests());
		

		if(!dto.getCity().equals("") && !dto.getStreet().equals("")) {
			apartment.getLocation().getAddress().setCity(dto.getCity());
			apartment.getLocation().getAddress().setStreet(dto.getStreet());
			apartment.getLocation().getAddress().setZipCode(dto.getZipCode());
			apartment.getLocation().getAddress().setNumber(dto.getNumber());
			
			apartment.getLocation().setLongitude(dto.getLongitude());
			apartment.getLocation().setLattitude(dto.getLatitude());
		}
		
		if(dto.getStatus() != null && dto.getStatus() != apartment.getStatus())
			apartment.setStatus(dto.getStatus());
		
		if(dto.getPricePerNight() != apartment.getPricePerNight())
			apartment.setPricePerNight(dto.getPricePerNight());
		
		if(dto.getAvailableDatesForRenting() != null) {
			List<String> dates = makeStringListOutOfString(dto.getAvailableDatesForRenting());
			apartment = addADatesForRentingToApartment(apartment, dates);
		}
			
		
		if(dto.getAmenities() != null)
			apartment.setAmenities(dto.getAmenities());
		
		if(dto.getImages() != null)
			apartment.setImages(dto.getImages());
		
		return apartment;
	}
	
	private Collection<Apartment> applySearchToCollection(Collection<Apartment> apartments, ApartmentSearchDTO dto) {
		if(dto.getNumberOfRoomsMin() >= 0 && dto.getNumberOfRoomsMax() > dto.getNumberOfRoomsMin())
			apartments = apartments.stream().filter(apartment -> {
				return apartment.getNumberOfRooms() >= dto.getNumberOfRoomsMin() &&
						apartment.getNumberOfRooms() <= dto.getNumberOfRoomsMax();
			}).collect(Collectors.toList());
		
		if(dto.getPriceMin() >= 0 && dto.getPriceMax() > dto.getPriceMin()) {
			apartments = apartments.stream().filter(apartment -> {
				return apartment.getPricePerNight() >= dto.getPriceMin() &&
						apartment.getPricePerNight() <= dto.getPriceMax();
			}).collect(Collectors.toList());
		}
		
		if(dto.getNumberOfGuests() > 0) 
			apartments = apartments.stream().filter(apartment -> {
				return apartment.getNumberOfGuests() == dto.getNumberOfGuests();
			}).collect(Collectors.toList());
		
		if(!dto.getCity().equals(""))
			apartments = apartments.stream().filter(apartment -> {
				return apartment.getLocation().getAddress().getCity().equals(dto.getCity());
			}).collect(Collectors.toList());
		
		if(!dto.getAvailableDatesForRenting().equals("")) {
			List<LocalDate> date = getCheckInAndCheckoutDate(dto.getAvailableDatesForRenting());
			LocalDate checkIn = date.get(0);
			LocalDate checkOut = date.get(1);
			apartments = apartments.stream().filter(apartment -> {
				return apartmentIsAvailableFromUntil(apartment, checkIn, checkOut);
			}).collect(Collectors.toList());
		}
		
		return apartments;
	}
	
	private List<Apartment> sortApartmentsByPriceASC(List<Apartment> apartments) {
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingDouble(Apartment::getPricePerNight)).collect(Collectors.toList());
		return sorted;
	}
	
	
	private List<Apartment> sortApartmentsByPriceDESC(List<Apartment> apartments) {
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingDouble(Apartment::getPricePerNight).reversed()).collect(Collectors.toList());
		return sorted;
	}

	private List<Apartment> sortApartmentsByNameASC(List<Apartment> apartments) {
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparing(Apartment::getApartmentName)).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByNameDESC(List<Apartment> apartments) {
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparing(Apartment::getApartmentName).reversed()).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByRoomsASC(List<Apartment> apartments) {
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingInt(Apartment::getNumberOfRooms)).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByRoomsDESC(List<Apartment> apartments) {
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingInt(Apartment::getNumberOfRooms).reversed()).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByGuestsASC(List<Apartment> apartments) {
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingInt(Apartment::getNumberOfGuests)).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByGuestsDESC(List<Apartment> apartments) {
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingInt(Apartment::getNumberOfGuests).reversed()).collect(Collectors.toList());
		return sorted;
	}
	
	private Collection<Apartment> getAllActiveApartmentsForProvidedHost(String username) {
		Collection<Apartment> apartments = new ArrayList<>();
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		
		if(host != null)
			apartments = host.getApartmentsForRent();
		
		apartments = apartments.stream().filter(apartment -> apartment.getStatus().equals(ApartmentStatus.ACTIVE) 
															&& !apartment.isDeleted()).collect(Collectors.toList());
		
		return apartments;
	}
	
	private Collection<Apartment> filterApartmentsByAmenities(Collection<Apartment> apartments, List<String> amenities) {
		apartments = apartments.stream().filter(apartment -> {
			List<Amenity> apertmentAmenities = apartment.getAmenities();
			return containsAllAmenities(apertmentAmenities, amenities);
		}).collect(Collectors.toList());
		return apartments;
	}
	
	public Collection<Apartment> filterApartmentsByStatus(Collection<Apartment> apartments, ApartmentStatus status) {
		apartments = apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(status);
		}).collect(Collectors.toList());
		
		return apartments;
	}
	
	public Collection<Apartment> filterApartmentsByType(Collection<Apartment> apartments, ApartmentType type) {
		apartments = apartments.stream().filter(apartment -> {
			return apartment.getApartmentType().equals(type);
		}).collect(Collectors.toList());
		
		return apartments;
	}
	
	private boolean containsAllAmenities(List<Amenity> apartmentAmenities, List<String> amenities) {
		for(String amenity : amenities) {
			boolean doesContainAmenity = false;
			for(Amenity apartmentAmenity : apartmentAmenities) {
				if(apartmentAmenity.getAmenity().equals(amenity)) {
					doesContainAmenity = true;
					break;
				}
					
			}
			// stop searching for other amenities if current amenity is not present in apartment
			if(!doesContainAmenity)
				return doesContainAmenity;
		}
		
		// All amenities are present in apartment
		return true;
	}
	
	private List<LocalDate> getCheckInAndCheckoutDate(String dateStrings) {
		List<LocalDate> dates = new ArrayList<>();
		String stringArray[] = dateStrings.split(",");
		for(String dateString : stringArray) {
			dateString = dateString.trim();
			String dateArray[] = dateString.split("/");
			int day = Integer.parseInt(dateArray[0]);
			int month = Integer.parseInt(dateArray[1]);
			int year = Integer.parseInt(dateArray[2]);
			
			LocalDate date = LocalDate.of(year, month, day);		
			dates.add(date);
		}
		return dates;
	}
	
	private boolean apartmentIsAvailableFromUntil(Apartment apartment, LocalDate checkIn, LocalDate checkOut) {
		long rentDuration = ChronoUnit.DAYS.between(checkIn, checkOut);
		
		LocalDate current = checkIn;
		for(long i=0; i<rentDuration; i++) {
			current = current.plusDays(1L);
			if(!apartment.getAvailabeDatesForRenting().contains(current))
				return false;

		}
		
		return true;
	}
	
	private List<Apartment> fetchApartmentForProvidedIds(List<Long> idsList) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		List<Apartment> apartments = new ArrayList<>();
		
		for(int i=0; i<idsList.size(); i++) {
			Apartment apartment = apartmentDAO.findApartmentById(idsList.get(i));
			apartments.add(apartment);
		}
		
		return apartments;
	}
	
}
