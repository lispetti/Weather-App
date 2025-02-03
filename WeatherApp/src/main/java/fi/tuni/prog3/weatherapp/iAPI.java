package fi.tuni.prog3.weatherapp;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
/**
 * Interface for extracting data from the OpenWeatherMap API.
 */
public interface iAPI {

    /**
     * Returns coordinates for a location.
     * @param loc Name of the location for which coordinates should be fetched.
     * @return double.
     */
    public double[] lookUpLocation(String loc) ;

    /**
     * Returns the current weather for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return list of strings containing weather info: weather description
     *         temperature and wind speed
     */
    public List<String> getCurrentWeather(double lat, double lon);

    /**
     * Returns a forecast for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return map, key is date and time as string and value is list of strings: temp, 
     *         weather description, raina amount.
     */
    public TreeMap<String, List<String>> getHourlyForecast(double lat, double lon);
   
    /**
     * Returns a daily forecast for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return map, key is date as string and value is list of strings: current temp, 
     *         min temp, max temp, weather description, iconID.
     */
    public TreeMap<String, List<String>> getDailyForecast(double lat, double lon);

   
}