package apex.wiley.com.demo.service;

import apex.wiley.com.demo.entity.StudyPlan;
import apex.wiley.com.demo.repository.StudyPlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new study plan from request payload and saves to database
     */
    public StudyPlan createStudyPlan(Map<String, Object> requestPayload) {
        try {
            // Extract required fields from the payload
            String courseKey = (String) requestPayload.get("courseKey");
            String userKey = (String) requestPayload.get("userKey");
            Object courseOutline = requestPayload.get("courseOutline");
            
            if (courseKey == null || userKey == null || courseOutline == null) {
                throw new RuntimeException("Missing required fields: courseKey, userKey, or courseOutline");
            }
            
            // Extract optional date fields
            LocalDate courseStartDate = parseDate((String) requestPayload.get("courseStartDate"));
            LocalDate courseEndDate = parseDate((String) requestPayload.get("courseEndDate"));
            
            // Extract optional study plan field
            Object studyPlanData = requestPayload.get("studyPlan");
            String studyPlanJson = null;
            if (studyPlanData != null) {
                studyPlanJson = objectMapper.writeValueAsString(requestPayload);
            }
            
            // Convert the course outline to JSON string
            String courseOutlineJson = objectMapper.writeValueAsString(courseOutline);
            
            StudyPlan studyPlan = new StudyPlan();
            studyPlan.setCourseKey(courseKey);
            studyPlan.setUserKey(userKey);
            studyPlan.setThreadId(UUID.randomUUID().toString()); // Generate new threadId
            studyPlan.setCourseStartDate(courseStartDate);
            studyPlan.setCourseEndDate(courseEndDate);
            studyPlan.setStudyPlan(studyPlanJson);
            studyPlan.setCourseOutline(courseOutlineJson);
            
            return studyPlanRepository.save(studyPlan);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process course outline JSON", e);
        }
    }

    /**
     * Parses a date string into LocalDate, returns null if invalid or null
     */
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try parsing in ISO format first (yyyy-MM-dd)
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            try {
                // Try parsing in other common formats
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e2) {
                // If both fail, return null
                return null;
            }
        }
    }

    /**
     * Retrieves study plan by ID
     */
    public Optional<StudyPlan> getStudyPlanById(String id) {
        try {
            Long studyPlanId = Long.parseLong(id);
            return studyPlanRepository.findById(studyPlanId);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves study plan by threadId
     */
    public Optional<StudyPlan> getStudyPlanByThreadId(String threadId) {
        return studyPlanRepository.findByThreadId(threadId);
    }

    /**
     * Updates the status of a specific week in a study plan
     */
    public Optional<StudyPlan> updateWeekStatus(String id, int weekNumber, String status) {
        try {
            // Validate status parameter
            if (!isValidStatus(status)) {
                throw new IllegalArgumentException("Invalid status: " + status + ". Must be one of: NOT_STARTED, IN_PROGRESS, COMPLETED");
            }

            Long studyPlanId = Long.parseLong(id);
            Optional<StudyPlan> optionalStudyPlan = studyPlanRepository.findById(studyPlanId);
            
            if (optionalStudyPlan.isEmpty()) {
                return Optional.empty();
            }

            StudyPlan studyPlan = optionalStudyPlan.get();
            String studyPlanJson = studyPlan.getStudyPlan();
            
            if (studyPlanJson == null || studyPlanJson.trim().isEmpty()) {
                throw new IllegalArgumentException("Study plan data is null or empty");
            }

            // Parse the study plan JSON
            Map<String, Object> studyPlanData = objectMapper.readValue(studyPlanJson, Map.class);
            Object studyPlanArray = studyPlanData.get("studyPlan");
            
            if (!(studyPlanArray instanceof java.util.List)) {
                throw new IllegalArgumentException("Study plan data format is invalid");
            }

            java.util.List<Map<String, Object>> weeks = (java.util.List<Map<String, Object>>) studyPlanArray;
            
            // Find and update the specific week
            boolean weekFound = false;
            for (Map<String, Object> week : weeks) {
                Object weekNumberObj = week.get("weekNumber");
                if (weekNumberObj instanceof Integer && ((Integer) weekNumberObj).equals(weekNumber)) {
                    week.put("status", status);
                    weekFound = true;
                    break;
                }
            }

            if (!weekFound) {
                throw new IllegalArgumentException("Week number " + weekNumber + " not found in study plan");
            }

            // Convert back to JSON and save
            String updatedStudyPlanJson = objectMapper.writeValueAsString(studyPlanData);
            studyPlan.setStudyPlan(updatedStudyPlanJson);
            
            StudyPlan savedStudyPlan = studyPlanRepository.save(studyPlan);
            return Optional.of(savedStudyPlan);
            
        } catch (NumberFormatException e) {
            return Optional.empty();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process study plan JSON", e);
        }
    }

    /**
     * Validates if the provided status is one of the allowed values
     */
    private boolean isValidStatus(String status) {
        return "NOT_STARTED".equals(status) || 
               "IN_PROGRESS".equals(status) || 
               "COMPLETED".equals(status);
    }
}
