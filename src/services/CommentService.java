package services;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dao.ApartmentDAO;
import dao.CommentDAO;
import model.Apartment;
import model.Comment;

@Path("/comment")
public class CommentService {
	@Context
	ServletContext context;
	
	public CommentService() {}
	
	@PostConstruct
	public void init() {
		if(context.getAttribute("commentDAO") == null)
			context.setAttribute("commentDAO", new CommentDAO(context.getRealPath("")));
		
		if(context.getAttribute("apartmentDAO") == null)
			context.setAttribute("apartmentDAO", new ApartmentDAO(context.getRealPath("")));
	}
	
	@GET
	@Path("/{apartmentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCommentsForApartment(@PathParam("apartmentID") Long apartmentID) {
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Apartment apartment = apartmentDAO.findApartmentById(apartmentID);
		
		if(apartment == null)
			return Response.status(404).entity("Apartment not found").build();
		
		List<Comment> comments = apartment.getComments();
		return Response.status(200).entity(comments).build();
	}
	
	
	
}
