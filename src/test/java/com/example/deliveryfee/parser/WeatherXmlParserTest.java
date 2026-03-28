package com.example.deliveryfee.parser;

import com.example.deliveryfee.entity.WeatherObservation;
import com.example.deliveryfee.enums.City;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WeatherXmlParserTest {

    @Autowired
    private WeatherXmlParser parser;

    @Test
    void parse_shouldReturnObservationsForTargetStations() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<observations>" +
                "<station><name>Tallinn-Harku</name><wmocode>26038</wmocode><airtemperature>5.0</airtemperature><windspeed>3.5</windspeed><phenomenon>Clear</phenomenon><timestamp>2026-03-28T12:00:00</timestamp></station>" +
                "<station><name>Tartu-Tõravere</name><wmocode>26242</wmocode><airtemperature>2.0</airtemperature><windspeed>2.0</windspeed><phenomenon>Rain</phenomenon><timestamp>2026-03-28T12:00:00</timestamp></station>" +
                "<station><name>Pärnu</name><wmocode>41803</wmocode><airtemperature>0.5</airtemperature><windspeed>4.0</windspeed><phenomenon>Snow</phenomenon><timestamp>2026-03-28T12:00:00</timestamp></station>" +
                "</observations>";

        List<WeatherObservation> observations = parser.parse(xml);
        assertThat(observations).hasSize(3);
        // Verify each mapping
        WeatherObservation tallinn = observations.stream().filter(o -> o.getCity() == City.TALLINN).findFirst().get();
        assertThat(tallinn.getAirTemperature()).isEqualTo(5.0);
        assertThat(tallinn.getWindSpeed()).isEqualTo(3.5);
        assertThat(tallinn.getWeatherPhenomenon()).isEqualTo("Clear");
        // ...
    }

    @Test
    void parse_skipsUnwantedStations() {
        String xml = "<observations><station><name>SomeOtherStation</name><wmocode>123</wmocode><airtemperature>1</airtemperature></station></observations>";
        List<WeatherObservation> observations = parser.parse(xml);
        assertThat(observations).isEmpty();
    }
}