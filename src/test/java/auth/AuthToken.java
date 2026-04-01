package auth;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;

import org.testng.annotations.Test;
import io.restassured.response.Response;

public class AuthToken {

    @Test
    public void testObtenerToken() {

        String body = """
                {
                 "clientName": "tutonil",
                 "clientEmail": "Tutoni@gmail.com"
                }
                    """;

        given().baseUri("https://simple-books-api.click")
                .basePath("/api-clients/")
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(201)
                .log().all();
    }

    String token="fbe20bd25813b1028843eb3babaf72a57ca2548c73ebfa55f67c722ecd9e88c3";

}
