package services;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/login")
public class LoginService {
	ServletContext context;
	
	public LoginService() {
		
	}
	
	public void init() {
		System.out.println("Init");
	}
	
	@GET
	public Response getAllUsers() {
		return Response.status(200).entity("All users").build();
	}
	
}
