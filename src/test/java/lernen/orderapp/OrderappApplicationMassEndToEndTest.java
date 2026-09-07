package lernen.orderapp;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.jupiter.json.JsonClasspathSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Datengetriebener Schnittstellentest: Jeder Testfall wird als HTTP-Request/Response-Paar
 * in {@code orders-endpoint-testcases.json} beschrieben. Geprüft wird ausschließlich das
 * über die REST-API sichtbare Endergebnis (Status-Code, Response-Body) – kein internes Mocking.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OrderappApplicationMassEndToEndTest {

    @Autowired
    final TestRestTemplate restTemplate;

    @Autowired
    OrderappApplicationMassEndToEndTest(final TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    record EndpointCase(String name, String method, String url, String expectedStatus, JsonNode expectedBody) {
        @Override
        public String toString() {
            return name;
        }
    }

    @BeforeAll
    void importTestData() {
        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test-bestellungen.csv"));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/batch-jobs/order-import", requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @ParameterizedTest(name = "{0}")
    @JsonClasspathSource("orders-endpoint-testcases.json")
    void schnittstelleLiefertErwartetesErgebnis(final EndpointCase testCase) throws Exception {
        assertThat(testCase.method()).isEqualToIgnoringCase("GET");

        final ResponseEntity<String> response = restTemplate.getForEntity(testCase.url(), String.class);
        log.info("{}: response = {}", testCase.name(), response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(testCase.expectedStatus()));

        if (testCase.expectedBody() != null && !testCase.expectedBody().isNull()) {
            JSONAssert.assertEquals(testCase.expectedBody().toString(), response.getBody(), JSONCompareMode.LENIENT);
        }
    }

}
