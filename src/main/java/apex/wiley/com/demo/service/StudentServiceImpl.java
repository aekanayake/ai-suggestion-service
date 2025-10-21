package apex.wiley.com.demo.service;

import apex.wiley.com.demo.dto.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Override
    public StudentData getStudentAnalytics(String contextId, String ltiUserId) {
        // Return different dummy data based on ltiUserId
        // If ltiUserId contains "new" or is empty, return new student data
        // Otherwise, return active student data
        
        if (ltiUserId == null || ltiUserId.isBlank() || ltiUserId.toLowerCase().contains("new")) {
            return getNewStudentData();
        } else {
            return getActiveStudentData();
        }
    }

    private StudentData getNewStudentData() {
        List<TopicAttention> topics = Arrays.asList(
            TopicAttention.builder()
                .name("Financial Statements")
                .overallProgress(25)
                .build(),
            TopicAttention.builder()
                .name("Accounting Principles")
                .overallProgress(10)
                .build()
        );

        List<Recommendation> recommendations = Arrays.asList(
            Recommendation.builder()
                .title("Start with Chapter 1")
                .priority("high")
                .duration("30 min")
                .confidence(95)
                .build(),
            Recommendation.builder()
                .title("Complete Pre-Course Assessment")
                .priority("medium")
                .duration("15 min")
                .confidence(88)
                .build()
        );

        return StudentData.builder()
            .studentType("new")
            .courseProgress(15)
            .averageGrade(0)
            .studyTime(0.0)
            .topicNeedToAttention(topics)
            .assignments(Collections.emptyList())
            .recentQuestions(Collections.emptyList())
            .recommendations(recommendations)
            .build();
    }

    private StudentData getActiveStudentData() {
        List<TopicAttention> topics = Arrays.asList(
            TopicAttention.builder()
                .name("Balance Sheet Analysis (Chapter 1)")
                .overallProgress(45)
                .build(),
            TopicAttention.builder()
                .name("Cash Flow Statement (Chapter 2)")
                .overallProgress(52)
                .build(),
            TopicAttention.builder()
                .name("Financial Ratios (Chapter 2)")
                .overallProgress(38)
                .build()
        );

        List<Assignment> assignments = Arrays.asList(
            Assignment.builder()
                .id(1)
                .title("Chapter 1 Practice Problems")
                .dueDate("Due in 2 days")
                .status("in_progress")
                .questionsCompleted(8)
                .totalQuestions(10)
                .progress(80)
                .build(),
            Assignment.builder()
                .id(2)
                .title("Chapter 2 Assessment")
                .dueDate("Due in 4 days")
                .status("not_started")
                .questionsCompleted(0)
                .totalQuestions(20)
                .progress(0)
                .build()
        );

        List<RecentQuestion> recentQuestions = Arrays.asList(
            RecentQuestion.builder()
                .id(1)
                .question("What is the difference between assets and liabilities?")
                .status("correct")
                .build(),
            RecentQuestion.builder()
                .id(2)
                .question("How to calculate depreciation?")
                .status("incorrect")
                .build()
        );

        List<Recommendation> recommendations = Arrays.asList(
            Recommendation.builder()
                .title("Balance Sheet Video Tutorial")
                .priority("high")
                .duration("12 min")
                .confidence(95)
                .build(),
            Recommendation.builder()
                .title("Financial Ratios Practice Quiz")
                .priority("high")
                .duration("20 min")
                .confidence(88)
                .build(),
            Recommendation.builder()
                .title("Cash Flow Interactive Example")
                .priority("medium")
                .duration("15 min")
                .confidence(82)
                .build()
        );

        return StudentData.builder()
            .studentType("active")
            .courseProgress(68)
            .averageGrade(82)
            .studyTime(2.5)
            .topicNeedToAttention(topics)
            .assignments(assignments)
            .recentQuestions(recentQuestions)
            .recommendations(recommendations)
            .build();
    }
}

