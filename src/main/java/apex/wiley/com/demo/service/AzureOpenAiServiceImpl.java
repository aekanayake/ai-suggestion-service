package apex.wiley.com.demo.service;

import com.azure.ai.openai.assistants.AssistantsClient;
import com.azure.ai.openai.assistants.AssistantsClientBuilder;
import com.azure.ai.openai.assistants.models.*;
import com.azure.core.credential.AzureKeyCredential;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AzureOpenAiServiceImpl implements AzureOpenAiService{

    @Value("${azure.openai.api.key}")
    private String azureApiKey;

    @Value("${azure.openai.endpoint}")
    private String azureEndpoint;

    @Value("${azure.openai.assistant.id}")
    private String studyPlanCreationAssistantId;

    private AssistantsClient client;

    @PostConstruct
    public void init() {
        System.out.println("Azure OpenAI Service Initialized");

        client = new AssistantsClientBuilder()
                .credential(new AzureKeyCredential(azureApiKey))
                .endpoint(azureEndpoint)
                .buildClient();
    }

    public String createStudyPlan(String courseContentPayload) throws InterruptedException {
        System.out.println("Creating study plan with payload: " + courseContentPayload);

        AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());
        ThreadMessage threadMessage = client.createMessage(thread.getId(), new ThreadMessageOptions(MessageRole.USER,
                courseContentPayload));
        System.out.println("Thread created with ID: " + thread.getId());
        System.out.println("Running the thread with Assistant ID: " + studyPlanCreationAssistantId);
        ThreadRun run = client.createRun(thread.getId(), new CreateRunOptions(studyPlanCreationAssistantId));


        do {
            run = client.getRun(run.getThreadId(), run.getId());
            System.out.println("Run status: " + run.getStatus());
            Thread.sleep(1000);
        } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);

        PageableList<ThreadMessage> messages = client.listMessages(run.getThreadId());
        List<ThreadMessage> data = messages.getData();
        for (int i = 0; i < data.size(); i++) {
            ThreadMessage dataMessage = data.get(i);
            MessageRole role = dataMessage.getRole();
            for (MessageContent messageContent : dataMessage.getContent()) {
                if ("assistant".equals(role.toString())) {
                    MessageTextContent messageTextContent = (MessageTextContent) messageContent;
                    String response = messageTextContent.getText().getValue();
                    response = response.replaceFirst("^```json\\s*", "")  // Remove starting ```json
                            .replaceFirst("```\\s*$", "");
                    System.out.println(i + ": Role = " + role + ", content = "
                            + response);
                    return response;
                }

            }
        }
        return null;
    }

    public String createThread() {
        AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());
        return thread.getId();
    }



}
