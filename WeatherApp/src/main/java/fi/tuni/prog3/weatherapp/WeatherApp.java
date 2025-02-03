package fi.tuni.prog3.weatherapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.TreeMap;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;

/**
 * The WeatherApp class represents a JavaFX application for displaying weather
 * information.
 */
public class WeatherApp extends Application {

    // The stage of the previous window
    private Stage previousStage;

    // Text field for entering location
    private TextField locationTextField;

    // Weather API instance for fetching weather data
    private WeatherAPI weatherAPI = new WeatherAPI();

    // Hourly forecast data stored as map with timestamps
    private Map<String, List<String>> hourlyForecast = new TreeMap<>();

    // Daily forecast data stored as map with dates
    private Map<String, List<String>> dailyForecast = new TreeMap<>();

    // Current weather data
    private List<String> currentWeather = new ArrayList<>();

    // Button for adding to favorites
    private Button favoriteButton;

    // Button for displaying favorite locations
    private Button favoriteListButton;

    // Button for searching weather for a location
    private Button searchButton;

    // Button for viewing search history
    private Button historyButton;

    // Button for quitting the application
    private Button quitButton;

    // Root pane of the application scene
    private BorderPane root;

    /**
     * Starts the JavaFX application.
     *
     * @param stage The primary stage for this application, onto which the
     * application scene is set.
     */
    @Override
    public void start(Stage stage) {
        // Store the current stage as previousStage
        this.previousStage = stage;

        // Load data from JSON storage
        Memory.loadFromJson();

        // Create the root BorderPane
        root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setStyle("-fx-background-color: #f0f0f0;");

        // Set background image
        String imagePath = "/images/background.jpg";
        Image backgroundImage = new Image(getClass().getResourceAsStream(imagePath));
        ImageView backgroundImageView = new ImageView(backgroundImage);
        root.getChildren().add(backgroundImageView);

        // Add HBox to the center of the BorderPane
        root.setCenter(getEveryBox());
        favoriteButton.setDisable(true);

        // Display weather for the current city if available
        if (GlobalVariables.getCurrentCity() != "") {
            displayWeatherForCurrentCity();
        }
       
        Scene scene = new Scene(root, 600, 750);
        stage.setScene(scene);
        stage.setTitle("WeatherApp");
        //stage.setResizable(false);
        stage.show();

        // Save data on application close
        stage.setOnCloseRequest(event -> {
            Memory.changeCurrentCity();
            Memory.saveFavorites();
            Memory.saveHistory();
            Memory.saveToJson();
        });
    }

    /**
     * Main method to launch the application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Creates a VBox to hold various sections of the application.
     *
     * @return VBox containing the sections.
     */
    private VBox getEveryBox() {
        // Create a VBox to hold sections
        VBox centerVBox = new VBox(10);

        // Create HBoxes for different sections
        HBox topPanelHBox = getTopPanelHBox();
        VBox currentWeatherVBox = getCurrentWeatherVBox();
        HBox hourlyForecastHBox = getHourlyForecastHBox();
        VBox dailyForecastVBox = getDailyForecastVBox();
        HBox rainDataHBox = getRainDataHBox();

        // Add sections to the VBox
        centerVBox.getChildren().addAll(topPanelHBox, currentWeatherVBox, hourlyForecastHBox, rainDataHBox, dailyForecastVBox);

        return centerVBox;
    }

    /**
     * Creates the top panel HBox containing location input and control buttons.
     *
     * @return HBox for the top panel.
     */
    private HBox getTopPanelHBox() {
        // Create a HBox for the top panel
        HBox topPanelHBox = new HBox(10);
        topPanelHBox.setPrefHeight(10);
        topPanelHBox.setStyle("-fx-background-color: rgba(220, 220, 220, 0.7); -fx-padding: 10;");

        // Create label and text field for location
        Label locationLabel = new Label("Location:");
        locationLabel.setStyle("-fx-font-size: 13px;");
        locationTextField = new TextField();

        // Create favorite, search, favorites list, history, and quit buttons
        favoriteButton = createFavoriteButton();
        searchButton = createSearchButton();
        favoriteListButton = createFavoriteListButton();
        historyButton = createHistoryButton();
        quitButton = getQuitButton();

        // Create label for temperature display
        Label temperatureLabel = new Label();

        // Add components to the HBox
        topPanelHBox.getChildren().addAll(locationLabel, locationTextField, searchButton,
                favoriteButton, favoriteListButton, historyButton, quitButton, temperatureLabel);

        return topPanelHBox;
    }

