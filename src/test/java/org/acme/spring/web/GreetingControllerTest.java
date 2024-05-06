package org.acme.spring.web;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.acme.spring.web.entity.HelloParam;
import org.acme.spring.web.entity.Title;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;

@QuarkusTest
public class GreetingControllerTest {
    static JsonPath openApiJson;

    @BeforeAll
    static void getOpenApiJson() throws IOException {
        openApiJson = new JsonPath(Files.readString(Path.of("target/definitions/openapi.json")));
    }

    @Test
    public void testHelloEndpoint() {
        given().when().get("/greeting").then().statusCode(200).body("phrase", equalTo("hello ?????"));
        List<String> params = openApiJson.get("paths.'/greeting'.get.parameters");
        assertThat(params.size()).isEqualTo(1);
    }

    @Nested
    class plainDtoDoesNotWork {
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param"})
        void openapiJsonDoesNotContainQueryParamsButRequestBody(String uri) {
            Map<String,?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).doesNotContain("parameters");
            assertThat(get.keySet()).contains("requestBody");
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-at-parameter-object"})
        void withPatchAtParameterObjectDoesNotWorkWhenItsFieldsAreNotAnnotatedWithAtQueryParameter(String uri) {
            Map<String,?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).doesNotContain("parameters");
            assertThat(get.keySet()).doesNotContain("requestBody");
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param", "/echo-param-at-parameter-object"})
        void stating415UnsupportedMediaTypeWhenNoContentTypeIsGiven(String uri) {
            given().when().accept(MediaType.APPLICATION_JSON_VALUE).get(uri).then().statusCode(415);
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param", "/echo-param-at-parameter-object"})
        void doesTreatParamObjectItAsBodyGettingNullWhenNoBodyIsSent(String uri) {
            given().when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .get(uri)
                    .then()
                    .statusCode(204);
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param", "/echo-param-at-parameter-object"})
        void doesTreatParamObjectItAsBodyGettingBodyInsteadOfQueryParams(String uri) {
            var param = new HelloParam();
            param.setTitle(Title.MR);
            param.setName("bean");
            param.setSuffix("whatever");

            given().when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(param)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("bean"))
                    .body("title", equalTo("MR"))
                    .body("suffix", equalTo("whatever"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param", "/echo-param-at-parameter-object"})
        void doesEvenPreferBodyGettingBodyOverQueryParams(String uri) {
            var param = new HelloParam();
            param.setTitle(Title.MR);
            param.setName("bean");
            param.setSuffix("whatever");

            given().when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(param)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("bean"))
                    .body("title", equalTo("MR"))
                    .body("suffix", equalTo("whatever"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param", "/echo-param-at-parameter-object"})
        void doesIgnoreQueryParamsCompletely(String uri) {

            given().when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(204);
        }

    }

    @Nested
    class dtoDoesOnlyWorkWhenCheated {
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-cheated-with-query-param"})
        void despiteHavingWorkingQueryParamsOpenApiClaimsHavingRequestBody(String uri) {
            Map<String,?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).contains("requestBody");
            assertThat(get.keySet()).doesNotContain("parameters");
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-at-parameter-object-cheated-with-query-param"})
        void withPatchAtParameterObjectFinallyWorks(String uri) {
            Map<String,?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).doesNotContain("requestBody");
            assertThat(get.keySet()).contains("parameters");
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-cheated-with-query-param", "/echo-param-at-parameter-object-cheated-with-query-param"})
        void giving200OnEmptyQuery(String uri) {
            given().when().accept(MediaType.APPLICATION_JSON_VALUE).get(uri).then().statusCode(200);
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-cheated-with-query-param", "/echo-param-at-parameter-object-cheated-with-query-param"})
        void doesIgnoreBody(String uri) {
            var param = new HelloParam();
            param.setTitle(Title.MR);
            param.setName("bean");
            param.setSuffix("whatever");

            given().when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(param)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", nullValue())
                    .body("title", nullValue())
                    .body("suffix", nullValue());
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-cheated-with-query-param", "/echo-param-at-parameter-object-cheated-with-query-param"})
        void doesPreferQueryParamsOverBody(String uri) {
            var param = new HelloParam();
            param.setTitle(Title.MR);
            param.setName("bean");
            param.setSuffix("whatever");

            given().when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(param)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("--query-name--"))
                    .body("title", equalTo("SIGNORA"))
                    .body("suffix", equalTo("--query-suffix--"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-cheated-with-query-param", "/echo-param-at-parameter-object-cheated-with-query-param"})
        void doesUseQueryParams(String uri) {
            given().when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("--query-name--"))
                    .body("title", equalTo("SIGNORA"))
                    .body("suffix", equalTo("--query-suffix--"));
        }

    }

    @Nested
    class cheatedDtoNeedsAtValidAnnotationForValidationAndAtBeanParamForOpenApi {
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-cheated-with-query-param-with-validations"})
        void despiteHavingWorkingQueryParamsAndAtBeanParamOpenApiClaimsHavingRequestBody(String uri) {
            Map<String,?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).contains("requestBody");
            assertThat(get.keySet()).doesNotContain("parameters");
        }
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-at-parameter-object-cheated-with-query-param-with-validations"})
        void withPatchAtParameterObjectFinallyWorks(String uri) {
            Map<String,?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).doesNotContain("requestBody");
            assertThat(get.keySet()).contains("parameters");
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-cheated-with-query-param-with-validations",
                "/echo-param-at-parameter-object-cheated-with-query-param-with-validations"})
        void giving400OnEmtptyQuery(String uri) {
            given().when().accept(MediaType.APPLICATION_JSON_VALUE).get(uri).then().statusCode(400).body("violations", hasSize(3));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-cheated-with-query-param-with-validations",
                "/echo-param-at-parameter-object-cheated-with-query-param-with-validations"})
        void doesUseQueryParams(String uri) {
            given().when()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("--query-name--"))
                    .body("title", equalTo("SIGNORA"))
                    .body("suffix", equalTo("--query-suffix--"));
        }

    }

}
