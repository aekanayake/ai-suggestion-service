package apex.wiley.com.demo.repository;

import apex.wiley.com.demo.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    
    Optional<StudyPlan> findByThreadId(String threadId);
    
    Optional<StudyPlan> findByCourseKeyAndUserKey(String courseKey, String userKey);
}
