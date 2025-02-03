package fi.tuni.prog3.weatherapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for writing to and reading from JSON files.
 */
public class Memory {
    private static final String FILENAME = "userData.json";
    private static String currentCity = "";
    private static List<String> favorites = new ArrayList<>();
    private static List<String> history = new ArrayList<>();

    /**
     * Changes the current city.
     */
    public static void changeCurrentCity() {
        currentCity = GlobalVariables.getCurrentCity();
    }

    /**
     * Adds favorite cities to the list of favorites.
     */
    public static void saveFavorites() {
        favorites = GlobalVariables.getFavorites();
    }

    /**
     * Adds last searched cities to the list of history.
     */
    public static void saveHistory() {
        history = GlobalVariables.getHistory();
    }

    /**
     * Saves current city, favorite cities and latest searches to the JSON file.
     */
    public static void saveToJson() {
        // Create a data structure to hold the information
        Data data = new Data(currentCity, favorites, history);

        // Convert data to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);

        // Write to JSON
        try (FileWriter writer = new FileWriter(FILENAME)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads latest city, list of favorite cities and list of latest searches from JSON 
     * file to Memory object.
     */
    public static void loadFromJson() {
        try (FileReader reader = new FileReader(FILENAME)) {
            // Parse JSON data back into Data object
            Gson gson = new Gson();
            Data data = gson.fromJson(reader, Data.class);

            // Update currentCity, favorites, and history with loaded data
            if (data != null) {
                if (!data.currentCity.isEmpty()) {
                    GlobalVariables.changeCurrentCity(data.currentCity);
                }
                if (data.favorites != null) {
                    GlobalVariables.addAllFavorites(data.favorites);
                }
                if (data.history != null) {
                    GlobalVariables.addWholeHistory(data.history);
                }
            }

        } catch (IOException e) {}
    }

    /**
     * Class for holding data that is written to and read from JSON file.
     */
    private static class Data {
        private String currentCity;
        private List<String> favorites;
        private List<String> history;

        /**
         * Constructs a new Data object with current city and list of favorite cities.
         *
         * @param currentCity The current city to be stored in the Data object.
         * @param favorites   The list of favorite cities to be stored in the Data object.
         * @param history     The list of last searched cities to be stored in the Data object.
         */
        public Data(String currentCity, List<String> favorites, List<String> history) {
            this.currentCity = currentCity;
            this.favorites = favorites;
            this.history = history;
        }
    }
}
