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


//submit an order with the token obtained in the previous test
        @Test
public void testSubmitOrder() {
        String body = """
                {
                 "bookId": 1,
                 "customerName": "Tutoni"
                }
                    """;

        given().baseUri("https://simple-books-api.click")
                .basePath("/orders")
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(201)
                .log().all();
    }







//otrer submmit order
@Test
public void testSubmitOrder2() {
        String body = """
                {
                 "bookId": 4,
                 "customerName": "Tutoni"
                }
                    """;

        given().baseUri("https://simple-books-api.click")
                .basePath("/orders")
                .contentType(ContentType.JSON)
               .auth().oauth2(token) //otra forma de pasar el token, sin necesidad de escribir el header de authorization, el metodo oauth2 se encarga de generar el header con el formato correcto "Bearer " + token
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(201)
                .log().all();
    }





}
