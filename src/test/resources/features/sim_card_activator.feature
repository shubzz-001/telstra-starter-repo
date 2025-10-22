Feature: SIM Card Activation

  Scenario: Successful SIM activation
    Given the actuator service is running
    When I submit an activation request with ICCID "1255789453849037777" and customer email "john.doe@example.com"
    Then the activation should be successful
    And querying the record with ID 1 should return active true

  Scenario: Failed SIM activation
    Given the actuator service is running
    When I submit an activation request with ICCID "8944500102198304826" and customer email "jane.doe@example.com"
    Then the activation should fail
    And querying the record with ID 2 should return active false
