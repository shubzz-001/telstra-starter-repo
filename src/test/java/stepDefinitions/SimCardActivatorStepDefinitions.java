package stepDefinitions;

import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SimCardActivatorStepDefinitions {

    @LocalServerPort
    private int port = 8080;

    private final RestTemplate restTemplate = new RestTemplate();

    private ResponseEntity<String> response;
    private ResponseEntity<Map> queryResponse;

    @Given("the actuator service is running")
    public void theActuatorServiceIsRunning() {
        // Optionally, you could check if the actuator endpoint is reachable
        ResponseEntity<String> actuatorResponse =
                restTemplate.getForEntity("http://localhost:8444/actuate", String.class);
        assertNotNull(actuatorResponse);
    }

    @When("I submit an activation request with ICCID {string} and customer email {string}")
    public void iSubmitAnActivationRequest(String iccid, String email) {
        String url = "http://localhost:" + port + "/activate";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("iccid", iccid);
        requestBody.put("customerEmail", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.postForEntity(url, entity, String.class);
    }

    @Then("the activation should be successful")
    public void activationShouldBeSuccessful() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("success") || response.getBody().contains("activated"));
    }

    @Then("the activation should fail")
    public void activationShouldFail() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("failed") || response.getBody().contains("false"));
    }

    @And("querying the record with ID {int} should return active true")
    public void queryRecordActiveTrue(Integer id) {
        String url = "http://localhost:" + port + "/sim?simCardId=" + id;
        queryResponse = restTemplate.getForEntity(url, Map.class);

        assertNotNull(queryResponse.getBody());
        assertEquals(true, queryResponse.getBody().get("active"));
    }

    @And("querying the record with ID {int} should return active false")
    public void queryRecordActiveFalse(Integer id) {
        String url = "http://localhost:" + port + "/sim?simCardId=" + id;
        queryResponse = restTemplate.getForEntity(url, Map.class);

        assertNotNull(queryResponse.getBody());
        assertEquals(false, queryResponse.getBody().get("active"));
    }
}
