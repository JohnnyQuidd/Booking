package services;

import java.util.ArrayList;
import java.util.Collection;
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

import dao.AdminDAO;
import dao.HostDAO;
import dao.UserDAO;
import dto.LoginDTO;
import dto.RegisterDTO;
import dto.UserPreviewDTO;
import model.Admin;
import model.Host;
import model.User;
import util.UsernameUniqueness;

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
		
		if(servletContext.getAttribute("adminDAO") == null) {
			servletContext.setAttribute("adminDAO", new AdminDAO(servletContext.getRealPath("")));
		}
		
		if(servletContext.getAttribute("hostDAO") == null) {
			servletContext.setAttribute("hostDAO", new HostDAO(servletContext.getRealPath("")));
		}
	}
	
	@Path("/all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers() {
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		Collection<User> users = (Collection<User>) userDAO.getUsers().values();
		
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
		
		List<UserPreviewDTO> dtos = new ArrayList<>();
		dtos.add(dto);
		
		return Response.status(200).entity(dtos).build();
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
			
			servletContext.setAttribute("userDAO", userDAO);
			return Response.status(200).entity("Data changed successfully").build();
		}
		return Response.status(400).entity("Bad request").build();
	}
	
	@Path("/login")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response loginUser(LoginDTO loginDTO, @Context HttpServletRequest request) {
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		User user = findUserForUsernameAndPassword(loginDTO, userDAO);
		if(user != null) {
			if(!user.isActive()) {
				return Response.status(403).entity("User is banned from logging").build();
			}
			
			request.getSession().setAttribute("username", user.getUsername());
			request.getSession().setAttribute("role", "user");
			return Response.status(200).entity("user").build();
		}
		
		AdminDAO adminDAO = (AdminDAO) servletContext.getAttribute("adminDAO");
		Admin admin = findAdminForUsernameAndPassword(loginDTO, adminDAO);

		if(admin != null) {
			request.getSession().setAttribute("username", admin.getUsername());
			request.getSession().setAttribute("role", "admin");
			return Response.status(200).entity("admin").build();
		}
		
		HostDAO hostDAO = (HostDAO) servletContext.getAttribute("hostDAO");
		Host host = findHostForUsernameAndPassword(loginDTO, hostDAO);
		
		if(host != null) {
			request.getSession().setAttribute("username", host.getUsername());
			request.getSession().setAttribute("role", "host");
			return Response.status(200).entity("host").build();
		}
		
		return Response.status(403).entity("Invalid credentials").build();
	}

	@Path("/register")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response registerUser(RegisterDTO userDTO) {
		
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		
		if(!UsernameUniqueness.isUsernameUnique(userDTO.getUsername(), servletContext)) {
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
		
		servletContext.setAttribute("userDAO", userDAO);
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
	
	@POST
	@Path("/block/{username}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response blockUser(@PathParam("username") String username, @Context HttpServletRequest request) {
		String role =  (String) request.getSession().getAttribute("role");
		if(!role.equals("admin")) return Response.status(403).entity("You have no permission to block users").build();
		
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		User user = userDAO.findUserByUsername(username);
		if(user == null) return Response.status(404).entity("User " + username + " not found").build();
		
		user.setActive(false);
		userDAO.saveUsers();
		servletContext.setAttribute("userDAO", userDAO);
		return Response.status(200).entity(username + " successfully blocked").build();
	}
	
	@POST
	@Path("/unblock/{username}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response unblockUser(@PathParam("username") String username, @Context HttpServletRequest request) {
		String role =  (String) request.getSession().getAttribute("role");
		if(!role.equals("admin")) return Response.status(403).entity("You have no permission to ublock users").build();
		
		UserDAO userDAO = (UserDAO) servletContext.getAttribute("userDAO");
		User user = userDAO.findUserByUsername(username);
		if(user == null) return Response.status(404).entity("User " + username + " not found").build();
		
		user.setActive(true);
		userDAO.saveUsers();
		servletContext.setAttribute("userDAO", userDAO);
		return Response.status(200).entity(username + " successfully unblocked").build();
	}
	
	private List<UserPreviewDTO> makeUserPreviewFromModel(Collection<User> users) {
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
	
	private Admin findAdminForUsernameAndPassword(LoginDTO dto, AdminDAO adminDAO) {
		for(Admin admin : adminDAO.getAdmins().values()) {
			if(admin.getUsername().equals(dto.getUsername()) && admin.getPassword().equals(dto.getPassword())) {
				return admin;
			}
		}
		
		return null;
	}
	
	private Host findHostForUsernameAndPassword(LoginDTO dto, HostDAO hostDAO) {
		for(Host host : hostDAO.getHosts().values()) {
			if(host.getUsername().equals(dto.getUsername()) && host.getPassword().equals(dto.getPassword())) {
				return host;
			}
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
