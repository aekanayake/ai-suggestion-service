package apex.wiley.com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private String title;
    private String priority;
    private String duration;
    private Integer confidence;
    private String orgReferenceId;
    private String orgReferenceType;
    private String url;
}

