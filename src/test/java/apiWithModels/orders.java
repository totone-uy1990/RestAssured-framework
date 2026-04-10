package apiWithModels;

import io.restassured.http.ContentType;
import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import net.datafaker.Faker;
import models.ApiClientRequest;
import models.ApiClientResponse;
import models.OrderRequest;
import models.OrderResponse;

public class orders {

    private static final String ANSI_GREEN = "\u001b[32m";
    private static final String ANSI_RESET = "\u001b[0m";
    private static String token;
    private static Faker faker = new Faker();
    private String orderId;

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

    // TEST

    @Test // validate response 201
    public void testSubmitOrderAndDeserialize() {

        OrderRequest orderPayload = new OrderRequest(
                faker.number().numberBetween(1, 6),
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
                faker.number().numberBetween(1, 6),
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
                faker.number().numberBetween(1, 6),
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



@Test
        public void testSubmitOrderWithInvalidToken() {
                OrderRequest orderPayload = new OrderRequest(
                        faker.number().numberBetween(1, 6),
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


@Test 
        public void testSubmitOrderWithInvalidBookId() {
                OrderRequest orderPayload = new OrderRequest(
                        999, // ID de libro no existente
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
                        .statusCode(400) // Esperamos un error de solicitud incorrecta
                        .log().all();
        }

  @Test
        public void testSubmitOrderWithEmptyCustomerName() {
                OrderRequest orderPayload = new OrderRequest(
                        faker.number().numberBetween(1, 6),
                        ""); // Nombre de cliente vacío
        
                given()
                        .baseUri("https://simple-books-api.click")
                        .basePath("/orders")
                        .contentType(ContentType.JSON)
                        .auth().oauth2(token)
                        .body(orderPayload)
                        .when()
                        .post()
                        .then()
                        .statusCode(400) // Esperamos un error de solicitud incorrecta
                        .log().all();
        }
@Test 
        public void testSubmitOrderWithInvalidCustomerName() {
                OrderRequest orderPayload = new OrderRequest(
                        faker.number().numberBetween(1, 6),
                        "A"); // Nombre de cliente demasiado corto
        
                given()
                        .baseUri("https://simple-books-api.click")
                        .basePath("/orders")
                        .contentType(ContentType.JSON)
                        .auth().oauth2(token)
                        .body(orderPayload)
                        .when()
                        .post()
                        .then()
                        .statusCode(400) // Esperamos un error de solicitud incorrecta
                        .log().all();
        }

        @Test
        public void testSubmitOrderWithNonNumericBookId() {     
                OrderRequest orderPayload = new OrderRequest(
                        Integer.parseInt("abc"), // ID de libro no numérico
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
                        .statusCode(400) // Esperamos un error de solicitud incorrecta
                        .log().all();
        }

        // test idempotencia
        @Test
        public void testSubmitSameOrderMultipleTimes() {
                OrderRequest orderPayload = new OrderRequest(
                        faker.number().numberBetween(1, 6),
                        faker.name().fullName());
        
                // Enviar la misma orden 3 veces
                for (int i = 0; i < 3; i++) {
                        given()
                                .baseUri("https://simple-books-api.click")
                                .basePath("/orders")
                                .contentType(ContentType.JSON)
                                .auth().oauth2(token)
                                .body(orderPayload)
                                .when()
                                .post()
                                .then()
                                .statusCode(201) // Esperamos que cada solicitud sea exitosa
                                .log().all();
                }
        }
        


        //Test PUT HAPPY PATH

        @Test
        public void testUpdateOrder() {
                // Primero, creamos una orden para obtener un orderId válido
                OrderRequest orderPayload = new OrderRequest(
                        faker.number().numberBetween(1, 6),
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
                OrderRequest updatePayload = new OrderRequest(
                        faker.number().numberBetween(1, 6),
                        faker.name().fullName());

                given()
                        .baseUri("https://simple-books-api.click")
                        .basePath("/orders/{orderId}")
                        .contentType(ContentType.JSON)
                        .auth().oauth2(token)
                        .pathParam("orderId", orderId)
                        .body(updatePayload)
                        .when()
                        .put()
                        .then()
                        .statusCode(200) // Esperamos un código de éxito para la actualización
                        .log().all();
        }

@Test
        public void testUpdateOrderWithInvalidId() {
                OrderRequest updatePayload = new OrderRequest(
                        faker.number().numberBetween(1, 6),
                        faker.name().fullName());

                given()
                        .baseUri("https://simple-books-api.click")
                        .basePath("/orders/{orderId}")
                        .contentType(ContentType.JSON)
                        .auth().oauth2(token)
                        .pathParam("orderId", "invalid_id") // ID no válido
                        .body(updatePayload)
                        .when()
                        .put()
                        .then()
                                .statusCode(400) // Esperamos un error de solicitud incorrecta
                        .log().all();
        }

}