    /**
     * Creates a VBox to display current weather information.
     *
     * @return VBox displaying current weather.
     */
    private VBox getCurrentWeatherVBox() {
        // Create VBox for upper middle panel
        VBox currentWeatherVBox = new VBox();
        currentWeatherVBox.setPrefHeight(200);
        currentWeatherVBox.setStyle("-fx-background-color: rgba(220, 220, 220, 0.7); -fx-padding: 10;");

        if (currentWeather.isEmpty() || hourlyForecast.isEmpty()) {
            // If no forecast available, display a default message
            Label label = new Label("No forecast available");
            label.setStyle("-fx-font-size: 12px;"); // Adjust font size if needed
            currentWeatherVBox.getChildren().add(label);
        } else {
            // Update background
            ImageView backgroundImageView = getBackground(currentWeather.get(0));
            root.getChildren().add(backgroundImageView);

            // Create location box
            String currentCity = GlobalVariables.getCurrentCity();
            currentCity = currentCity.substring(0, 1).toUpperCase() + currentCity.substring(1);
            HBox cityBox = createCurrentWeatherHBox(currentCity);
            cityBox.setAlignment(Pos.CENTER_LEFT);
            ((Label) cityBox.getChildren().get(0)).setStyle("-fx-font-size: 18px;"); // Adjust font size if needed
            currentWeatherVBox.getChildren().add(cityBox);

            // Create HBox for temperature and weather icon
            HBox tempAndIconHBox = new HBox(10);
            currentWeatherVBox.getChildren().add(tempAndIconHBox);

            // Create VBox for temperature
            VBox tempBox = new VBox(10);
            String temp = currentWeather.get(1);
            Label tempLabel = new Label(temp + " °C"); // Adding "°C" to temperature label
            tempLabel.setStyle("-fx-font-size: 15px;"); // Adjust font size if needed
            tempLabel.setAlignment(Pos.CENTER); // Align to center
            tempBox.getChildren().add(tempLabel);
            tempBox.setAlignment(Pos.CENTER); // Align to center

            // Create VBox for weather icon
            VBox weatherIconBox = new VBox(10);
            weatherIconBox.setPrefHeight(60);
            weatherIconBox.setAlignment(Pos.CENTER); // Align to center

            ImageView iconImageView = getIcon(currentWeather.get(0));
            iconImageView.setFitWidth(60);
            iconImageView.setFitHeight(60);
            weatherIconBox.getChildren().add(iconImageView);

            // Add tempBox and weatherIconBox to tempAndIconHBox
            tempAndIconHBox.getChildren().addAll(tempBox, weatherIconBox);
            tempAndIconHBox.setAlignment(Pos.CENTER); 

            // Create min/max temperature box
            Map.Entry<String, List<String>> firstEntry = ((TreeMap<String, List<String>>) dailyForecast).firstEntry();
            List<String> firstList = firstEntry.getValue();
            String min = firstList.get(0);
            String max = firstList.get(1);
            HBox minMaxBox = createCurrentWeatherHBox(min + " °C / " + max + " °C");
            minMaxBox.setAlignment(Pos.CENTER);
            minMaxBox.getChildren().forEach(node -> ((Label) node).setStyle("-fx-font-size: 15px;")); // Adjust font size
            currentWeatherVBox.getChildren().add(minMaxBox);

            // Create wind speed box
            String wind = currentWeather.get(2);
            HBox windBox = createCurrentWeatherHBox(wind + " m/s");
            windBox.getChildren().forEach(node -> ((Label) node).setStyle("-fx-font-size: 15px;")); // Adjust font size
            currentWeatherVBox.getChildren().add(windBox);
        }
        return currentWeatherVBox;
    }

    /**
     * Creates an HBox containing a single line of current weather information.
     *
     * @param info Information to be displayed in the box.
     * @return HBox displaying the provided information.
     */
    private HBox createCurrentWeatherHBox(String info) {
        HBox hBox = new HBox(10);
        hBox.setPrefHeight(50);
        hBox.setStyle("-fx-background-color: rgba(220, 220, 220, 0); -fx-padding: 5;");
        Label label = new Label(info);
        hBox.setAlignment(Pos.CENTER); // Set alignment to CENTER
        hBox.getChildren().add(label); // Add label to the HBox
        return hBox;
    }

