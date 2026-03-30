package api;

import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ApiTest {

        @Test
        public void testGetRequest() {
                given()
                                .baseUri("https://jsonplaceholder.typicode.com")
                                .when()
                                .get("/posts/1")
                                .then()
                                .statusCode(200)
                                .body("userId", equalTo(1))
                                .body("id", equalTo(1));
        }

        @Test
        public void testRickyMortyAPI() {
                with()
                                .baseUri("https://rickandmortyapi.com/api/")
                                .basePath("/location/20")
                                .log().all();

        }

        // POST
        @Test
        public void dcrearUsuario() {
                String body = """
                                {
                                  "title": "Patos",
                                  "body": "Contenido del post",
                                  "id": "103",
                                  "number": "doce"
                                }
                                """;
                given()
                                .baseUri("https://jsonplaceholder.typicode.com")
                                .contentType(ContentType.JSON)// header
                                .body(body)
                                .when()
                                .post("/posts")
                                .then().statusCode(201)
                                .log().all();
        }

        // VALIDACION CAMPOS JSON

        @Test
        public void validarCampsenUnJSON() {
                Response response = given()
                                .when().get("https://jsonplaceholder.typicode.com/users/1")
                                .then()
                                .statusCode(200)
                                .extract().response();
                System.out.println(response.asString());

                String name, username, suite;

                name = response.jsonPath().getString("name");
                username = response.jsonPath().getString("username");
                suite = response.jsonPath().getString("adress.suite");

                System.out.println("Name: " + name);
                System.out.println("Username: " + username);
                System.out.println("suite: " + suite);

                // validations
                response.then().body("address.suite", equalTo("Apt. 556"));
                response.then().body("name", equalTo(name));
                response.then().body("suite", equalTo(suite));

        }

        @Test
        public void borrarElemento() {
                // post
                String body = """
                                {
                                  "title": "RORO",
                                  "body": "Contenido del post"
                                }
                                """;
                                 given()
                                .baseUri("https://jsonplaceholder.typicode.com")
                                .basePath("/posts")
                                .contentType(ContentType.JSON)
                                .body(body)
                                .when()
                                .post()
                                .then()
                                .statusCode(201);

                // GET
                Response response = given()
                                .baseUri("https://jsonplaceholder.typicode.com")
                                .basePath("/posts/1")
                                .contentType(ContentType.JSON)
                                .when()
                                .get()
                                .then()
                                .statusCode(200)
                                .extract().response();

                System.out.println(response.asString());

                String title;
                int id;
                title = response.jsonPath().getString("title");
                id = response.jsonPath().getInt("id");

                // validations
                response.then().body("title", equalTo(title));
                response.then().body("id", equalTo(id));

                // DELETE
                given()
                                .baseUri("https://jsonplaceholder.typicode.com")
                                .basePath("/posts/1")
                                .when()
                                .delete()
                                .then()
                                .statusCode(200);

                // Verificar que ya no existe (jsonplaceholder siempre devuelve 200)
                given()
                                .baseUri("https://jsonplaceholder.typicode.com")
                                .basePath("/posts/1")
                                .when()
                                .get()
                                .then()
                                .statusCode(200);

        }

}
