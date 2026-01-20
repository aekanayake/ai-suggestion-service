package apex.wiley.com.demo.service;

import apex.wiley.com.demo.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final AzureOpenAiService azureOpenAiService;
    private final ObjectMapper objectMapper;
    private final JwtTokenService jwtTokenService;

    public StudentServiceImpl(AzureOpenAiService azureOpenAiService, ObjectMapper objectMapper, JwtTokenService jwtTokenService) {
        this.azureOpenAiService = azureOpenAiService;
        this.objectMapper = objectMapper;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public StudentData getStudentAnalytics() {
        // Extract claims from JWT token via JwtTokenService
        String contextId = jwtTokenService.getContextId();
        String ltiUserId = jwtTokenService.getLtiUserId();

        // Validate required claims
        if (contextId == null || contextId.isBlank()) {
            throw new IllegalArgumentException("lmscontextid claim is required and cannot be null or blank");
        }
        if (ltiUserId == null || ltiUserId.isBlank()) {
            throw new IllegalArgumentException("lmsuserid claim is required and cannot be null or blank");
        }

        // Try to load mock data from JSON file first
        String fileName = contextId + "_" + ltiUserId + ".json";
        try {
            StudentDataDto mockData = loadMockDataFromFile(fileName);
            if (mockData != null) {
                return convertMockDataToStudentData(mockData);
            }
        } catch (Exception e) {
            // Log the error and fall back to default data
            System.err.println("Failed to load mock data from file: " + fileName + ". Error: " + e.getMessage());
        }
        return null;
    }

    private StudentDataDto loadMockDataFromFile(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("mockData/" + fileName);

        if (!resource.exists()) {
            return null;
        }

        StudentDataDto mockData = objectMapper.readValue(resource.getInputStream(), StudentDataDto.class);
        return mockData;
    }

    private StudentData convertMockDataToStudentData(StudentDataDto mockData) {
        StudentAnalyticsAiResponse aiAnalytics = null;

        try {
            System.out.println("=== Starting AI Analytics Generation ===");

            // Build payload for AI analysis
            String payload = buildStudentAnalysisPayload(mockData);
            System.out.println("Payload built successfully. Length: " + payload.length());

            // Call Azure OpenAI service
            System.out.println("Calling Azure OpenAI service...");
            String aiResponse = azureOpenAiService.analyzeStudentPerformance(payload);
            System.out.println("Azure OpenAI service returned. Response is " + (aiResponse != null ? "not null" : "null"));

            // Parse AI response
            if (aiResponse != null && !aiResponse.isEmpty()) {
                System.out.println("Parsing AI response...");
                aiAnalytics = parseAiResponse(aiResponse);
            } else {
                System.err.println("AI response is null or empty!");
            }
        } catch (Exception e) {
            System.err.println("Error getting AI analytics: " + e.getMessage());
            e.printStackTrace();
        }

        // If AI analysis failed, use defaults
        if (aiAnalytics == null) {
            aiAnalytics = StudentAnalyticsAiResponse.builder()
                .studyTime(0.0)
                .topicNeedToAttention(Collections.emptyList())
                .recentQuestions(Collections.emptyList())
                .recommendations(Collections.emptyList())
                .build();
        }

        return StudentData.builder()
            .studentType(mockData.getStudentType())
            .courseProgress(calculateCourseProgress(mockData.getAssignments()))
            .averageGrade(calculateAverageGrade(mockData.getAssignments()))
            .studyTime(aiAnalytics.getStudyTime())
            .topicNeedToAttention(aiAnalytics.getTopicNeedToAttention())
            .assignments(convertAssignments(mockData.getAssignments()))
            .recentQuestions(aiAnalytics.getRecentQuestions())
            .recommendations(aiAnalytics.getRecommendations())
            .build();
    }

    private Integer calculateCourseProgress(List<AssignmentDto> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return 0;
        }

        // Calculate overall course progress based on assignment progress
        double totalProgress = assignments.stream()
            .mapToDouble(AssignmentDto::getProgress)
            .average()
            .orElse(0.0);

        return (int) Math.round(totalProgress);
    }

    private Integer calculateAverageGrade(List<AssignmentDto> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return 0;
        }

        // Calculate average grade from complete and in_progress assignments only (exclude not_started)
        List<AssignmentDto> gradedAssignments = assignments.stream()
            .filter(assignment -> !assignment.getStatus().equals("not_started") 
                && assignment.getGrade() != null 
                && assignment.getGrade() > 0)
            .collect(Collectors.toList());

        if (gradedAssignments.isEmpty()) {
            return 0;
        }

        double averageGrade = gradedAssignments.stream()
            .mapToDouble(AssignmentDto::getGrade)
            .average()
            .orElse(0.0);

        return (int) Math.round(averageGrade);
    }

    private List<Assignment> convertAssignments(List<AssignmentDto> mockAssignments) {
        if (mockAssignments == null) {
            return Collections.emptyList();
        }

        return mockAssignments.stream()
            .map(this::convertMockAssignment)
            .collect(Collectors.toList());
    }

    private Assignment convertMockAssignment(AssignmentDto mockAssignment) {
        return Assignment.builder()
            .id(mockAssignment.getId() != null ? mockAssignment.getId().hashCode() : 0)
            .title(mockAssignment.getTitle())
            .dueDate(mockAssignment.getDueDate())
            .status(mockAssignment.getStatus())
            .questionsCompleted(mockAssignment.getQuestionsCompleted())
            .correctQuestions(mockAssignment.getCorrectQuestions())
            .totalQuestions(mockAssignment.getTotalQuestions())
            .progress(mockAssignment.getGrade())
            .build();
    }

    private String buildStudentAnalysisPayload(StudentDataDto mockData) throws Exception {
        // Extract all questions from all assignments
        List<Question> allQuestions = new ArrayList<>();

        if (mockData.getAssignments() != null) {
            for (AssignmentDto assignment : mockData.getAssignments()) {
                if (assignment.getQuestions() != null) {
                    allQuestions.addAll(assignment.getQuestions());
                }
            }
        }

        // Build a structured payload for AI
        Map<String, Object> payload = new HashMap<>();
        payload.put("currentDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        payload.put("studentType", mockData.getStudentType());
        payload.put("totalAssignments", mockData.getAssignments() != null ? mockData.getAssignments().size() : 0);
        payload.put("assignments", mockData.getAssignments());
        payload.put("totalQuestions", allQuestions.size());

        // Calculate some statistics
        long completedQuestions = allQuestions.stream()
            .filter(q -> q.getNumAttempts() != null && q.getNumAttempts() > 0)
            .count();

        long incorrectQuestions = allQuestions.stream()
            .filter(q -> q.getScore() != null && q.getScore() == 0 && q.getNumAttempts() != null && q.getNumAttempts() > 0)
            .count();

        payload.put("completedQuestions", completedQuestions);
        payload.put("incorrectQuestions", incorrectQuestions);
        payload.put("questions", allQuestions);

        // Add instruction for the AI
        String instruction = "Analyze this student data and provide analytics. " +
            "IMPORTANT: Study time should be RECOMMENDED HOURS PER WEEK based on upcoming assignment due dates and student performance. " +
            "Consider: " +
            "1) Upcoming assignment due dates to determine urgency and weekly time needed, " +
            "2) Number of incomplete questions and assignments, " +
            "3) Student's current performance (incorrect answers, multiple attempts), " +
            "4) Recommend realistic weekly study hours (typically 5-15 hours per week). " +
            "\n\nProvide: " +
            "- studyTime: Recommended study hours PER WEEK (not total), " +
            "- topicNeedToAttention: Top 3-5 topics where student struggles. Include orgReferenceTitle, orgReferenceId, orgReferenceType, url, and overallProgress, " +
            "- recentQuestions: Only the 3 MOST RECENT question attempts with their status (correct/incorrect/not_attempted), sorted by most recent first, " +
            "- recommendations: Top 3-5 personalized study recommendations prioritized by urgency. Include orgReferenceTitle, orgReferenceId, orgReferenceType, url, priority, duration, and confidence.";

        payload.put("instruction", instruction);

        return objectMapper.writeValueAsString(payload);
    }

    private StudentAnalyticsAiResponse parseAiResponse(String aiResponse) {
        try {
            System.out.println("=== Parsing AI Response ===");
            System.out.println("Raw AI Response: " + aiResponse);

            StudentAnalyticsAiResponse response = objectMapper.readValue(aiResponse, StudentAnalyticsAiResponse.class);

            System.out.println("Parsed successfully!");
            System.out.println("Study Time: " + response.getStudyTime());
            System.out.println("Topics Need Attention: " + (response.getTopicNeedToAttention() != null ? response.getTopicNeedToAttention().size() : "null"));
            System.out.println("Recent Questions: " + (response.getRecentQuestions() != null ? response.getRecentQuestions().size() : "null"));
            System.out.println("Recommendations: " + (response.getRecommendations() != null ? response.getRecommendations().size() : "null"));

            return response;
        } catch (Exception e) {
            System.err.println("Failed to parse AI response: " + e.getMessage());
            e.printStackTrace();

            // Return default values if parsing fails
            return StudentAnalyticsAiResponse.builder()
                .studyTime(0.0)
                .topicNeedToAttention(Collections.emptyList())
                .recentQuestions(Collections.emptyList())
                .recommendations(Collections.emptyList())
                .build();
        }
    }

}

