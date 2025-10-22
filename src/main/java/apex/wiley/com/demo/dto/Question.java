package apex.wiley.com.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private String questionId;
    private String title;
    private Integer numAttempts;
    private Integer maxAttempts;
    private Double score;
    private String lastSubmitDate;
    private String orgReferenceId;
    private String orgReferenceTitle;
    private String derivedFrom;
    private Double weight;
    private String mqId;
    
    @JsonProperty("Question")
    private Question[] nestedQuestions;
}