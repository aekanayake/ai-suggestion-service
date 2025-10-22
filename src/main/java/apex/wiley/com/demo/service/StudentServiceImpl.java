package apex.wiley.com.demo.service;

import apex.wiley.com.demo.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final AzureOpenAiService azureOpenAiService;
    private final ObjectMapper objectMapper;

    public StudentServiceImpl(AzureOpenAiService azureOpenAiService, ObjectMapper objectMapper) {
        this.azureOpenAiService = azureOpenAiService;
        this.objectMapper = objectMapper;
    }

    @Override
    public StudentData getStudentAnalytics(String contextId, String ltiUserId) {
        // Validate required parameters
        if (contextId == null || contextId.isBlank()) {
            throw new IllegalArgumentException("contextId is required and cannot be null or blank");
        }
        if (ltiUserId == null || ltiUserId.isBlank()) {
            throw new IllegalArgumentException("ltiUserId is required and cannot be null or blank");
        }

        // Try to load mock data from JSON file first
        String fileName = contextId + "_" + ltiUserId + ".json";
        try {
            MockStudentData mockData = loadMockDataFromFile(fileName);
            if (mockData != null) {
                return convertMockDataToStudentData(mockData);
            }
        } catch (Exception e) {
            // Log the error and fall back to default data
            System.err.println("Failed to load mock data from file: " + fileName + ". Error: " + e.getMessage());
        }
        return null;
    }

    private MockStudentData loadMockDataFromFile(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("mockData/" + fileName);

        if (!resource.exists()) {
            return null;
        }

        MockStudentData mockData = objectMapper.readValue(resource.getInputStream(), MockStudentData.class);
        return mockData;
    }

    private StudentData convertMockDataToStudentData(MockStudentData mockData) {
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

    private Integer calculateCourseProgress(List<MockAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return 0;
        }

        // Calculate overall course progress based on assignment progress
        double totalProgress = assignments.stream()
            .mapToDouble(MockAssignment::getProgress)
            .average()
            .orElse(0.0);

        return (int) Math.round(totalProgress);
    }

    private Integer calculateAverageGrade(List<MockAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return 0;
        }

        // Calculate average grade from completed assignments only
        List<MockAssignment> completedAssignments = assignments.stream()
            .filter(assignment -> "complete".equals(assignment.getStatus()) && assignment.getGrade() != null)
            .collect(Collectors.toList());

        if (completedAssignments.isEmpty()) {
            return 0;
        }

        double averageGrade = completedAssignments.stream()
            .mapToDouble(MockAssignment::getGrade)
            .average()
            .orElse(0.0);

        return (int) Math.round(averageGrade);
    }

    private List<Assignment> convertAssignments(List<MockAssignment> mockAssignments) {
        if (mockAssignments == null) {
            return Collections.emptyList();
        }

        return mockAssignments.stream()
            .map(this::convertMockAssignment)
            .collect(Collectors.toList());
    }

    private Assignment convertMockAssignment(MockAssignment mockAssignment) {
        return Assignment.builder()
            .id(mockAssignment.getId() != null ? mockAssignment.getId().hashCode() : 0)
            .title(mockAssignment.getTitle())
            .dueDate(mockAssignment.getDueDate())
            .status(mockAssignment.getStatus())
            .questionsCompleted(mockAssignment.getQuestionsCompleted())
            .totalQuestions(mockAssignment.getTotalQuestions())
            .progress(mockAssignment.getProgress())
            .build();
    }

    private String buildStudentAnalysisPayload(MockStudentData mockData) throws Exception {
        // Extract all questions from all assignments
        List<Question> allQuestions = new ArrayList<>();
        
        if (mockData.getAssignments() != null) {
            for (MockAssignment assignment : mockData.getAssignments()) {
                if (assignment.getQuestions() != null) {
                    allQuestions.addAll(assignment.getQuestions());
                }
            }
        }
        
        // Build a structured payload for AI
        Map<String, Object> payload = new HashMap<>();
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
        String instruction = "Analyze this student data and provide analytics including: " +
            "1) Estimated study time based on questions completed and attempts, " +
            "2) Topics that need attention based on incorrect answers or multiple attempts, " +
            "3) Recent questions with their status, " +
            "4) Personalized recommendations for improvement.";
        
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

