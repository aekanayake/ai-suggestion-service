package apex.wiley.com.demo.service;

import apex.wiley.com.demo.dto.StudentData;

public interface StudentService {
    StudentData getStudentAnalytics(String contextId, String ltiUserId);
}

