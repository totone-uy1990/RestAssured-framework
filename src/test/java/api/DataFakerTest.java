package api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import net.datafaker.Faker;
import org.testng.annotations.Test;

public class DataFakerTest {

    Faker faker = new Faker();

    @Test
    public void testCrearUsuarioConFaker() {
        String titulo = faker.book().title();
        String body = String.format("""
            {
              "title": "%s",
              "body": "%s",
              "userId": %d
            }
            """, 
            titulo,
            faker.lorem().paragraph(),
            faker.number().numberBetween(1, 10)
        );

        given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/posts")
            .then()
            .statusCode(201)
            .body("title", equalTo(titulo))
            .log().all();
    }

    @Test
    public void testDatosFaker() {
        System.out.println("Nombre: " + faker.name().fullName());
        System.out.println("Email: " + faker.internet().emailAddress());
        System.out.println("Teléfono: " + faker.phoneNumber().phoneNumber());
        System.out.println("Dirección: " + faker.address().fullAddress());
        System.out.println("Empresa: " + faker.company().name());
        System.out.println("Username: " + faker.internet().username());
        System.out.println("Password: " + faker.internet().password());
    }
}
