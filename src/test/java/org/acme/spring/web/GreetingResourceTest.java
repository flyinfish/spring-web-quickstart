package org.acme.spring.web;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.acme.spring.web.entity.HelloParam;
import org.acme.spring.web.entity.Title;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;

@QuarkusTest
@TestHTTPEndpoint(GreetingResource.class)
public class GreetingResourceTest {
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
    @TestHTTPEndpoint(GreetingResource.class)
    class plainDtoDoesNotWork {
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param"})
        void openapiJsonDoesNotContainQueryParamsButRequestBody(String uri) {
            Map<String, ?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).doesNotContain("parameters");
            assertThat(get.keySet()).contains("requestBody");
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param"})
        void stating415UnsupportedMediaTypeWhenNoContentTypeIsGiven(String uri) {
            given().when().accept("application/json").get(uri).then().statusCode(415);
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param"})
        void doesTreatParamObjectItAsBodyGettingNullWhenNoBodyIsSent(String uri) {
            given().when().contentType("application/json").accept("application/json").get(uri).then().statusCode(204);
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param"})
        void doesTreatParamObjectItAsBodyGettingBodyInsteadOfQueryParams(String uri) {
            var param = new HelloParam();
            param.setTitle(Title.MR);
            param.setName("bean");
            param.setSuffix("whatever");

            given().when()
                    .contentType("application/json")
                    .body(param)
                    .accept("application/json")
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", CoreMatchers.equalTo("bean"))
                    .body("title", CoreMatchers.equalTo("MR"))
                    .body("suffix", CoreMatchers.equalTo("whatever"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param"})
        void doesEvenPreferBodyGettingBodyOverQueryParams(String uri) {
            var param = new HelloParam();
            param.setTitle(Title.MR);
            param.setName("bean");
            param.setSuffix("whatever");

            given().when()
                    .contentType("application/json")
                    .body(param)
                    .accept("application/json")
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", CoreMatchers.equalTo("bean"))
                    .body("title", CoreMatchers.equalTo("MR"))
                    .body("suffix", CoreMatchers.equalTo("whatever"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param"})
        void doesIgnoreQueryParamsCompletely(String uri) {

            given().when()
                    .contentType("application/json")
                    .accept("application/json")
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(204);
        }

    }

    @Nested
    @TestHTTPEndpoint(GreetingResource.class)
    class queryParamsAsBeanDoNotWorkWhenNotAnnotatedWithAtBeanParam {
        @ParameterizedTest
        @ValueSource(strings = {"/jakartars/echo-param-with-query-param"})
        void despiteHavingWorkingQueryParamsOpenApiClaimsHavingRequestBody(String uri) {
            Map<String, ?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).contains("requestBody");
            assertThat(get.keySet()).doesNotContain("parameters");
        }

        @Disabled("does not work as in pure jaxrs-app!!!")
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-with-query-param"})
        void giving415OnEmptyQueryAndEmptyBody(String uri) {
            given().when().accept("application/json").get(uri).then().statusCode(415);
        }

        @Disabled("does not work as in pure jaxrs-app!!!")
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-with-query-param"})
        void doesInterpretBody(String uri) {
            var param = new HelloParam();
            param.setTitle(Title.MR);
            param.setName("bean");
            param.setSuffix("whatever");

            given().when()
                    .contentType("application/json")
                    .body(param)
                    .accept("application/json")
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("bean"))
                    .body("title", equalTo("MR"))
                    .body("suffix", equalTo("whatever"));
        }

        @Disabled("does not work as in pure jaxrs-app!!!")
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-with-query-param"})
        void doesPreferBodyOverQueryParams(String uri) {
            var param = new HelloParam();
            param.setTitle(Title.MR);
            param.setName("bean");
            param.setSuffix("whatever");

            given().when()
                    .contentType("application/json")
                    .body(param)
                    .accept("application/json")
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

        @Disabled("does not work as in pure jaxrs-app!!!")
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-with-query-param"})
        void doesNotUseQueryParams(String uri) {
            given().when()
                    .contentType("application/json")
                    .accept("application/json")
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(204);
        }

    }

    @Nested
    @TestHTTPEndpoint(GreetingResource.class)
    class cheatedDtoNeedsAtValidAnnotationForValidationAndAtBeanParamForOpenApi {
        @ParameterizedTest
        @ValueSource(strings = {"/jakartars/echo-param-with-query-param-with-validations"})
        void despiteHavingWorkingQueryParamsAndAtBeanParamOpenApiClaimsHavingRequestBody(String uri) {
            Map<String, ?> get = openApiJson.get("paths.'%s'.get".formatted(uri));
            assertThat(get.keySet()).doesNotContain("requestBody");
            assertThat(get.keySet()).contains("parameters");
        }

        @Disabled("does not work as in pure jaxrs-app!!! -- pure gives 'violations' instead 'parameterViolations', as gluon-error-handling ist different anyway this might work for us")
        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-with-query-param-with-validations"})
        void giving400OnEmtptyQuery(String uri) {
            given().when().accept("application/json").get(uri).then().statusCode(400).body("parameterViolations", hasSize(3));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/echo-param-with-query-param-with-validations"})
        void doesUseQueryParams(String uri) {
            given().when()
                    .contentType("application/json")
                    .accept("application/json")
                    .queryParam("title", Title.SIGNORA)
                    .queryParam("name", "--query-name--")
                    .queryParam("suffix", "--query-suffix--")
                    .get(uri)
                    .then()
                    .statusCode(200)
                    .body("name", CoreMatchers.equalTo("--query-name--"))
                    .body("title", CoreMatchers.equalTo("SIGNORA"))
                    .body("suffix", CoreMatchers.equalTo("--query-suffix--"));
        }

    }

}