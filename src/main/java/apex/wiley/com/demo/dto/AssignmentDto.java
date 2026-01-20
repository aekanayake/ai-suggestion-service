package apex.wiley.com.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDto {
    private String id;
    private String title;
    private String dueDate;
    private String status;
    private Integer questionsCompleted;
    private Integer correctQuestions;
    private Integer totalQuestions;
    private Integer progress;
    private Integer grade;

    @JsonProperty("Question")
    private List<Question> questions;
}
