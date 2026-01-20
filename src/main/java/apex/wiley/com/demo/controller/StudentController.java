package apex.wiley.com.demo.controller;

import apex.wiley.com.demo.dto.StudentData;
import apex.wiley.com.demo.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/analytics")
    public ResponseEntity<StudentData> getStudentAnalytics() {
        StudentData studentData = studentService.getStudentAnalytics();
        return ResponseEntity.ok(studentData);
    }
}

