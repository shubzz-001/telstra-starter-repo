package au.com.telstra.simcardactivator.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sim")
public class SimActivationController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/activate")
    public ResponseEntity<String> activateSim(@RequestBody Map<String, String> payload) {
        String iccid = payload.get("iccid");
        String customerEmail = payload.get("customerEmail");

        if (iccid == null || customerEmail == null) {
            return ResponseEntity.badRequest().body("Missing required fields: iccid or customerEmail");
        }

        Map<String, String> actuatorRequest = new HashMap<>();
        actuatorRequest.put("iccid", iccid);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:8444/actuate",
                    actuatorRequest,
                    Map.class
            );

            boolean success = (Boolean) response.getBody().get("success");

            if (success) {
                System.out.println("✅ SIM " + iccid + " activated for " + customerEmail);
                return ResponseEntity.ok("Activation successful for: " + customerEmail);
            } else {
                System.out.println("❌ SIM activation failed for " + iccid);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Activation failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error contacting actuator service");
        }
    }
}
