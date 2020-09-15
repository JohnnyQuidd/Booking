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
import javax.ws.rs.PUT;
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
								 .apartmentName(apartment.getApartmentName())
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
	@Produces(MediaType.APPLICATION_JSON)
	public Response getComments(@Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		
		if(!role.equals("admin")) return Response.status(403).entity("You have no permission to see all comments").build();
		
		CommentDAO commentDAO = (CommentDAO) context.getAttribute("commentDAO");
		Collection<Comment> comments = commentDAO.getComments().values();
		return Response.status(200).entity(comments).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/approved/apartment/{apartmentId}")
	public Response getApprovedCommentsForApartment(@PathParam("apartmentId") Long apartmentId) {
		CommentDAO commentDAO = (CommentDAO) context.getAttribute("commentDAO");
		Collection<Comment> comments = commentDAO.getComments().values();
		
		comments = comments.stream().filter(comment -> {
			return comment.getApartmentId().equals(apartmentId) && comment.getStatus().equals(CommentStatus.APPROVED);
		}).collect(Collectors.toList());
		
		return Response.status(200).entity(comments).build();
	}
	
	@GET
	@Path("/created/host/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCreatedCommentsForHost(@PathParam("username") String username) {
		CommentDAO commentDAO = (CommentDAO) context.getAttribute("commentDAO");
		Collection<Comment> comments = commentDAO.getComments().values();
		
		comments = comments.stream().filter(comment -> {
			return comment.getHost().equals(username) && comment.getStatus().equals(CommentStatus.CREATED);
		}).collect(Collectors.toList());
		
		
		return Response.status(200).entity(comments).build();
	}
	
	@PUT
	@Path("/approve/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response approveComment(@PathParam("id") Long id, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		CommentDAO commentDAO = (CommentDAO) context.getAttribute("commentDAO");
		
		Comment comment = commentDAO.findCommentById(id);
		
		if(!role.equals("admin") && !username.equals(comment.getHost())) 
			return Response.status(403).entity("You have no permission to modify comment state").build();
		
		if(comment == null) return Response.status(404).entity("Comment not found").build();
		
		comment.setStatus(CommentStatus.APPROVED);
		if(commentDAO.modifyComment(comment)) {
			context.setAttribute("commentDAO", commentDAO);
			return Response.status(200).entity("Comment approved").build();
		}
		
		return Response.status(500).entity("An error occurred while persisting comments").build();
	}
	
	@PUT
	@Path("/decline/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response declineComment(@PathParam("id") Long id, @Context HttpServletRequest request) {
		String role = (String) request.getSession().getAttribute("role");
		String username = (String) request.getSession().getAttribute("username");
		CommentDAO commentDAO = (CommentDAO) context.getAttribute("commentDAO");
		
		Comment comment = commentDAO.findCommentById(id);
		
		if(!role.equals("admin") && !username.equals(comment.getHost())) 
			return Response.status(403).entity("You have no permission to modify comment state").build();
		
		if(comment == null) return Response.status(404).entity("Comment not found").build();
		
		comment.setStatus(CommentStatus.DECLINED);
		if(commentDAO.modifyComment(comment)) {
			context.setAttribute("commentDAO", commentDAO);
			return Response.status(200).entity("Comment declined").build();
		}
		
		return Response.status(500).entity("An error occurred while persisting comments").build();
	}
	
	
	
}
