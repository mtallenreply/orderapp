package lernen.orderapp.service;


import lernen.orderapp.entity.Channel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
@Slf4j
@SpringBootTest(webEnvironment=RANDOM_PORT)
@AutoConfigureTestRestTemplate
class StatisticsAggregatorIntTest {


    @Autowired
    private StatisticsAggregator statisticsAggregatorUnderTest;
    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("test-bestellungen.csv"));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final ResponseEntity<String> _ = restTemplate.postForEntity(
                "/api/batch-jobs/order-import", requestEntity, String.class);
    }

    @Test
    void testCalcNumberOfOrdersPerChannel() {
        final Map<Channel, Long> result = statisticsAggregatorUnderTest.calcNumberOfOrdersPerChannel();
        log.info("result = {}", result);

        assertThat(result).isEqualTo(Map.of(
                Channel.PARTNER, 7L,
                Channel.ONLINE, 9L,
                Channel.RETAIL, 8L
        ));

    }
    @Test
    void testCalcTotalEarnings() {
        final BigDecimal result = statisticsAggregatorUnderTest.calcTotalEarnings();
        log.info("result = {}", result);
        assertThat(result).isEqualTo(new BigDecimal("4693.52"));
    }
    @Test
    void testCalcTop() {
        final List<String> result = statisticsAggregatorUnderTest.calcTop(
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2026-12-31"),5L);
        log.info("result = {}", result);
        // Erst der erste Lauf des ImportJobs verbindet namen mit nummern
//        assertThat(result).containsExactly("C-1003", "C-1002", "C-1001", "C-1005", "C-1004");
        assertThat(result).containsExactly("Clara Voss", "Bernd Klein", "Anna Berger", "Erika Sommer", "Dieter Wolf");
    }

}
