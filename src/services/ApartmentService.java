package services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
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
import dto.ApartmentSortingDTO;
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
		
		host.getApartmentsForRent().add(apartment);
		hostDAO.saveHosts();
		
		
		if(apartmentDAO.addNewApartment(apartment)) {
			context.setAttribute("apartmentDAO", apartmentDAO);
			return Response.status(201).entity("Apartment successfully added").build();
		}
		
		return Response.status(500).entity("An error occurred while saving apartment").build();
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
	
	@Path("/{apartmentID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActiveApartmentForProidedName(@PathParam("apartmentID") Long apartmentID) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		//Collection<Apartment> apartments = getAllActiveApartments(apartmentDAO);
		Collection<Apartment> apartments = getAllApartments();
		
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
			return apartment.getStatus().equals(ApartmentStatus.ACTIVE);
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
			return apartment.getStatus().equals(ApartmentStatus.INACTIVE);
		}).collect(Collectors.toList());
		
		return Response.status(200).entity(apartments).build();
	}
	
	@Path("/{aparmentID}")
	@DELETE
	public Response deleteApartmentById(@PathParam("apartmentID") Long id, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		
		if(role.equals("admin") || isHostOfAnApartment(username, id)) {
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
	
	@Path("/advancedSearch")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchApartments(ApartmentSearchDTO searchDTO) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		//Collection<Apartment> apartments = getAllActiveApartments(apartmentDAO);
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
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
				break;
			case "nameASC" : sortedApartments = sortApartmentsByNameASC(apartmentsDTO);
				break;
			case "nameDESC" : sortedApartments = sortApartmentsByNameDESC(apartmentsDTO);
				break;
			case "roomsASC" : sortedApartments = sortApartmentsByRoomsASC(apartmentsDTO);
				break;
			case "roomsDESC" : sortedApartments = sortApartmentsByRoomsDESC(apartmentsDTO);
				break;
			case "guestsASC" : sortedApartments = sortApartmentsByGuestsASC(apartmentsDTO);
				break;
			case "guestsDESC" : sortedApartments = sortApartmentsByGuestsDESC(apartmentsDTO);
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
			case "host" : apartments = getAllActiveApartmentsForProvidedHost(username);	
		}
		
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
	
	@SuppressWarnings("deprecation")
	private Apartment addADatesForRentingToApartment(Apartment apartment, List<String> dates) {
		List<Date> dateList = new ArrayList<>();
		
		for(String dateString : dates) {
			String dateArray[] = dateString.split("/");
			int day = Integer.parseInt(dateArray[0]);
			int month = Integer.parseInt(dateArray[1]) + 1;
			int year = Integer.parseInt(dateArray[2]);
			
			Date date = new Date();
			date.setDate(day);
			date.setMonth(month);
			date.setYear(year - 1900);
			
			dateList.add(date);
		}
		
		apartment.setAvailabeDatesForRenting(dateList);
		
		return apartment;
	}
	
	private boolean isHostOfAnApartment(String username, Long id) {
		HostDAO hostDAO = (HostDAO) context.getAttribute("hostDAO");
		Host host = hostDAO.findHostByUsername(username);
		
		if(host == null) return false;
		
		for(Apartment apartment : host.getApartmentsForRent()) {
			if(apartment.getId() == id)
				return true;
		}
		
		return false;
	}
	
	private Collection<Apartment> getAllActiveApartments(ApartmentDAO apartmentDAO) {
		Collection<Apartment> apartments = apartmentDAO.getApartments().values();
		
		apartments = apartments.stream().filter(apartment -> {
			return apartment.getStatus().equals(ApartmentStatus.ACTIVE);
		}).collect(Collectors.toList());
		
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
			List<Date> date = getCheckInAndCheckoutDate(dto.getAvailableDatesForRenting());
			Date checkIn = date.get(0);
			Date checkOut = date.get(1);
			apartments = apartments.stream().filter(apartment -> {
				return apartmentIsAvailableFromUntil(apartment, checkIn, checkOut);
			}).collect(Collectors.toList());
		}
		
		return apartments;
	}
	
	private List<Apartment> sortApartmentsByPriceASC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingDouble(Apartment::getPricePerNight)).collect(Collectors.toList());
		return sorted;
	}
	
	
	private List<Apartment> sortApartmentsByPriceDESC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingDouble(Apartment::getPricePerNight).reversed()).collect(Collectors.toList());
		return sorted;
	}

	private List<Apartment> sortApartmentsByNameASC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparing(Apartment::getApartmentName)).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByNameDESC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparing(Apartment::getApartmentName).reversed()).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByRoomsASC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingInt(Apartment::getNumberOfRooms)).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByRoomsDESC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingInt(Apartment::getNumberOfRooms).reversed()).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByGuestsASC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
		List<Apartment> sorted = new ArrayList<>();
		sorted = apartments.stream().sorted(Comparator.comparingInt(Apartment::getNumberOfGuests)).collect(Collectors.toList());
		return sorted;
	}
	
	private List<Apartment> sortApartmentsByGuestsDESC(ApartmentSortingDTO apartmentsDTO) {
		List<Apartment> apartments = apartmentsDTO.getApartments();
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
		
		apartments = apartments.stream().filter(apartment -> apartment.getStatus().equals(ApartmentStatus.ACTIVE))
				.collect(Collectors.toList());
		
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
	
	@SuppressWarnings("deprecation")
	private List<Date> getCheckInAndCheckoutDate(String dateStrings) {
		List<Date> dates = new ArrayList<>();
		String stringArray[] = dateStrings.split(",");
		for(String dateString : stringArray) {
			dateString = dateString.trim();
			String dateArray[] = dateString.split("/");
			int day = Integer.parseInt(dateArray[0]);
			int month = Integer.parseInt(dateArray[1]) + 1;
			int year = Integer.parseInt(dateArray[2]);
			
			Date date = new Date();
			date.setDate(day);
			date.setMonth(month);
			date.setYear(year - 1900);
			
			dates.add(date);
		}
		return dates;
	}
	
	private boolean apartmentIsAvailableFromUntil(Apartment apartment, Date cIn, Date cOut) {
		boolean available = true;
		LocalDate checkIn = cIn.toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDate();
		LocalDate checkOut = cOut.toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDate();
		
		long rentDuration = ChronoUnit.DAYS.between(checkIn, checkOut);
		
		Date current =  java.sql.Date.valueOf(checkIn);
		for(long i=0; i<rentDuration; i++) {
			Calendar c = Calendar.getInstance(); 
			c.setTime(current); 
			c.add(Calendar.DATE, 1);
			current = c.getTime();
			
			if(!apartment.getAvailabeDatesForRenting().contains(current)) {
				available = false;
			}

		
		}
		
		
		return available;
	}
	
	
	
}
