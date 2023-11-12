import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


/* Retrieve weather data for API
   - backend will fetch from external framework
     and return it here to use
 */
public class WeatherApp{
    // Fetch weather data for x location:
    public static JSONObject getWeatherData(String locationName){
        // Location Coord:
        JSONArray coordinates = getLocationData(locationName);

        // extract coordinates (latitude and longitude) data:
        JSONObject location = (JSONObject) coordinates.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // build API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch&timezone=America%2FNew_York";

        //https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41
        //latitude=52.52&longitude=13.41&hourly=temperature_2m,weather_code,wind_speed_10m,temperature_80m&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch&timezone=America%2FNew_York
        //https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m
        try{
            // call the API and fetch response:
            HttpURLConnection httpURLConnection = fetchApiResponce(urlString);

            // check for response status: RS = 200 = connection
            if(httpURLConnection.getResponseCode() != 200){
                System.out.println("Error: I cant find the URL RS!");
                return null;
            }
            // Store the data result:
            StringBuilder resultJSON = new StringBuilder();
            Scanner sc = new Scanner(httpURLConnection.getInputStream());
            while(sc.hasNext()){
                resultJSON.append(sc.nextLine());
            }
            sc.close();
            httpURLConnection.disconnect();

            // Parse data (check for syntactical correctness/ obtain values stored in the JSON)
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObjectResult = (JSONObject) jsonParser.parse(String.valueOf(resultJSON));

            // retrieve hourly data:
            JSONObject hourly = (JSONObject) jsonObjectResult.get("hourly");

            // we want the current hour's data, we need to keep track
            // of the index of the current hour
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // getter for temperature:
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // getter for weather code:
            JSONArray weatherCode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

            // getter for humidity:
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // getter for windspeed:
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windSpeed = (double) windspeedData.get(index);

            // Construct the composite JSON object from this data:
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windSpeed);

            return weatherData;

        }catch(Exception exception){
            exception.printStackTrace();
        }
        return null;
    }

    public static JSONArray getLocationData(String locationName){
        // API requirement:
        locationName = locationName.replaceAll(" ", "+");


        // build API url with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            // call API with method
            HttpURLConnection connected = fetchApiResponce(urlString);

            // Check HTTP Response Status Level:
            // 200 = Successful connection
            // 400 = Bad request
            // 500 = Internal Server Error

            if(connected.getResponseCode() != 200){
                System.out.println("HTTP Response Status Error");
                return null;
            }else{
                // Store API Results ***IN A STRING BUILDER (MutableString):
                StringBuilder resultJSONSB = new StringBuilder();
                Scanner sc = new Scanner(connected.getInputStream());

                // Scan in and store
                while(sc.hasNext()){
                    resultJSONSB.append(sc.nextLine());
                }
                sc.close();
                connected.disconnect(); // close url connection

                // Parse JSON string to object
                JSONParser jsonParser = new JSONParser();
                JSONObject resultJSONObject = (JSONObject) jsonParser.parse(String.valueOf(resultJSONSB));

                // get the list of location data the API generates from the location
                // the user types in:
                JSONArray locationData = (JSONArray) resultJSONObject.get("results");
                return locationData;
            }
        }catch(Exception exception){
            exception.printStackTrace();
        }
        // compiler requirement: could not find location data
        return null;
    }

    // instantiate method:
    private static HttpURLConnection fetchApiResponce(String urlStr){
        try{
            // Try to find a connection:
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // set request method to get
            connection.setRequestMethod("GET");

            // connect to the API
            connection.connect();
            return connection;
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
        // compiler requirement
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeArr){
        String currentTime = getCurrentTime();

        // Iterate through to find a time match:
        for(int i = 0; i < timeArr.size(); i++){
            String time = (String) timeArr.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                // return the index:
                return i;
            }
        }
        // compiler requirement
        return 0;
    }

    public static String getCurrentTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Format date to reflect how its set up (pattern) in the API
        DateTimeFormatter formatterAPI = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // Format and print out the current date and time data:
        String formattedDateAndTime = currentDateTime.format(formatterAPI);

        return formattedDateAndTime;
    }

    // Converts the code to WMO interpretation form (re-work later):
    public static String convertWeatherCode(long weatherCode){
        String weatherCondition = "";
        if(weatherCode == 0L){
            weatherCondition = "Sunny";
        }else if(weatherCode <= 3L && weatherCode > 0L){
            weatherCondition = "Cloudy";
        }else if((weatherCode >= 51L && weatherCode <= 67L)
                || (weatherCode >= 80L && weatherCode <= 99L)){
            weatherCondition = "Rainy";
        }else if(weatherCode >= 71L && weatherCode <= 77L){
            weatherCondition = "Snow";
        }
        return weatherCondition;
    }
}