    /**
     * Creates an HBox to display hourly forecast information.
     *
     * @return HBox displaying hourly forecast.
     */
    private HBox getHourlyForecastHBox() {
        // Create an HBox for the middle panel
        HBox hourlyForecastHBox = new HBox(10); // Set spacing between nodes
        hourlyForecastHBox.setPrefHeight(75);
        hourlyForecastHBox.setStyle("-fx-background-color: rgba(220, 220, 220, 0.7); -fx-padding: 10;");
        hourlyForecastHBox.setAlignment(Pos.CENTER);

        // Check if hourlyForecast is empty
        if (hourlyForecast.isEmpty()) {
            // If no forecast available, display a default message
            Label label = new Label("No forecast available");
            hourlyForecastHBox.getChildren().add(label);
        } else {
            // If hourlyForecast is not empty, create HBoxes for each entry
            for (Map.Entry<String, List<String>> entry : hourlyForecast.entrySet()) {
                String date = entry.getKey(); // Get the date
                List<String> forecastData = entry.getValue(); // Get the forecast data for that date

                // Create an HBox for the current entry
                HBox hBox = createVBoxForHourlyForecast(date, forecastData);
                hourlyForecastHBox.getChildren().add(hBox);
            }
        }

        return hourlyForecastHBox;
    }

    /**
     * Creates a VBox to hold hourly forecast details vertically within an HBox.
     *
     * @param date The date of the forecast.
     * @param forecastData The forecast data for the specified date.
     * @return HBox containing the VBox with hourly forecast details.
     */
    private HBox createVBoxForHourlyForecast(String date, List<String> forecastData) {
        // Create a VBox to hold the forecast details vertically
        VBox vBox = new VBox(2); // Set spacing between nodes
        vBox.setStyle("-fx-background-color: rgba(220, 220, 220, 0.7); -fx-padding: 0;");

        // Example: Access specific forecast values (customize as needed)
        String time = date.substring(date.indexOf(" ") + 4); // Extract time from date
        String weather = forecastData.get(1);
        String temp = forecastData.get(0);

        // Labels for each attribute with fixed width
        Label timeLabel = new Label(time);
        Label weatherLabel = new Label(weather);
        Label tempLabel = new Label(temp + " °C");

        // Add labels and icon to the VBox
        vBox.getChildren().addAll(timeLabel, getIcon(weather), tempLabel);
        vBox.setAlignment(Pos.CENTER);

        // Wrap the VBox inside an HBox to ensure horizontal alignment
        HBox hBox = new HBox(vBox);
        hBox.setAlignment(Pos.CENTER);

        return hBox;
    }

