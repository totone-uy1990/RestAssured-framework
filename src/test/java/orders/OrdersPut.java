package orders;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import net.datafaker.Faker;
import models.ApiClientRequest;
import models.ApiClientResponse;
import models.OrderRequest;
import models.OrderResponse;

public class OrdersPut {

        private static final String ANSI_GREEN = "\u001b[32m";
        private static final String ANSI_RESET = "\u001b[0m";
        private static String token;
        private static Faker faker = new Faker();

        // Método auxiliar para obtener un ID de libro que sabemos que está en stock
        private int getAvailableBookId() {
                // Según la API, estos IDs de ficción suelen estar en stock (1, 3, 4, 6)
                int[] availableBooks = { 1, 3, 4, 6 };
                return availableBooks[faker.number().numberBetween(0, availableBooks.length - 1)];
        }

        @BeforeClass(alwaysRun = true)
        public void testGetToken() {
                ApiClientRequest requestPayload = new ApiClientRequest(
                                faker.name().firstName(),
                                faker.internet().emailAddress());

                ApiClientResponse responseModel = given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/api-clients/")
                                .contentType(ContentType.JSON)
                                .body(requestPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .log().all()
                                .extract().as(ApiClientResponse.class);

                token = responseModel.getAccessToken();
                System.out.println(ANSI_GREEN + "Token deserializado: " + token + ANSI_RESET);
        }

        // test authentication
        @Test
        public void testUpdateOrderWithoutAuth() {
                // Primero necesitamos una orden válida para tratar de actualizarla sin auth
                OrderRequest orderPayload = new OrderRequest(
                                getAvailableBookId(),
                                faker.name().fullName());

                OrderResponse orderResponse = given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .body(orderPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .extract().as(OrderResponse.class);

                String orderId = orderResponse.getOrderId();

                String updatePayload = """
                                {
                                    "customerName": "%s"
                                }
                                """.formatted(faker.name().fullName()).stripIndent();

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .pathParam("orderId", orderId)
                                .body(updatePayload)
                                .when()
                                .patch()
                                .then()
                                .statusCode(401) // Esperamos un error de no autorizado
                                .log().all();
        }

        @Test
        public void testUpdateOrder() {
                // Primero, creamos una orden para obtener un orderId válido
                OrderRequest orderPayload = new OrderRequest(
                                getAvailableBookId(),
                                faker.name().fullName());

                OrderResponse orderResponse = given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .body(orderPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .log().all()
                                .extract().as(OrderResponse.class);

                String orderId = orderResponse.getOrderId();

                // Ahora, actualizamos la orden con nuevos datos
                // La Simple Books API dice que solo podemos actualizar el 'customerName' usando
                // PATCH,
                // no podemos hacer PUT ni actualizar el bookId.
                String updatePayload = """
                                {
                                    "customerName": "%s"
                                }
                                """.formatted(faker.name().fullName()).stripIndent();

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .pathParam("orderId", orderId)
                                .body(updatePayload)
                                .when()
                                .patch() // Simple Books API soporta PATCH para actualizar
                                .then()
                                .statusCode(204) // Retorna 204 No Content en actualización exitosa
                                .log().all();
        }

}
