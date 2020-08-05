package services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dto.LoginDTO;
import dto.RegisterDTO;

@Path("/user")
public class UserService {
	
	public UserService() {
		
	}
	
	public void init() {
		System.out.println("Init");
	}
	
	@GET
	public Response getAllUsers() {
		return Response.status(200).entity("All users").build();
	}
	
	@Path("/login")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUser(LoginDTO loginDTO) {
		System.out.println(loginDTO.getUsername() + " " + loginDTO.getPassword());
		return Response.status(200).entity(loginDTO).build();
	}
	
	@Path("/register")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerUser(RegisterDTO userDTO) {
		System.out.println(userDTO);
		return Response.status(201).entity(userDTO).build();
	}
	
}
