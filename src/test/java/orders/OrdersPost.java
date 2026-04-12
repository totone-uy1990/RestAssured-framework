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

public class OrdersPost {

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

        @DataProvider(name = "invalidOrderData")
        public Object[][] invalidOrderData() {
                return new Object[][] {
                                { "ID de libro no existente", new OrderRequest(999, faker.name().fullName()) },
                                { "Nombre de cliente vacío", new OrderRequest(getAvailableBookId(), "") },
                                { "Nombre de cliente demasiado corto",
                                                new OrderRequest(getAvailableBookId(), "A") },
                                { "ID de libro no numérico", new OrderRequest(23456789, faker.name().fullName()) }
                };
        }

        @DataProvider(name = "emptyFieldsOrderData")
        public Object[][] emptyFieldsOrderData() {
                return new Object[][] {
                                { "Solo el bookId está presente y el customerName es nulo",
                                                new OrderRequest(getAvailableBookId(), null) },
                                { "Solo el bookId está presente y el customerName es vacío",
                                                new OrderRequest(getAvailableBookId(), "") },
                                { "Solo el customerName está presente y el bookId es 0",
                                                new OrderRequest(0, faker.name().fullName()) }
                };
        }

        // TEST

        @Test // validate response 201
        public void testSubmitOrderhappyPath() {

                OrderRequest orderPayload = new OrderRequest(
                                getAvailableBookId(),
                                faker.name().fullName());

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .body(orderPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(201)
                                .log().all();
        }

        // schema validation

        @Test
        public void testSubmitOrderAndValidateSchema() {
                OrderRequest orderPayload = new OrderRequest(
                                getAvailableBookId(),
                                faker.name().fullName());

                given()
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
                                .assertThat()
                                .body(matchesJsonSchemaInClasspath("schemas/orderResponseSchema.json"));
        }

        // negative test

        @Test
        public void testSubmitOrderWithoutAuth() {
                OrderRequest orderPayload = new OrderRequest(
                                getAvailableBookId(),
                                faker.name().fullName());

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .body(orderPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(401)
                                .log().all();
        }

        // test authentication
        @Test
        public void testSubmitOrderWithInvalidToken() {
                OrderRequest orderPayload = new OrderRequest(
                                getAvailableBookId(),
                                faker.name().fullName());

                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .auth().oauth2("invalid_token") // Token no válido
                                .body(orderPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(401) // Esperamos un error de no autorizado
                                .log().all();
        }

        // negative test data driven
        @Test(dataProvider = "emptyFieldsOrderData")
        public void testSubmitOrderWithEmptyFields(String description, OrderRequest orderPayload) {
                given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .body(orderPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(400) // Esperamos un error de solicitud incorrecta porque falta un campo o
                                                 // está vacío
                                .log().all();
        }

        // test de no idempotencia:
        /*
         * Verifica que POST crea recursos nuevos cada vez por mas que el payload sea el
         * mismo,
         * lo cual es un comportamiento común en APIs REST para POST. Si el API fuera
         * idempotente,
         * esperaríamos que la misma solicitud con el mismo payload no creara múltiples
         * recursos
         * o devolviera el mismo resultado cada vez.
         * Dado que Simple Books API no implementa idempotencia real para POST, este
         * test documenta ese comportamiento.
         */

        @Test
        public void testSubmitSameOrderMultipleTimes() {
                OrderRequest orderPayload = new OrderRequest(
                                getAvailableBookId(),
                                faker.book().title());

                // Enviar la misma orden 3 veces
                String firstOrderId = null;
                for (int i = 0; i < 3; i++) {
                        Response response = given()
                                        .baseUri("https://simple-books-api.click")
                                        .basePath("/orders")
                                        .contentType(ContentType.JSON)
                                        .auth().oauth2(token)
                                        .body(orderPayload)
                                        .when()
                                        .post()
                                        .then()
                                        .log().all()
                                        .extract().response();

                        if (i == 0) {
                                Assert.assertEquals(response.statusCode(), 201,
                                                "La primera solicitud debe crear la orden exitosamente");
                                firstOrderId = response.jsonPath().getString("orderId");
                                Assert.assertNotNull(firstOrderId, "El orderId no debe ser nulo");
                        } else {
                                Assert.assertEquals(response.statusCode(), 201,
                                                "El API devuelve 201 creando nuevas órdenes");

                                String currentOrderId = response.jsonPath().getString("orderId");
                                if (currentOrderId.equals(firstOrderId)) {
                                        Assert.fail("OrderIds son iguales - la API está implementando idempotencia (raro para POST)");
                                }
                        }
                }
        }



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

                String updatePayload = "{ \"customerName\": \"" + faker.name().fullName() + "\" }";

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
}
