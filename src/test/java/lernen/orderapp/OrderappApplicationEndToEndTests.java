package lernen.orderapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OrderappApplicationEndToEndTests {

    @Autowired
    final TestRestTemplate restTemplate;
    @Autowired
    OrderappApplicationEndToEndTests( TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Test
    void postOrderImportTest()  {

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test-bestellungen.csv"));
        final  HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final  HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/batch-jobs/order-import", requestEntity, String.class);

        System.out.println("response = " + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(String.class);
        assertThat(response.getBody()).isEqualTo("1");

    }
    @Test
    void getOrderImportwithStatusTest()  {
        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test-bestellungen.csv"));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final ResponseEntity<String>  response = restTemplate.postForEntity(
                "/api/batch-jobs/order-import", requestEntity, String.class);

        final ResponseEntity<Map>  response2 = restTemplate.getForEntity(
                "/api/batch-jobs/order-import/{executionId}", Map.class,1);


        final var expected = Map.of(

            "exitCode", "COMPLETED",
                "exitDescription", "",
                //"exitException", null,
                "running", false
        );
        assertThat(response2.getBody()).containsAllEntriesOf(expected);

        System.out.println("response = " + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(String.class);

    }
    @Test
    void getOrdersTest()  {
        final var  response = restTemplate.getForEntity(
                "/api/orders?customerId={customerId}&channel={channel}&dateFrom={dateFrom}&dateTo={dateTo}&sorting={sorting}", List.class,
                "C-1001", "ONLINE", "2026-01-01", "2026-12-31", true
                );
        System.out.println("response = " + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final List<Map<String, Object>> orders = response.getBody();
        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(o -> o.get("orderId"))
                .containsExactlyInAnyOrder("ORD-2001", "ORD-2016");
        assertThat(orders).allSatisfy(o -> assertThat(o.get("channel")).isEqualTo("ONLINE"));
    }
    @Test
    void getStatisticsTest()  {
        final var  response = restTemplate.getForEntity(
                "/api/customers/{customer_id}/statistics", Map.class,"C-1001");
        System.out.println("response = " + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final var  expected=Map.of( "Total Earnings",752.2, "number of Orders",5);
        assertThat(response.getBody()).containsAllEntriesOf(expected);
    }
    @Test
    void getTopCustomersTest()  {
        final ResponseEntity<List> response = restTemplate.getForEntity(
                "/api/statistics/top-customers?limit={limit}&dateFrom={dateFrom}&dateTo={dateTo}",
                List.class,
                5, "1900-01-01", "2026-12-31");
        System.out.println("response = " + response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final  List<String> expected= List.of( "Clara Voss",
                "Bernd Klein",
                "Anna Berger",// Hier Problem mit den Doppelten Buchungen OrderId ist gleich
                "Erika Sommer",
                "Dieter Wolf");
        assertThat(response.getBody()).containsExactlyElementsOf( expected);

    }

    @Test
    void contextLoads() {
    }

}
