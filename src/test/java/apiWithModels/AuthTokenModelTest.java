package apiWithModels;

import io.restassured.http.ContentType;
import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import net.datafaker.Faker;

import models.ApiClientRequest;
import models.ApiClientResponse;
import models.OrderRequest;
import models.OrderResponse;
import models.BookResponse;

public class AuthTokenModelTest {

    public static final String ANSI_GREEN = "\u001b[32m";
    public static final String ANSI_RESET = "\u001b[0m";

    private static String token;
    private static Faker faker = new Faker();
    private String orderId;

    @BeforeClass(alwaysRun = true)
    public void testGetToken() {
        ApiClientRequest requestPayload = new ApiClientRequest(
            faker.name().firstName(),
            faker.internet().emailAddress()
        );

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




    @Test
    public void testSubmitOrderAndDeserialize() {
        OrderRequest orderPayload = new OrderRequest(
            faker.number().numberBetween(1, 6),
            faker.name().fullName()
        );

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

        orderId = orderResponse.getOrderId();
        
        Assert.assertTrue(orderResponse.isCreated());
        Assert.assertNotNull(orderId);
        System.out.println(ANSI_GREEN + "Order ID obtenido desde modelo: " + orderId + ANSI_RESET);
    }

    @Test(dependsOnMethods = "testSubmitOrderAndDeserialize")
    public void testGetOrder() {
        given()
                .baseUri("https://simple-books-api.click")
                .pathParam("orderId", orderId)
                .basePath("/orders/{orderId}")
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId))
                .log().all();
    }

    @Test
    public void testGetOrdersAndDeserializeList() {  //validamos la consulta con parámetros y deserializamos la respuesta a un array de modelos
        BookResponse[] books = given()
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
                .log().all()
                .extract().as(BookResponse[].class);

        Assert.assertTrue(books.length > 0);
        Assert.assertNotNull(books[0].getName());
        System.out.println(ANSI_GREEN + "Título del primer libro en la lista: " + books[0].getName() + ANSI_RESET);
    }

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
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/books-schema.json"))
                .log().all();
    }
}
