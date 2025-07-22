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
            
            // Convert the course outline to JSON string
            String courseOutlineJson = objectMapper.writeValueAsString(courseOutline);
            
            StudyPlan studyPlan = new StudyPlan();
            studyPlan.setCourseKey(courseKey);
            studyPlan.setUserKey(userKey);
            studyPlan.setThreadId(UUID.randomUUID().toString()); // Generate new threadId
            studyPlan.setCourseStartDate(courseStartDate);
            studyPlan.setCourseEndDate(courseEndDate);
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
}