    /**
     * Creates an HBox containing a button to trigger the rain data window.
     *
     * @return HBox with a button to get rain data.
     */
    private HBox getRainDataHBox() {
        // Create a button to trigger the rain data window
        Button rainDataButton = new Button("Get Rain Data");

        // Set action for the button
        rainDataButton.setOnAction(event -> {
            // Create a new stage for the rain data window
            Stage rainDataWindow = new Stage();
            rainDataWindow.setTitle("Rain Data");

            // Create a CategoryAxis for the X-axis (time) and a NumberAxis for the Y-axis (rain amount)
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Time");
            NumberAxis yAxis = new NumberAxis(0, 3, 1);
            yAxis.setLabel("Rain Amount (mm)");

            // Create a BarChart to display rain data
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Hourly Rain Data");

            // Create a series for rain data
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Rain Amount (mm)");

            // Add data to the series
            for (Map.Entry<String, List<String>> entry : hourlyForecast.entrySet()) {
                String time = entry.getKey();
                double rainAmount = Double.parseDouble(entry.getValue().get(2));

                // Extract the hour from the time string
                String[] timeParts = time.split(":");
                int hour = Integer.parseInt(timeParts[1]);

                // Format the hour to display on the X-axis
                String formattedHour = String.format("%02d:00", hour);

                series.getData().add(new XYChart.Data<>(formattedHour, rainAmount));
            }


            // Add the series to the BarChart
            barChart.getData().add(series);

            // Set label formatter for Y-axis to display integers only
            yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
                @Override
                public String toString(Number object) {
                    return String.valueOf(object.intValue());
                }
            });

            // Create layout for the rain data window
            VBox layout = new VBox();
            layout.getChildren().addAll(barChart);

            // Create a scene and set it to the stage
            Scene scene = new Scene(layout, 500, 300);
            rainDataWindow.setScene(scene);

            // Set modality to APPLICATION_MODAL to prevent interaction with other windows
            rainDataWindow.initModality(Modality.APPLICATION_MODAL);
            rainDataWindow.show();
        });

        // Create an HBox to hold the button
        HBox rainDataHBox = new HBox(rainDataButton);
        rainDataHBox.setAlignment(Pos.CENTER);
        rainDataHBox.setPadding(new Insets(0));

        return rainDataHBox;
    }

    /**
     * Creates a VBox to display daily forecast information.
     *
     * @return VBox displaying daily forecast.
     */
    private VBox getDailyForecastVBox() {
        // Create a VBox for the right side
        VBox dailyForecastVBox = new VBox();
        dailyForecastVBox.setPrefHeight(300);
        dailyForecastVBox.setStyle("-fx-background-color: rgba(220, 220, 220, 0.7); -fx-padding: 10;");

        // Check if dailyForecast is empty
        if (dailyForecast.isEmpty()) {
            // If no forecast available, add a default message to the VBox
            Label label = new Label("No forecast available");
            dailyForecastVBox.getChildren().add(label);
        } else {
            // If dailyForecast is not empty, create HBoxes for each entry
            for (Map.Entry<String, List<String>> entry : dailyForecast.entrySet()) {
                String date = entry.getKey(); // Get the date
                List<String> forecastData = entry.getValue(); // Get the forecast data for that date

                // Create an HBox for the current entry
                HBox hBox = createHBoxForDailyForecast(date, forecastData);
                dailyForecastVBox.getChildren().add(hBox);
            }
        }

        return dailyForecastVBox;
    }

    /**
     * Creates an HBox to hold the daily forecast details.
     *
     * @param date The date of the forecast.
     * @param forecastData The forecast data for the specified date.
     * @return HBox containing the daily forecast details.
     */
    private HBox createHBoxForDailyForecast(String date, List<String> forecastData) {
        // Create an HBox to hold the forecast details
        HBox hBox = new HBox(10); // Set spacing between nodes
        hBox.setPrefHeight(50);
        hBox.setStyle("-fx-background-color: rgba(220, 220, 220, 0); -fx-padding: 10;");

        // Extract day and month from the date
        String[] parts = date.split("/");
        String dayMonth = parts[1] + "/" + parts[0];

        // Access specific forecast values
        int minTemp = (int) Math.round(Double.parseDouble(forecastData.get(0)));
        int maxTemp = (int) Math.round(Double.parseDouble(forecastData.get(1)));
        String weather = forecastData.get(2);

        // Labels for each attribute with fixed width
        Label dateLabel = new Label(dayMonth);
        dateLabel.setPrefWidth(110);
        dateLabel.setStyle("-fx-font-size: 13px;");

        Label weatherLabel = new Label("Weather: " + weather);
        weatherLabel.setPrefWidth(150);
        weatherLabel.setStyle("-fx-font-size: 13px;");

        Label tempLabel = new Label(minTemp + "° / " + maxTemp + "°");
        tempLabel.setPrefWidth(110);
        tempLabel.setStyle("-fx-font-size: 13px;");

        hBox.setAlignment(Pos.CENTER);

        // Add labels to the HBox
        hBox.getChildren().addAll(dateLabel, getIcon(weather), tempLabel);

        return hBox;
    }

    /**
     * Creates a button labeled "Quit" to terminate the application.
     *
     * @return Quit button.
     */
    private Button getQuitButton() {
        // Create a button
        Button button = new Button("Quit");

        // Add an event to terminate the application when clicked
        button.setOnAction((ActionEvent event) -> {
            Memory.changeCurrentCity();
            Memory.saveFavorites();
            Memory.saveHistory();
            Memory.saveToJson();
            Platform.exit();
        });

        return button;
    }

    /**
     * Creates a search button with functionality to fetch weather information
     * for the specified location.
     *
     * @return Search button.
     */
    private Button createSearchButton() {
        Button searchButton = new Button("Search");
        searchButton.setOnAction(event -> {
            String location;
            if (GlobalVariables.getCurrentFavorite() != "") {
                location = GlobalVariables.getCurrentFavorite();
            } else {
                location = capitalizeLocation(locationTextField.getText());
            }
            // Add city to history and move it to the front of history
            GlobalVariables.addCityToHistory(location);
            GlobalVariables.changeCurrentFavorite("");

            double[] locationInfo = weatherAPI.lookUpLocation(location);

            // Check if locationInfo contains [1000, 1000], idicates that apicall did not work
            if (locationInfo[0] == 1000 && locationInfo[1] == 1000) {
                return; // This will exit the method without executing further code.
            }

            currentWeather = weatherAPI.getCurrentWeather(locationInfo[0], locationInfo[1]);
            hourlyForecast = weatherAPI.getHourlyForecast(locationInfo[0], locationInfo[1]);
            dailyForecast = weatherAPI.getDailyForecast(locationInfo[0], locationInfo[1]);

            // Update the middle HBox with the new hourly forecast data
            HBox middleHBox = getHourlyForecastHBox();

            // Retain other components in the center VBox
            VBox centerVBox = getEveryBox();
            centerVBox.getChildren().set(2, middleHBox); // Replace the middle HBox

            BorderPane.setAlignment(centerVBox, Pos.CENTER);
            ((BorderPane) searchButton.getScene().getRoot()).setCenter(centerVBox);

            if (favoriteButton.isDisabled()) {
                favoriteButton.setDisable(false);
            }

            // Update favorite button
            if (GlobalVariables.isFavorite(location)) {
                favoriteButton.setText("</3");
            } else {
                favoriteButton.setText("<3");
            }
        });

        return searchButton;
    }

    /**
     * Displays weather information for the current city.
     */
    private void displayWeatherForCurrentCity() {
        // Get the current city
        String currentCity = GlobalVariables.getCurrentCity();
        // Get weather information for the current city
        double[] locationInfo = weatherAPI.lookUpLocation(currentCity);
        currentWeather = weatherAPI.getCurrentWeather(locationInfo[0], locationInfo[1]);
        hourlyForecast = weatherAPI.getHourlyForecast(locationInfo[0], locationInfo[1]);
        dailyForecast = weatherAPI.getDailyForecast(locationInfo[0], locationInfo[1]);

        // Update the middle HBox with the new hourly forecast data
        HBox middleHBox = getHourlyForecastHBox();

        // Retain other components in the center VBox
        VBox centerVBox = getEveryBox();
        centerVBox.getChildren().set(2, middleHBox);

        BorderPane.setAlignment(centerVBox, Pos.CENTER);
        root.setCenter(centerVBox);

        if (favoriteButton.isDisabled()) {
            favoriteButton.setDisable(false);
        }

        // Update favorite button
        if (GlobalVariables.isFavorite(currentCity)) {
            favoriteButton.setText("</3");
        } else {
            favoriteButton.setText("<3");
        }
    }

    /**
     * Creates a button with a heart symbol to mark or unmark the current city
     * as a favorite.
     *
     * @return Favorite button.
     */
    private Button createFavoriteButton() {
        favoriteButton = new Button("<3");

        favoriteButton.setOnAction(event -> {
            if (!GlobalVariables.isFavorite(GlobalVariables.getCurrentCity())) {
                GlobalVariables.addFavorite(GlobalVariables.getCurrentCity());
                favoriteButton.setText("</3");
            } else {
                GlobalVariables.removeFavorite(GlobalVariables.getCurrentCity());
                favoriteButton.setText("<3");
            }
        });
        return favoriteButton;
    }

    /**
     * Creates a button to open the favorites list.
     *
     * @return Favorites list button.
     */
    private Button createFavoriteListButton() {
        favoriteListButton = new Button("Favorites");

        favoriteListButton.setOnAction(event -> {
            openPopup(new Stage(), previousStage, searchButton);
        });

        return favoriteListButton;
    }

    /**
     * Creates a button to open the search history.
     *
     * @return History button.
     */
    private Button createHistoryButton() {
        historyButton = new Button("History");

        historyButton.setOnAction(event -> {
            openHistoryPopup(new Stage(), previousStage, searchButton);
        });

        return historyButton;
    }

    /**
     * Retrieves the appropriate icon image based on the weather type.
     *
     * @param weather The type of weather.
     * @return ImageView object representing the weather icon.
     */
    private ImageView getIcon(String weather) {
        // Define the default image path
        String imagePath = "/images/random.png";
        // Check the weather type and update the image path accordingly
        switch (weather) {
            case "Clouds":
                imagePath = "/images/cloud.png";
                break;
            case "Rain":
                imagePath = "/images/rain.png";
                break;
            case "Clear":
                imagePath = "/images/sun.png";
                break;
            case "Snow":
                imagePath = "/images/snow.png";
                break;
        }

        // Load the icon image
        Image iconImage = new Image(getClass().getResourceAsStream(imagePath));

        // Create an ImageView for the icon
        ImageView iconImageView = new ImageView(iconImage);

        // Set the dimensions of the ImageView to make the image smaller
        iconImageView.setFitWidth(25); // Set the width to 20 pixels
        iconImageView.setFitHeight(25); // Set the height to 20 pixels

        return iconImageView; // Return the ImageView object
    }

    /**
     * Retrieves the appropriate background image based on the weather type.
     *
     * @param weather The type of weather.
     * @return ImageView object representing the background image.
     */
    private ImageView getBackground(String weather) {
        // Define the default image path
        String imagePath = "/images/background.jpg";
        // Check the weather type and update the image path accordingly
        switch (weather) {
            case "Clouds":
                imagePath = "/images/background.jpg";
                break;
            case "Rain":
                imagePath = "/images/rain.jpg";
                break;
            case "Clear":
                imagePath = "/images/sun.jpg";
                break;
            case "Snow":
                imagePath = "/images/snow.jpg";
                break;
        }

        // Load the background image
        Image backgroundImage = new Image(getClass().getResourceAsStream(imagePath));

        // Create an ImageView for the background
        ImageView backgroundImageView = new ImageView(backgroundImage);

        return backgroundImageView; // Return the ImageView object
    }

    /**
     * Opens a popup window to display the list of favorite locations.
     *
     * @param newStage The stage for the popup window.
     * @param previousStage The stage of the main application window.
     * @param searchButton The search button of the main application.
     */
    private void openPopup(Stage newStage, Stage previousStage, Button searchButton) {
        newStage.initModality(Modality.APPLICATION_MODAL);

        Label headerLabel = new Label("Favorites");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(event -> {
            previousStage.show();
            newStage.close();
        });

        VBox container = new VBox();
        container.getChildren().add(headerLabel);

        int size = GlobalVariables.amountOfFavorites();
        List<String> favoritesList = GlobalVariables.getFavorites();

        for (int i = 0; i < size; i++) {
            Button button = new Button(favoritesList.get(i));
            int index = i;
            button.setOnAction(event -> {
                previousStage.show();
                newStage.close();
                GlobalVariables.changeCurrentFavorite(favoritesList.get(index));
                searchButton.fire();
            });
            container.getChildren().add(button);
        }

        container.getChildren().add(new Region());
        VBox.setVgrow(new Region(), Priority.ALWAYS);

        container.getChildren().add(quitButton);

        container.setSpacing(10);
        container.setPadding(new Insets(10));
        newStage.setScene(new Scene(container));

        newStage.setResizable(false);

        newStage.show();
    }

    /**
     * Opens a popup window to display the search history.
     *
     * @param newStage The stage for the popup window.
     * @param previousStage The stage of the main application window.
     * @param searchButton The search button of the main application.
     */
    private void openHistoryPopup(Stage newStage, Stage previousStage, Button searchButton) {
        //blocks input to other window
        newStage.initModality(Modality.APPLICATION_MODAL);

        Label headerLabel = new Label("History");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(event -> {
            previousStage.show();
            newStage.close();
        });

        VBox container = new VBox();
        container.getChildren().add(headerLabel);

        int size = GlobalVariables.sizeOfHistory();
        List<String> historyList = GlobalVariables.getHistory();

        for (int i = 0; i < size; i++) {
            Button button = new Button(historyList.get(i));
            int index = i;
            button.setOnAction(event -> {
                previousStage.show();
                newStage.close();
                GlobalVariables.changeCurrentFavorite(historyList.get(index));
                searchButton.fire();
            });
            container.getChildren().add(button);
        }

        container.getChildren().add(new Region());
        VBox.setVgrow(new Region(), Priority.ALWAYS);

        container.getChildren().add(quitButton);

        container.setSpacing(10);
        container.setPadding(new Insets(10));
        newStage.setScene(new Scene(container));

        newStage.setResizable(false);

        newStage.show();
    }

    /**
     * Capitalizes the first letter of each word in a given location string.
     *
     * @param location The location string to be capitalized.
     * @return The capitalized location string.
     */
    public static String capitalizeLocation(String location) {
        String[] words = location.toLowerCase().split("\\s+");
        StringBuilder capitalizedLocation = new StringBuilder();
        //capitalizes first letter of each word
        for (String word : words) {
            if (!word.isEmpty()) {
                String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
                capitalizedLocation.append(capitalizedWord).append(" ");
            }
        }
        return capitalizedLocation.toString().trim();
    }

}
