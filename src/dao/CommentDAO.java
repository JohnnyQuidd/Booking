package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;
import lombok.Setter;
import model.Comment;

@Getter
@Setter
public class CommentDAO {
	private String path;
	private Map<Long, Comment> comments = new HashMap<>();
	
	public CommentDAO(String path) {
		this.path = path;
		loadComments();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void loadComments() {
		String loadPath = this.path + "comment.json";
        BufferedReader in = null;
        File file = null;
        try {
            file = new File(loadPath);
            in = new BufferedReader(new FileReader(file));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibilityChecker(
                    VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            TypeFactory factory = TypeFactory.defaultInstance();
            MapType type = factory.constructMapType(HashMap.class, Long.class, Comment.class);

            objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            this.comments = (Map<Long, Comment>) objectMapper.readValue(file, type);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	@SuppressWarnings("deprecation")
	public void saveComments() {
		FileWriter fileWriter = null;
		File file = null;
		try {
			file = new File(this.path + "comment.json");
			file.createNewFile();
			fileWriter = new FileWriter(file);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			String string = objectMapper.writeValueAsString(this.comments);
			fileWriter.write(string);
		} catch (IOException eeee) {
			eeee.printStackTrace();
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
		}
	}
	
	public boolean addNewComment(Comment comment) {
		try {
			this.comments.put(comment.getId(), comment);
			saveComments();
			return true;
		} catch(Exception e) {
			System.out.println("An error occured while saving comments");
			return false;
		}
	}
	
	public Comment findCommentById(Long id) {
		for(Comment comment : this.comments.values()) {
			if(comment.getId().equals(id))
				return comment;
		}
		
		return null;
	}
	
	public boolean modifyComment(Comment comment) {
		try {
			comments.remove(comment.getId());
			comments.put(comment.getId(), comment);
			saveComments();
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
