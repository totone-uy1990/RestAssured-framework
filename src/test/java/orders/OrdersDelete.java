package orders;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import net.datafaker.Faker;
import models.ApiClientRequest;
import models.ApiClientResponse;
import models.OrderRequest;
import models.OrderResponse;

public class OrdersDelete {

        private static final String ANSI_GREEN = "\u001b[32m";
        private static final String ANSI_RESET = "\u001b[0m";
        private static String token;
        private static Faker faker = new Faker();

        private int getAvailableBookId() {
                int[] availableBooks = { 1, 3, 4, 6 };
                return availableBooks[faker.number().numberBetween(0, availableBooks.length - 1)];
        }

        private String createOrder() {
                OrderRequest orderPayload = new OrderRequest(
                                getAvailableBookId(),
                                faker.name().fullName());

                return given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .body(orderPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .extract().path("orderId");
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

        // TEST DELETE HAPPY PATH
        @Test
        public void testDeleteOrderHappyPath() {
                String orderId = createOrder();

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .pathParam("orderId", orderId)
                                .when()
                                .delete()
                                .then()
                                .statusCode(204)
                                .log().all();
        }

        // TEST DELETE - Validar que la orden fue eliminada
        @Test
        public void testDeleteOrderAndVerifyIsDeleted() {
                String orderId = createOrder();

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .pathParam("orderId", orderId)
                                .when()
                                .delete()
                                .then()
                                .statusCode(204)
                                .log().all();

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .pathParam("orderId", orderId)
                                .when()
                                .get()
                                .then()
                                .statusCode(404)
                                .log().all();
        }

        // TEST DELETE - Sin auth
        @Test
        public void testDeleteOrderWithoutAuth() {
                String orderId = createOrder();

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .pathParam("orderId", orderId)
                                .when()
                                .delete()
                                .then()
                                .statusCode(401)
                                .log().all();
        }

        // TEST DELETE - Token inválido
        @Test
        public void testDeleteOrderWithInvalidToken() {
                String orderId = createOrder();

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .auth().oauth2("invalid_token")
                                .pathParam("orderId", orderId)
                                .when()
                                .delete()
                                .then()
                                .statusCode(401)
                                .log().all();
        }

        // TEST DELETE - Orden que no existe
        @Test
        public void testDeleteNonExistentOrder() {
                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/orden-inexistente-123")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .when()
                                .delete()
                                .then()
                                .statusCode(404)
                                .log().all();
        }

        // TEST DELETE - Idempotencia (eliminar orden ya eliminada)
        @Test
        public void testDeleteOrderTwiceIsIdempotent() {
                String orderId = createOrder();

                Response firstDelete = given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .pathParam("orderId", orderId)
                                .when()
                                .delete()
                                .then()
                                .extract().response();

                Assert.assertEquals(firstDelete.statusCode(), 204,
                                "Primera eliminación debe retornar 204");

                Response secondDelete = given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders/{orderId}")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .pathParam("orderId", orderId)
                                .when()
                                .delete()
                                .then()
                                .extract().response();

                Assert.assertEquals(secondDelete.statusCode(), 404,
                                "Segunda eliminación debe retornar 404 (orden ya no existe)");
        }
}