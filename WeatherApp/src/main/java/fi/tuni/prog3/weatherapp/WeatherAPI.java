package fi.tuni.prog3.weatherapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.TreeMap;

import com.google.gson.JsonArray;



/**
 * class for extracting data from the OpenWeatherMap API.
 */
public class WeatherAPI implements iAPI {

    private static final String API_KEY = "b8238bf2e5d810aa939ec3ac98ffb8ea";
   
    /**
     * Fetches a JSON file from a given URL and returns it as a StringBuilder.
     * @param urlString url of website from where data is fetched
     * @return A StringBuilder containing the JSON file data
     * 
     */
    public StringBuilder jsonFile(String urlString) {
        try{
            URL url = new URL(urlString);

            // Open connection and fetch data
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();
           
            return response;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
   
   /**
     * Returns coordinates for a location.
     * @param loc Name of the location for which coordinates should be fetched.
     * @return double[].
     */
    public double[] lookUpLocation(String loc) {
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q="
                    + URLEncoder.encode(loc, "UTF-8") + "&appid=" + API_KEY + "&units=metric";

            //check if place exists
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return new double[]{1000, 1000};
            }

            StringBuilder response = jsonFile(urlString);
            
            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

            JsonObject coord = jsonResponse.getAsJsonObject("coord");
            double lon = coord.get("lon").getAsDouble();
            double lat = coord.get("lat").getAsDouble();

            // Adding city as current city
            GlobalVariables.changeCurrentCity(WeatherApp.capitalizeLocation(loc));

            return new double[]{lat, lon};

        } catch (IOException e) {
            return new double[]{1000, 1000};
        }
    }

    /**
     * Returns the current weather for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return list of strings containing weather info: weather description
     *         temperature and wind speed
     */
    @Override
    public List<String> getCurrentWeather(double lat, double lon) {

        List<String> weatherInfo = new ArrayList<>();
        String urlString = "https://api.openweathermap.org/data/2.5/weather?lat="
                + lat + "&lon=" + lon + "&appid=" + API_KEY;

        StringBuilder response = jsonFile(urlString);
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

        // Extract weather description
        JsonArray weatherArray = jsonResponse.getAsJsonArray("weather");
        JsonObject weatherObject = weatherArray.get(0).getAsJsonObject();
        String description = weatherObject.get("main").getAsString();
        weatherInfo.add(description);

        // Extract temperature in Kelvin and convert to Celsius
        double temperatureKelvin = jsonResponse.getAsJsonObject("main").get("temp").getAsDouble();
        double temperatureCelsius = temperatureKelvin - 273.15; // Conversion from Kelvin to Celsius
        long roundedTemp = Math.round(temperatureCelsius);
        String formattedTemperature = String.valueOf(roundedTemp);
        weatherInfo.add(formattedTemperature);

        // Extract wind speed
        double windSpeed = jsonResponse.getAsJsonObject("wind").get("speed").getAsDouble();
        long roundedWind = Math.round(windSpeed);
        String windSpeedString = String.valueOf(roundedWind); // Convert long to string
        weatherInfo.add(windSpeedString);
       
        return weatherInfo;
    }


