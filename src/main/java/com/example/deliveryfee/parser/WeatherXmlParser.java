package com.example.deliveryfee.parser;

import com.example.deliveryfee.entity.WeatherObservation;
import com.example.deliveryfee.enums.City;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WeatherXmlParser {

    private static final Map<String, City> STATION_TO_CITY = Map.of(
            "Tallinn-Harku", City.TALLINN,
            "Tartu-Tõravere", City.TARTU,
            "Pärnu", City.PARNU
    );

    public List<WeatherObservation> parse(String xml) {
        List<WeatherObservation> observations = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            Element root = doc.getDocumentElement();
            String timestampAttr = root.getAttribute("timestamp");

            LocalDateTime observationTimestamp = null;
            if (!timestampAttr.isBlank()) {
                observationTimestamp = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(Long.parseLong(timestampAttr)),
                        java.time.ZoneId.systemDefault()
                );
            }

            NodeList stationNodes = doc.getElementsByTagName("station");
            for (int i = 0; i < stationNodes.getLength(); i++) {
                Element stationElem = (Element) stationNodes.item(i);

                String name = getElementText(stationElem, "name");
                if (!STATION_TO_CITY.containsKey(name)) {
                    continue;
                }

                WeatherObservation obs = new WeatherObservation();
                obs.setStationName(name);
                obs.setCity(STATION_TO_CITY.get(name));
                obs.setWmoCode(getElementText(stationElem, "wmocode"));
                obs.setAirTemperature(parseDouble(getElementText(stationElem, "airtemperature")));
                obs.setWindSpeed(parseDouble(getElementText(stationElem, "windspeed")));
                obs.setWeatherPhenomenon(getElementText(stationElem, "phenomenon"));
                obs.setObservationTimestamp(observationTimestamp);
                observations.add(obs);
            }
            return observations;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML weather data", e);
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value);
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Element element = (Element) nodeList.item(0);
            return element.getTextContent();
        }
        return null;
    }
}