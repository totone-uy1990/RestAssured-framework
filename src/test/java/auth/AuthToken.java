package auth;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;

import org.testng.annotations.Test;
import io.restassured.response.Response;

public class AuthToken {
        // URL REFERENCE:
        // https://github.com/vdespa/introduction-to-postman-course/blob/main/simple-books-api.md
        @Test
        public void testGetToken() {

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

        String token = "fbe20bd25813b1028843eb3babaf72a57ca2548c73ebfa55f67c722ecd9e88c3";

        // submit an order with the token obtained in the previous test
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

        // other submmit order with auth
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
                                .auth().oauth2(token) // other way to pass the token in the header of authorization with
                                                      // the format "Bearer " + token
                                .body(body)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .log().all();
        }

        // get an order with the token obtained in the previous test
        @Test
        public void testGetOrder() {
                String orderID = "Gku2S33oaqF2NzUz3aDoo";
                Response response = given()
                                .baseUri("https://simple-books-api.click")
                                .pathParam("OrderId", orderID) // es simplemente un parametro como valor
                                .basePath("/orders/{OrderId}") // endpoint con el path parameter
                                .contentType(ContentType.JSON)
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get()
                                .then()
                                .statusCode(200)
                                .log().all().extract().response();

        }

        // request with query an pathparameter
        @Test
        public void testGetOrdersWithQueryAndPathParam() {
                String orderID = "Gku2S33oaqF2NzUz3aDoo";
                Response response = given()
                                .baseUri("https://simple-books-api.click")
                                .queryParam("limit", 10) // query parameter para limitar el numero de resultados a 1
                                .queryParam("type","fiction")                                
                                .basePath("/books") // endpoint con el path parameter
                                .contentType(ContentType.JSON)
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get()
                                .then()
                                .statusCode(200)
                                .log().all().extract().response();

        }


}
