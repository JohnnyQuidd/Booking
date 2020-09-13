package services;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import dto.NewCommentDTO;
import model.Apartment;
import model.Comment;
import model.CommentStatus;

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
		
		if(context.getAttribute("hostDAO") == null)
			context.setAttribute("hostDAO", new HostDAO(context.getRealPath("")));

	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response postANewComment(NewCommentDTO commentDTO, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		
		if(!role.equals("user") || !username.equals(commentDTO.getUsername()))
			return Response.status(403).entity("You have no permission to post a comment").build();
		
		CommentDAO commentDAO = (CommentDAO) context.getAttribute("commentDAO");
		Long id = 0L;
		
		while(commentDAO.getComments().containsKey(id))
			id = ThreadLocalRandom.current().nextLong(0, 65000);
		
		ApartmentDAO apartmentDAO = (ApartmentDAO) context.getAttribute("apartmentDAO");
		Apartment apartment = apartmentDAO.findApartmentById(commentDTO.getApartmentId());
		
		if(apartment == null) return Response.status(404).entity("Apartment not found").build();
		
		Comment comment = Comment.builder()
								 .username(commentDTO.getUsername())
								 .apartmentId(commentDTO.getApartmentId())
								 .text(commentDTO.getText())
								 .rating(commentDTO.getRating())
								 .id(id)
								 .host(apartment.getHostName())
								 .status(CommentStatus.CREATED)
								 .timestamp(LocalDateTime.now())
								 .build();
		if(commentDAO.addNewComment(comment)) {
			context.setAttribute("commentDAO", commentDAO);
			return Response.status(201).entity("Comment successfully posted").build();
		}
		
		return Response.status(500).entity("An error occurred while saving comment").build();
	}
	
	@GET
	@Path("/created/apartment/{apartmentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCommentsForApartment(@PathParam("apartmentID") Long apartmentID) {
		CommentDAO commentDAO = (CommentDAO) context.getAttribute("commentDAO");
		Collection<Comment> comments = commentDAO.getComments().values();
		
		comments = comments.stream().filter(comment -> comment.getApartmentId().equals(apartmentID)).collect(Collectors.toList());
		
		
		return Response.status(200).entity(comments).build();
	}
	
	@GET
	@Path("/host/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCommentsForHost(@PathParam("username") String username) {
		CommentDAO commentDAO = (CommentDAO) context.getAttribute("commentDAO");
		Collection<Comment> comments = commentDAO.getComments().values();
		
		comments = comments.stream().filter(comment -> comment.getHost().equals(username)).collect(Collectors.toList());
		
		
		return Response.status(200).entity(comments).build();
	}
	
	
	
}
