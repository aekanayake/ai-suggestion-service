package apex.wiley.com.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanDto {
    private String courseKey;
    private String userKey;
    private String threadId;
    private Object studyPlan;
    private Object courseOutline;
}
