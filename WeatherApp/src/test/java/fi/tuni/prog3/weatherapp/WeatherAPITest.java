package fi.tuni.prog3.weatherapp;

import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the WeatherAPI class.
 */
public class WeatherAPITest {

    private static final String API_KEY = "b8238bf2e5d810aa939ec3ac98ffb8ea";
    
    private WeatherAPI instance;
    private static final double VALID_LATITUDE = 60.1695; // Valid latitude for testing
    private static final double VALID_LONGITUDE = 24.9355; // Valid longitude for testing
    

    /**
     * Initialization code executed once before all test methods.
     */
    @BeforeAll
    public static void setUpClass() {
        
    }

    /**
     * Cleanup code executed once after all test methods.
     */
    @AfterAll
    public static void tearDownClass() {
        
    }

    /**
     * Initialize the instance before each test method.
     */
    @BeforeEach
    public void setUp() {
        instance = new WeatherAPI();
    }

    /**
     * Clean up the instance after each test method.
     */
    @AfterEach
    public void tearDown() {
        instance = null;
    }

    
    /**
     * Tests the lookUpLocation method with a valid location parameter.
     * 
     */
    @Test
    public void testLookUpLocation_SuccessfulResponse() {
        
        // Test method
        double[] result = instance.lookUpLocation("Helsinki");

        // Verify result
        assertArrayEquals(new double[]{VALID_LATITUDE, VALID_LONGITUDE}, result);
    }
    
    
    
    /**
     * Tests the lookUpLocation method with invalid location parameter.
     * 
     */
    @Test
    public void testLookUpLocation_UnsuccessfulResponse(){
        
        // Test method
        double[] result = instance.lookUpLocation("Invalid Location");

        // Verify result
        assertArrayEquals(new double[]{1000, 1000}, result);
    }
    
    
    
    /**
     * Tests the getCurrentWeather method with valid latitude and longitude parameters.
     */
    @Test
    public void testGetCurrentWeather_ValidParameters() {
        List<String> result = instance.getCurrentWeather(VALID_LATITUDE, VALID_LONGITUDE);
        assertNotNull(result); // Ensure the result is not null
        assertEquals(3, result.size()); // Ensure the result contains three elements
    }


   
    /**
     * Tests the getHourlyForecast method with valid latitude and longitude parameters.
     */
    @Test
    public void testGetHourlyForecast_ValidParameters() {
        TreeMap<String, List<String>> result = instance.getHourlyForecast(VALID_LATITUDE, VALID_LONGITUDE);
        assertValidResult(result);
        assertEquals(12, result.size()); // Ensure the result contains forecast data for 12 hours
    }

    /**
     * Tests the getDailyForecast method with valid latitude and longitude parameters.
     */
    @Test
    public void testGetDailyForecast_ValidParameters() {
        TreeMap<String, List<String>> result = instance.getDailyForecast(VALID_LATITUDE, VALID_LONGITUDE);
        assertValidResult(result);
        assertEquals(7, result.size()); // Ensure the result contains forecast data for 7 days
    }

    /**
     * Helper method to assert that the result TreeMap is valid (not null and not empty).
     * 
     * @param result The TreeMap to be validated.
     */
    private void assertValidResult(TreeMap<String, List<String>> result) {
        assertNotNull(result); // Ensure the result is not null
        assertFalse(result.isEmpty()); // Ensure the result is not empty
    }
}
