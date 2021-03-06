package model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Comment {
	private Long id;
	private String username;
	private String host;
	private String text;
	private Long apartmentId;
	private String apartmentName;
	private CommentStatus status;
	private int rating;
	private LocalDateTime timestamp;
}
