package apex.wiley.com.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;

@Entity
@Table(name = "study_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_key", nullable = false)
    private String courseKey;

    @Column(name = "user_key", nullable = false)
    private String userKey;

    @Column(name = "thread_id", nullable = false)
    private String threadId;

    @Column(name = "course_start_date")
    private LocalDate courseStartDate;

    @Column(name = "course_end_date")
    private LocalDate courseEndDate;

    @Column(name = "study_plan", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String studyPlan;

    @Column(name = "course_outline", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String courseOutline;
}