    /**
     * Returns a forecast for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return map, key is date and time as string and value is list of strings: temp, 
     *         weather description, rain amount.
     */
    @Override
    public TreeMap<String, List<String>> getHourlyForecast(double lat, double lon) {
        String urlString = "https://pro.openweathermap.org/data/2.5/forecast/hourly?lat=" + lat + "&lon="
                + lon + "&appid=" + API_KEY + "&units=metric";

        StringBuilder response = jsonFile(urlString);
        // parse JSON response
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray list = jsonResponse.getAsJsonArray("list");
        TreeMap<String, List<String>> resultMap = new TreeMap<>();

        for (int i = 0; i < 12; ++i) {
            JsonObject listItem = list.get(i).getAsJsonObject();
           
            // extracting date and time
            String dtTxt = listItem.get("dt_txt").getAsString();
            LocalDateTime dateTime = LocalDateTime.parse(dtTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ZonedDateTime utcDateTime = ZonedDateTime.of(dateTime, ZoneId.of("UTC"));
            ZonedDateTime utcPlus3DateTime = utcDateTime.withZoneSameInstant(ZoneId.of("UTC+3"));
            String date = String.valueOf(utcPlus3DateTime.getDayOfMonth()); // Day part 
            String hour = String.format("%02d", utcPlus3DateTime.getHour()); // Hour part
            String minute = String.format("%02d", utcPlus3DateTime.getMinute()); // Minute part

            // Combining date, hour, and minute with colon separators
            String time = date + ":" + hour + ":" + minute;

            // extracting temperature
            JsonObject mainObject = listItem.getAsJsonObject("main");
            double temp = mainObject.get("temp").getAsDouble();
            long roundedTemp = Math.round(temp);

            // extracting weather
            JsonArray weatherArray = listItem.getAsJsonArray("weather");
            JsonObject weatherObject = weatherArray.get(0).getAsJsonObject();
            String mainWeather = weatherObject.get("main").getAsString();
           
            // Extract snow or rain data
            double rainAmount = 0.0;

            if (listItem.has("snow") && listItem.getAsJsonObject("snow").has("1h")) {
                rainAmount = listItem.getAsJsonObject("snow").get("1h").getAsDouble();
            }

            if (listItem.has("rain") && listItem.getAsJsonObject("rain").has("1h")) {
                rainAmount = listItem.getAsJsonObject("rain").get("1h").getAsDouble();
            }
           
            if (!resultMap.containsKey(time)) {
                resultMap.put(time, new ArrayList<>());
            }
            resultMap.get(time).add(String.valueOf(roundedTemp));
            resultMap.get(time).add(mainWeather);
            resultMap.get(time).add(String.valueOf(rainAmount));
        }
        return resultMap;
    }

    /**
     * Returns a daily forecast for the given coordinates.
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return map, key is date as string and value is list of strings: current temp, 
     *         min temp, max temp, weather description.
     */
    @Override
    public TreeMap<String, List<String>> getDailyForecast(double lat, double lon) {
        String urlString = "https://api.openweathermap.org/data/2.5/forecast/daily?lat=" + lat + "&lon=" + lon
                + "&cnt=16&appid=" + API_KEY + "&units=metric";


        // Parse the JSON response
        StringBuilder response = jsonFile(urlString);
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray list = jsonResponse.getAsJsonArray("list");
        TreeMap<String, List<String>> resultMap = new TreeMap<>();

        // Loop through the forecast data
        int count = 0;
        for (JsonElement element : list) {
            if (count >= 7)
                break;

            JsonObject item = element.getAsJsonObject();
            // Extract date
            String dtTxt = item.get("dt").getAsString();
            String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH").format(new java.util.Date(Long.parseLong(dtTxt) * 1000));

            // Extract temperature
            JsonObject tempObject = item.getAsJsonObject("temp");
            double tempMin = tempObject.get("min").getAsDouble();
            long roundedTempMin = Math.round(tempMin);
            double tempMax = tempObject.get("max").getAsDouble();
            long roundedTempMax = Math.round(tempMax);

            // Extract weather
            JsonArray weatherArray = item.getAsJsonArray("weather");
            JsonObject weatherObject = weatherArray.get(0).getAsJsonObject();
            String mainWeather = weatherObject.get("main").getAsString();

 

            // Store the forecast data
            List<String> forecastData = new ArrayList<>();
            forecastData.add(String.valueOf(roundedTempMin));
            forecastData.add(String.valueOf(roundedTempMax));
            forecastData.add(mainWeather);

            resultMap.put(date, forecastData);

            count++;
        }
        return resultMap;
    }
}