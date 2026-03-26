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

            NodeList stationNodes = doc.getElementsByTagName("station");
            for (int i = 0; i < stationNodes.getLength(); i++) {
                Element stationElem = (Element) stationNodes.item(i);

                String name = getElementText(stationElem, "name");
                if (!STATION_TO_CITY.containsKey(name)) {
                    continue; // skip stations not in our list
                }
                City city = STATION_TO_CITY.get(name);

                String wmoCode = getElementText(stationElem, "wmocode");
                String airTempStr = getElementText(stationElem, "airtemperature");
                Double airTemperature = airTempStr != null ? Double.parseDouble(airTempStr) : null;
                String windSpeedStr = getElementText(stationElem, "windspeed");
                Double windSpeed = windSpeedStr != null ? Double.parseDouble(windSpeedStr) : null;
                String weatherPhenomenon = getElementText(stationElem, "phenomenon");
                String timestampStr = getElementText(stationElem, "timestamp");
                LocalDateTime observationTimestamp = timestampStr != null ? LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;

                WeatherObservation obs = new WeatherObservation();
                obs.setStationName(name);
                obs.setWmoCode(wmoCode);
                obs.setAirTemperature(airTemperature);
                obs.setWindSpeed(windSpeed);
                obs.setWeatherPhenomenon(weatherPhenomenon);
                obs.setObservationTimestamp(observationTimestamp);
                obs.setCity(city);

                observations.add(obs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML weather data", e);
        }
        return observations;
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