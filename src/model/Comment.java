package model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class Comment {
	private Long id;
	private String username;
	private String text;
	private Long apartmentId;
	private boolean active;
	private int rating;
	private LocalDateTime timestamp;
}
