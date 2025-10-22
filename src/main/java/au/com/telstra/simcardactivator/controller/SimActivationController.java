package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.entity.SimCardRecord;
import au.com.telstra.simcardactivator.repository.SimCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/sim")
public class SimActivationController {

    @Autowired
    private SimCardRepository simCardRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/activate")
    public ResponseEntity<String> activateSim(@RequestBody Map<String, String> payload) {
        String iccid = payload.get("iccid");
        String customerEmail = payload.get("customerEmail");

        if (iccid == null || customerEmail == null) {
            return ResponseEntity.badRequest().body("Missing iccid or customerEmail");
        }

        // Send request to actuator
        Map<String, String> actuatorRequest = new HashMap<>();
        actuatorRequest.put("iccid", iccid);

        boolean success = false;

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:8444/actuate",
                    actuatorRequest,
                    Map.class
            );

            success = (Boolean) response.getBody().get("success");

            // Save record in DB
            SimCardRecord record = new SimCardRecord(iccid, customerEmail, success);
            simCardRepository.save(record);

            if (success) {
                return ResponseEntity.ok("Activation successful for: " + customerEmail);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Activation failed for: " + customerEmail);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error contacting actuator service");
        }
    }

    // ✅ New endpoint to query record by ID
    @GetMapping("/query")
    public ResponseEntity<?> getSimRecord(@RequestParam Long simCardId) {
        Optional<SimCardRecord> recordOpt = simCardRepository.findById(simCardId);

        if (recordOpt.isPresent()) {
            SimCardRecord record = recordOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("iccid", record.getIccid());
            response.put("customerEmail", record.getCustomerEmail());
            response.put("active", record.isActive());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Record not found for ID: " + simCardId);
        }
    }
}
