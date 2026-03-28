package com.example.deliveryfee.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(WeatherApiClient.class)
class WeatherApiClientTest {

    @Autowired
    private WeatherApiClient client;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void fetchWeatherData_returnsXml() {
        String expectedXml = "<observations><station>...</station></observations>";
        mockServer.expect(requestTo("https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php"))
                .andRespond(withSuccess(expectedXml, MediaType.APPLICATION_XML));

        String result = client.fetchWeatherData();
        assertThat(result).isEqualTo(expectedXml);
    }
}