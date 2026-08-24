package lernen.orderapp;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OrderappApplicationEndToEndTests {

    @Autowired
    final TestRestTemplate restTemplate;
    @Autowired
    OrderappApplicationEndToEndTests( final TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    ResponseEntity<String> prepareDB(){
        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test-bestellungen.csv"));
        final  HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final  HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(
                "/api/batch-jobs/order-import", requestEntity, String.class);
    }

    @Test
    void postOrderImportTest()  {
        final ResponseEntity<String> response = prepareDB();

        log.info("response = {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(String.class);
        assertThat(response.getBody()).isEqualTo("1");

    }
    @Test
    void getOrderImportwithStatusTest()  {
        final ResponseEntity<String> response = prepareDB();


        final ResponseEntity<Map>  response2 = restTemplate.getForEntity(
                "/api/batch-jobs/order-import/{executionId}", Map.class,1);


        final Map<String,Object> expected = Map.of(

                "exitCode", "COMPLETED",
                "exitDescription", "",
                //"exitException", null,
                "running", false
        );
        assertThat(response2.getBody()).containsAllEntriesOf(expected);

        log.info("response = {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(String.class);

    }
    @Test
    void getOrdersTest()  {
        final ResponseEntity<String> _ = prepareDB();

        final ResponseEntity<List>  response = restTemplate.getForEntity(
                "/api/orders?customerId={customerId}&channel={channel}&dateFrom={dateFrom}&dateTo={dateTo}&sorting={sorting}", List.class,
                "C-1001", "ONLINE", "2026-01-01", "2026-12-31", true
                );
        log.info("response = {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final List<Map<String, Object>> orders = response.getBody();
        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(o -> o.get("orderId"))
                .containsExactlyInAnyOrder("ORD-3011", "ORD-3026");
        assertThat(orders).allSatisfy(o -> assertThat(o).containsEntry("channel", "ONLINE"));
    }
    @Test
    void getOrdersNotFoundTest()  {
        final ResponseEntity<String> _ = prepareDB();

        final ResponseEntity<Exception>  response = restTemplate.getForEntity(
                "/api/orders?customerId={customerId}&channel={channel}&dateFrom={dateFrom}&dateTo={dateTo}&sorting={sorting}", Exception.class,
                "xy", "ONLINE", "2026-01-01", "2026-12-31", true
        );
        log.info("response = {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getStatisticsTest()  {
        final ResponseEntity<String> _ = prepareDB();

        final ResponseEntity<Map>  response = restTemplate.getForEntity(
                "/api/customers/{customer_id}/statistics", Map.class,"C-1001");
        log.info("response = {}", response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final Map<String, Object>  expected=Map.of( "Total Earnings",752.2, "number of Orders",5);
        assertThat(response.getBody()).containsAllEntriesOf(expected);
    }
    @Test
    void getTopCustomersTest()  {
        final ResponseEntity<String> _ = prepareDB();

        final ResponseEntity<List> response = restTemplate.getForEntity(
                "/api/statistics/top-customers?limit={limit}&dateFrom={dateFrom}&dateTo={dateTo}",
                List.class,
                5, "1900-01-01", "2026-12-31");
        log.info("response = {}", response);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final  List<String> expected= List.of( "Clara Voss",
                "Bernd Klein",
                "Anna Berger",// Hier Problem mit den Doppelten Buchungen OrderId ist gleich
                "Erika Sommer",
                "Dieter Wolf");
        assertThat(response.getBody()).containsExactlyElementsOf( expected);

    }

}
