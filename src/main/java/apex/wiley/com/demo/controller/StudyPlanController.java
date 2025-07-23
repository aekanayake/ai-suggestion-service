package apex.wiley.com.demo.controller;

import apex.wiley.com.demo.entity.StudyPlan;
import apex.wiley.com.demo.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/study-plan")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping
    public ResponseEntity<StudyPlan> createStudyPlan(@RequestBody Map<String, Object> requestPayload) {
        StudyPlan studyPlan = studyPlanService.createStudyPlan(requestPayload);
        return ResponseEntity.ok(studyPlan);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyPlan> getStudyPlanById(@PathVariable String id) {
        Optional<StudyPlan> studyPlan = studyPlanService.getStudyPlanById(id);
        return studyPlan.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/thread/{threadId}")
    public ResponseEntity<StudyPlan> getStudyPlanByThreadId(@PathVariable String threadId) {
        Optional<StudyPlan> studyPlan = studyPlanService.getStudyPlanByThreadId(threadId);
        return studyPlan.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/week/{weekNumber}")
    public ResponseEntity<StudyPlan> updateWeekStatus(
            @PathVariable String id,
            @PathVariable int weekNumber,
            @RequestBody Map<String, Object> requestBody) {

        try {
            String status = (String) requestBody.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<StudyPlan> updatedStudyPlan = studyPlanService.updateWeekStatus(id, weekNumber, status.toUpperCase());
            return updatedStudyPlan.map(ResponseEntity::ok)
                                  .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
