package apex.wiley.com.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicAttention {
    @JsonProperty("title")
    @JsonAlias({"name", "title"})
    private String title;
    
    private String orgReferenceId;
    private String orgReferenceType;
    private String url;
    private Integer overallProgress;
}

