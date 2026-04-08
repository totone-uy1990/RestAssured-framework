package auth;

import io.restassured.http.ContentType;
import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import net.datafaker.Faker;

public class AuthToken {

        // https://github.com/vdespa/introduction-to-postman-course/blob/main/simple-books-api.md

        public static final String ANSI_GREEN = "\u001b[32m";
        public static final String ANSI_RESET = "\u001b[0m";

        private static String token;

        private static Faker faker = new Faker();

        @BeforeClass(alwaysRun = true)
        public static void testGetToken() {
                Faker faker = new Faker();
                String clientName = faker.name().firstName();
                String clientEmail = faker.internet().emailAddress();
                String body = String.format("""
                                {
                                 "clientName": "%s",
                                 "clientEmail": "%s"
                                 }
                                    """, clientName, clientEmail);

                Response response = given().baseUri("https://simple-books-api.click")
                                .basePath("/api-clients/")
                                .contentType(ContentType.JSON) // valida que el content type de la petición sea JSON, si no lo es, la petición fallará con un error 415 Unsupported Media Type
                                .body(body)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .log().all()
                                .extract().response();

                System.out.println("Response: " + response.asString());
                token = response.jsonPath().getString("accessToken");
                System.out.println(ANSI_GREEN + "Token obtenido: " + token + ANSI_RESET);
        }

        @Test
        public void testSubmitOrder() {
                String customerName = faker.name().firstName();

                String body = String.format("""
                                {
                                 "bookId": 3,
                                 "customerName": "%s"
                                }
                                    """, customerName);

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

        String orderId;

        @Test(invocationCount = 2)
        public void testSubmitOrder2() {
                String customerName = faker.name().fullName();
                int bookId = faker.number().numberBetween(1, 6);

                String body = String.format("""
                                {
                                 "bookId": %d,
                                 "customerName": "%s"
                                }
                                    """, bookId, customerName);

                Response response = given().baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .body(body)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .log().all().extract().response();

                orderId = response.jsonPath().getString("orderId");

                System.out.println(ANSI_GREEN + "Order ID: " + orderId + ANSI_RESET);
        }

        @Test(invocationCount = 3)
        public void testGetOrder() {

                Response response = given()
                                .baseUri("https://simple-books-api.click")
                                .pathParam("orderId", orderId)
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get()
                                .then()
                                .statusCode(200)
                                .log().all().extract().response();

        }

        @Test()
        public void testGetOrdersWithQueryAndPathParam() {

                Response response = given()
                                .baseUri("https://simple-books-api.click")
                                .queryParam("limit", 10)
                                .queryParam("type", "fiction")
                                .basePath("/books")
                                .contentType(ContentType.JSON)
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get()
                                .then()
                                .statusCode(200)
                                .log().all().extract().response();

        }

        @Test
        public void testGetAllBooks() {
                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/books")
                                .contentType(ContentType.JSON)
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get()
                                .then()
                                .statusCode(200)
                                .log().all();
        }
/*validacion de campos a través de un esquema JSON, 
para asegurarnos de que la respuesta cumple con la estructura 
esperada y contiene los campos necesarios:
*/  
@Test
        public void testValidateQueryParametersSchema() {
                given()
                        .baseUri("https://simple-books-api.click")
                        .queryParam("limit", 10)
                        .queryParam("type", "fiction")
                        .basePath("/books")
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .body(matchesJsonSchemaInClasspath("schemas/books-schema.json"))
                        .log().all();



                }
        
}
