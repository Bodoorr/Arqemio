package com.ga.company_service.cucumber;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectSteps {
    private Response response;

    @When("I request projects without a token")
    public void requestWithoutToken() {
        response = given().get("http://localhost:8082/company/projects");
    }

    @When("I request projects with an invalid token")
    public void requestWithInvalidToken() {
        response = given().header("Authorization", "Bearer invalid-token")
                .get("http://localhost:8082/company/projects");
    }

    @Then("the response status should be {int}")
    public void checkStatus(int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode());
    }
}
