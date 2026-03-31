package api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.response.Response;

public class SoapRequestTest {

        String bodySoapNormalInput = """
                        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                                       xmlns:tns="http://www.dataaccess.com/webservicesserver/">
                             <soap:Body>
                                   <tns:NumberToWords>
                                            <tns:ubiNum>500</tns:ubiNum>
                                   </tns:NumberToWords>
                             </soap:Body>
                        </soap:Envelope>
                           """;
        String bodySoapInvalidInput = """
                        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                                       xmlns:tns="http://www.dataaccess.com/webservicesserver/">
                             <soap:Body>
                                   <tns:NumberToWords>
                                            <tns:ubiNum>abc</tns:ubiNum>
                                   </tns:NumberToWords>
                             </soap:Body>
                        </soap:Envelope>
                           """; // valor no numérico para probar el caso negativo
        // colors
        public static final String ANSI_GREEN = "\u001b[32m";
        public static final String ANSI_R = "\u001b[0m";

        @Test
        public void testSoarequest() {
                System.out.println(ANSI_GREEN + "PRUEBA DE CASO POSITIVO: " + ANSI_R);

                System.out.println(ANSI_GREEN + "FORMA DE HACERLO 1: " + ANSI_R + bodySoapNormalInput);
                Response response = given()
                                .baseUri("https://www.dataaccess.com")
                                .basePath("/webservicesserver/NumberConversion.wso")
                                .contentType("text/xml; charset=UTF-8")
                                .body(bodySoapNormalInput)
                                .post()
                                .then()
                                .log().all()
                                .statusCode(200)
                                .extract().response();

                System.out.println(ANSI_GREEN
                                + "ESTE ES EL CUEROPO DE LA RESPUESTA: "
                                + response.getBody().asString());

                // otra forma de hacerlo AQUI CON assertions en el mismo bloque de código, sin
                // necesidad de extraer la respuesta para luego hacer las validaciones

        }

        @Test
        public void testSoapRequestWithAssertions() {

                // happy path
                System.out.println(ANSI_GREEN + "FORMA DE HACERLO 2: " + ANSI_R);
                Response response2 = given()
                                .contentType("text/xml; charset=UTF-8")/*
                                                                        * es un header que indica el tipo de contenido
                                                                        * que se está enviando en la solicitud, en este
                                                                        * caso, XML
                                                                        * con codificación
                                                                        * UTF-8.
                                                                        */
                                .baseUri("https://www.dataaccess.com")
                                .basePath("/webservicesserver/NumberConversion.wso")
                                .body(bodySoapNormalInput)
                                .when()
                                .post()
                                .then()
                                .log().all()
                                .statusCode(200)
                                .extract().response();

                System.out.println(ANSI_GREEN
                                + "ESTE ES EL CUERPO DE LA RESPUESTA: DE LA FORMA 2: " + ANSI_R
                                + response2.getBody().asString());

                // navegamos y extraemos el valor de las respuesta:

                // VALUES:
              //  String xmlResponse = response2.getBody().asString();

                String result = response2.xmlPath().getString("Envelope.Body.NumberToWordsResponse.NumberToWordsResult"); // xpath estricto
               
                                String alternativeResult = response2.xmlPath().getString("**NumberToWordsResult").trim(); // xpath con trim
                                                                                                    // para eliminar
                                                                                                    // espacios en
                                                                                                    // blanco

                // String expectedResult ="five hundred";

                // validar que el resultado es exactamente "five hundred" usando Assert de
                // TestNG
                System.out.println(ANSI_GREEN + "EL RESULTADO ES: "+ alternativeResult + ANSI_R + result);

                Assert.assertEquals(alternativeResult, "five hundred", "El resultado no es el esperado");
                /*
                 * considerando que la especificacion no nos dice nada sobre espacios
                 * adicionales,
                 * es importante usar trim() para eliminar cualquier espacio en blanco antes o
                 * después del resultado esperado.
                 * Esto ayuda a evitar fallos en la prueba debido a espacios no deseados en la
                 * respuesta.
                 */

                // con hamcrest

                /*
                 * busca la cadena "five hundred" en cualquier parte del resultado, lo que es
                 * útil si no estamos seguros de la estructura
                 * exacta de la respuesta o si el resultado puede contener información
                 * adicional además de "five hundred"
                 * pero es menos eficiente.
                 * .body(containsString("five hundred"));
                 */

        }

        // negative case solo para validar que el servicio maneja correctamente entradas
        // no válidas, como texto en lugar de números, y devuelve un error adecuado.
        @Test
        public void testSoapRequestNegative() {
                System.out.println(ANSI_GREEN + "PRUEBA DE CASO NEGATIVO: " + ANSI_R);
                given()
                                .contentType("text/xml; charset=UTF-8")
                                .baseUri("https://www.dataaccess.com")
                                .basePath("/webservicesserver/NumberConversion.wso")
                                .body(bodySoapInvalidInput)
                                .when()
                                .post()
                                .then()
                                .log().all()
                                .statusCode(500); // El servicio deberia devolver un error 500 para entradas no
                                                  // numéricas
        }
}
