module fi.tuni.progthree.weatherapp {
    requires javafx.controls;
    exports fi.tuni.prog3.weatherapp;
    requires com.google.gson;
    opens fi.tuni.prog3.weatherapp to com.google.gson;
}
