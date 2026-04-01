package auth;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;

import org.testng.annotations.Test;
import io.restassured.response.Response;public class AuthToken {
    

    Response response;

    @Test
    public Response obtenerToken() {

        String body = """
                {
                 "clientName": "tutoni",
                 "clientEmail": "Tutoni@gmai.com"
                }
                    """;

        // obtencion del token

        response = given().baseUri("https://simple-books-api.click")
                .basePath("/api-clients/")
                .header("Authorization", "Bearer " + body)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(201)
                .log().all()
                .extract()
                .response();

        return response;
    }

}
