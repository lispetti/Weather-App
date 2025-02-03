package fi.tuni.prog3.weatherapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * class for saving favorites and history
 */
public class GlobalVariables {
    private static WeatherAPI weatherAPI = new WeatherAPI();
    private static String currentCity = "";
    private static String currentFavorite = "";
    private static List<String> favorites = new ArrayList<>();
    private static List<String> history = new ArrayList<>();
    

    /**
     * Returns the latest searched city
     * @return string current city
     */
    public static String getCurrentCity(){
        return currentCity;
    }

    /**
     * Changes current
     * @param String new current city
     */
    public static void changeCurrentCity(String city){
        currentCity = city;
    }
    
    /**
     * Returns city that is pressed from favorites/history pop-up. If emty, search button
     * has been pressed
     * @return String city that is pressed from pop-up
     */
    public static String getCurrentFavorite(){
        return currentFavorite;
    }
    
    /**
     * Changes currentFavorite to the city that is pressed from history/favorites pop-up
     * @param String City that is pressed in favorites/history pop-up
     */
    public static void changeCurrentFavorite(String favorite){
        currentFavorite = favorite;
    }

    /**
     * Adds city to list of favorites
     * @param String ciy to be added to favorites
     */
    public static void addFavorite(String favorite) {
        favorites.add(favorite);
    }

    /**
     * Removes city from list of favorites
     * @param String city to be removed
     */
    public static void removeFavorite(String favorite) {
        for(int i = 0; i < favorites.size() ; ++i){
            double[] cityLocation = weatherAPI.lookUpLocation(favorites.get(i));
            double[] favoriteLocation = weatherAPI.lookUpLocation(favorite);
            if(Arrays.equals(cityLocation, favoriteLocation)){
                favorites.remove(i);
            }
        }
    }

    /**
     * checks if city is in favorites
     * @param String city that is checked if it is in favorites
     * @return boolean, true if iscity is in favorites and false if not
     */
    public static boolean isFavorite(String favorite) {
        for(String city : favorites){
            double[] cityLocation = weatherAPI.lookUpLocation(city);
            double[] favoriteLocation = weatherAPI.lookUpLocation(favorite);
            if(Arrays.equals(cityLocation, favoriteLocation)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if city is in history
     * @param String city that is checked if it is in history
     * @return boolean, true if city in history, false if not
     */
    public static boolean isCityInHistory(String newCity){
        Iterator<String> iterator = history.iterator();
        while(iterator.hasNext()){
            String city = iterator.next();
            double[] cityLocation = weatherAPI.lookUpLocation(city);
            double[] newCityLocation = weatherAPI.lookUpLocation(newCity);
            if(Arrays.equals(cityLocation, newCityLocation)){
                iterator.remove();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds city to list of latest searches aka history
     * @param String city to be added to history list
     */
    public static void addCityToHistory(String newCity){
        isCityInHistory(newCity);
        history.add(0, newCity);
        
        if(history.size() > 10){
            history = history.subList(0, 10);
        }
    }
    
    /**
     * Returns list of lates searches
     * @return list of cities in history as strings
     */
    public static List<String> getHistory(){
        return history;
    }
    
    /**
     * Returns the amount of cities in history
     * @return int amount of cities in history
     */
    public static int sizeOfHistory(){
        return history.size();
    }

    /**
     * Returns the amount of cities in favorites
     * @return int amount of cities in favorites
     */
    public static int amountOfFavorites(){
        return favorites.size();
    }
    
    /**
     * Returns the list of favorites as strings
     * @return list of favorites as strings
     */
    public static List<String> getFavorites(){
        return favorites;
    }

    /**
     * Adds favorites from json memory file to list of favorites when program is restarted
     * @param list of favorite cities as strings
     */
    public static void addAllFavorites(List<String> favoritesFromMemory){
        favorites = favoritesFromMemory;
    }

    /**
     * Adds history from json memory file to history list when program is restarted
     * @param list of latest searches as strings
     */
    public static void addWholeHistory(List<String> historyFromMemory){
        history = historyFromMemory;
    }
}
