package apex.wiley.com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    private Integer id;
    private String title;
    private String dueDate;
    private String status;
    private Integer questionsCompleted;
    private Integer totalQuestions;
    private Integer progress;
}

