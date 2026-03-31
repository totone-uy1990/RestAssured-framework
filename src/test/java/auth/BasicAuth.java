package auth;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import org.testng.annotations.Test;

public class BasicAuth {

    @Test
    public void testBasicAuth() {
        given()
                .baseUri("https://httpbin.org")
                .basePath("/basic-auth/user/passwd")
                .auth()
                .basic("user", "passwd")
                .get()
                .then()
                .log().all()
                .statusCode(200);

    }

    @Test
    public void testBasicAuthEndpoint() {

        /*
         
          pasamos el base64 del usuario y contraseña en el header 
          de authorization, con el formato "Basic " + base64(usuario:contraseña)
          no necesitamos llamar al metodo auth().basic() porque ya estamos pasando 
          el header de authorization con el formato correcto,  
         */

        given()

                .header("Authorization", "Basic cG9zdG1hbjpwYXNzd29yZA==")
                .when()
                .get("https://postman-echo.com/basic-auth")
                .then()
                .statusCode(200)
                .log().all();
    }

    @Test
    public void testBasicAuthEndpoint2() {
        given()
                .baseUri("https://postman-echo.com")
                .basePath("/basic-auth")
                .auth()
                .basic("postman", "password") //se encarga de generar el header de authorization con el formato correcto
                .get()
                .then()
                .statusCode(200);
    }

}