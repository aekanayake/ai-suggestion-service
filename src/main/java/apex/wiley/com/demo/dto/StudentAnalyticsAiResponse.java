package apex.wiley.com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnalyticsAiResponse {
    private Double studyTime;
    private List<TopicAttention> topicNeedToAttention;
    private List<RecentQuestion> recentQuestions;
    private List<Recommendation> recommendations;
}

