package services;

import java.util.ArrayList;
import java.util.List;

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

import dao.UserDAO;
import dto.LoginDTO;
import dto.RegisterDTO;
import dto.UserPreviewDTO;
import model.User;

@Path("/user")
public class UserService {
	@Context
	ServletContext servletContext;
	
	public UserService() {
		
	}
	
	@PostConstruct
	public void init() {
		if(servletContext.getAttribute("userDAO") == null) {
			servletContext.setAttribute("userDAO", new UserDAO(servletContext.getRealPath("")));
		}
	}
	
	@Path("/all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers() {
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		List<User> users = (List<User>) userDAO.getUsers().values();
		
		List<UserPreviewDTO> usersPreview = makeUserPreviewFromModel(users);
		return Response.status(200).entity(usersPreview).build();
	}
	
	@Path("/{username}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserForUsername(@PathParam("username") String username) {
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		
		User user = findUserForGivenUsername(username, userDAO);
		if(user == null) 
			return Response.status(404).entity("User not found").build();
		
		UserPreviewDTO dto = UserPreviewDTO.builder()
				.username(user.getUsername())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.gender(user.getGender())
				.active(user.isActive())
				.build();
		
		return Response.status(200).entity(dto).build();
	}
	
	@Path("/{username}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response modifyUserData(@PathParam("username") String username, RegisterDTO userDTO) {
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		
		if(validFieldsForModifying(userDTO)) {
			User user = findUserForGivenUsername(userDTO.getUsername(), userDAO);
			if(user == null) 
				return Response.status(404).entity("User not found").build();
			
			if(!user.getUsername().equals(username)) {
				return Response.status(403).entity("You have no permission to change data").build();
			}
			modifyUser(user, userDTO);
			userDAO.addNewUser(user);
			return Response.status(200).entity("OK").build();
		}
		return Response.status(400).entity("Bad request").build();
	}
	
	@Path("/login")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUser(LoginDTO loginDTO, @Context HttpServletRequest request) {
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		User user = findUserForUsernameAndPassword(loginDTO, userDAO);
		if(user == null) {
			return Response.status(403).entity("Invalid credentials").build();
		}
		
		if(!user.isActive()) {
			return Response.status(403).entity("User is banned from logging").build();
		}
		
		request.getSession().setAttribute("username", user.getUsername());
		return Response.status(200).entity("OK").build();
	}

	@Path("/register")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response registerUser(RegisterDTO userDTO) {
		
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		
		if(usernameIsAlreadyTaken(userDTO.getUsername(), userDAO)) {
			System.out.println("Username is already taken");
			return Response.status(403).entity("Username is already taken").build();
		}
		
		if(!validFields(userDTO)) {
			System.out.println("Invalid fields");
			return Response.status(400).entity("Invalid fields").build();
		}
		
		if(saveUserToAFile(userDTO, userDAO)) {
			System.out.println("User successfully created");
			return Response.status(201).entity("User created").build();
		}
		
		return Response.status(500).entity("Server error occured while saving user").build();
	}
	
	@POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response logout(@Context HttpServletRequest request) {
        request.getSession().invalidate();
        return Response.status(200).entity("OK").build();
    }
	
	private List<UserPreviewDTO> makeUserPreviewFromModel(List<User> users) {
		List<UserPreviewDTO> dtos = new ArrayList<>();
		
		for(User user : users) {
			UserPreviewDTO dto = UserPreviewDTO.builder()
					.username(user.getUsername())
					.firstName(user.getFirstName())
					.lastName(user.getLastName())
					.gender(user.getGender())
					.active(user.isActive())
					.build();
			
			dtos.add(dto);
		}
		
		return dtos;
	}
	
	private User findUserForGivenUsername(String username, UserDAO userDAO) {
		for(User user : userDAO.getUsers().values()) {
			if(user.getUsername().equals(username))
				return user;
		}
		return null;
	}
	
	public void modifyUser(User user, RegisterDTO dto) {
		if(!dto.getFirstName().equals("") && !dto.getFirstName().equals(user.getFirstName())) {
			user.setFirstName(dto.getFirstName());
		}
		if(!dto.getLastName().equals("") && !dto.getLastName().equals(user.getLastName())) {
			user.setLastName(dto.getLastName());
		}
		if(!dto.getPassword().equals("") && !dto.getPassword().equals(user.getPassword())) {
			user.setPassword(dto.getPassword());
		}
	}
	
	private User findUserForUsernameAndPassword(LoginDTO loginDTO, UserDAO userDAO) {
		for(User user : userDAO.getUsers().values()) {
			if(user.getUsername().equals(loginDTO.getUsername()) && user.getPassword().equals(loginDTO.getPassword()))
				return user;
		}
		return null;
	}
	
	private boolean usernameIsAlreadyTaken(String username, UserDAO userDAO) {
		for(User user : userDAO.getUsers().values()) {
			if(user.getUsername().equals(username))
				return true;
		}
		return false;
	}
	
	private boolean validFieldsForModifying(RegisterDTO dto) {
		return !dto.getUsername().trim().equals("") && !dto.getFirstName().trim().equals("")
				&& !dto.getLastName().trim().equals("")
				&& !dto.getPassword().trim().equals("");
	}
	
	private boolean validFields(RegisterDTO dto) {
		return !dto.getUsername().trim().equals("") && !dto.getFirstName().trim().equals("")
				&& !dto.getLastName().trim().equals("")
				&& (dto.getGender().equals("Male") || dto.getGender().equals("Female"))
				&& !dto.getPassword().trim().equals("");
	}
	
	private boolean saveUserToAFile(RegisterDTO dto, UserDAO userDAO) {
		User user = User.builder()
				.username(dto.getUsername())
				.firstName(dto.getFirstName())
				.lastName(dto.getLastName())
				.gender(dto.getGender())
				.password(dto.getPassword())
				.active(true)
				.build();
		
		return userDAO.addNewUser(user);
	}
	
}
