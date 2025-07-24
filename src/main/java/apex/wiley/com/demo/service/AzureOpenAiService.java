package apex.wiley.com.demo.service;

public interface AzureOpenAiService {

    String createStudyPlan(String courseContentPayload) throws InterruptedException;

    String reviseStudyPlan(String originalStudyPlanPayload) throws InterruptedException;
}
