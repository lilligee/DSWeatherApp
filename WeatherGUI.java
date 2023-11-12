import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
public class WeatherGUI extends JFrame{
    private JSONObject weatherData;
    public WeatherGUI(){
        super("Weather"); // 1. GUI Setup title
        setDefaultCloseOperation(EXIT_ON_CLOSE); // so gui ends when closed
        setSize(500, 382); // 450'650 pixels
        setLocationRelativeTo(null); // load gui in the center of the screen
        setLayout(null); // to be able to customize placement
        setResizable(false); // so the gui cant be manipulated
        // Here is the portion to add the pngs:
        addGuiComponents();
    }
        public void addGuiComponents(){

            JTextField searchTxtField = new JTextField(); // search field
            searchTxtField.setBounds(10,309,351,30); // set location and size
            // change font style & size:
            searchTxtField.setFont(new Font("Nintendo DS BIOS", Font.ROMAN_BASELINE, 18));
            add(searchTxtField);

            // temperature text:
            JLabel temperatureText = new JLabel("10'F");
            temperatureText.setBounds(-196,0,500,65);
            temperatureText.setFont(new Font("Nintendo DS BIOS",Font.PLAIN,48));

            // center the text:
            temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
            add(temperatureText);

            // weather condition description:
            JLabel weatherConditionDesc = new JLabel("Sunny");

            // Setting type font
            weatherConditionDesc.setFont(new Font("Nintendo DS BIOS", Font.PLAIN,32));
            weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
            weatherConditionDesc.setBounds(-206,32,500,65);
            add(weatherConditionDesc);

            // humid test:
            JLabel humidText = new JLabel("<html><b>Humidity</b> 100%</html>");
            humidText.setBounds(380,45,500,310);
            humidText.setFont(new Font("Nintendo DS BIOS", Font.PLAIN, 16));
            add(humidText);

            // windspeed text
            JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km</html>");
            windspeedText.setBounds(380,25,500,310);
            windspeedText.setFont(new Font("Nintendo DS BIOS",Font.PLAIN,16));
            add(windspeedText);

            // add a search button from directory:
            JButton searchButton = new JButton(loadImage("src/assets/search.png"));
            // changes to a hand when hovered over:
            searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            searchButton.setBounds(370,309,47,29);
            searchButton.addActionListener(new ActionListener(){

                // Here starts the connection of the front to the back:
                @Override
                public void actionPerformed(ActionEvent e){
                    // fetch location from the user-input:
                    String userInput = searchTxtField.getText();

                    // format the input (regular expression quantifier)
                    if(userInput.replaceAll("\\s", "").length() <= 0){
                        return;
                    }
                    // retrieve weatherData: (Type-casting ensures we get the right type from a JSONObject)
                    weatherData = WeatherApp.getWeatherData(userInput);
                    // here is where the OOP connects
                    String weatherConditionDeterminant = (String) weatherData.get("weather_condition");
                    // Update the temperature text:
                    double temperature = (double) weatherData.get("temperature");
                    temperatureText.setText(temperature +" F");

                    // Update weatherCondition text:
                    weatherConditionDesc.setText(weatherConditionDeterminant);

                    // Update humidity test (and format)
                    long humidity = (long) weatherData.get("humidity");
                    humidText.setText("<html><b>Humidity</b> "+humidity+"%<html>");

                    // update windspeed test (and format)
                    double windspeed = (double) weatherData.get("windspeed");
                    windspeedText.setText("<html><b>Windspeed</b> "+windspeed+"mph<html>");

                    JSONObject weatherImagePaths = new JSONObject();
                    weatherImagePaths.put("Sunny", "src/assets/acwwsunny.GIF");
                    weatherImagePaths.put("Cloudy", "src/assets/acwwcloudysky.jpg");
                    weatherImagePaths.put("Rainy", "src/assets/acwwrainy.GIF");
                    weatherImagePaths.put("Snow", "src/assets/acwwsnowy.GIF");

                    JSONObject descriptors = new JSONObject();
                    descriptors.put("Sunny", "Clear skies!");
                    descriptors.put("Cloudy", "The sky is covered in a big,\ngrey blanket");
                    descriptors.put("Rainy", "Dont forget your umbrella!");
                    descriptors.put("Snow", "Let it snow,\tlet it snow,\tlet it snow!");

                    JLabel weatherBackground = null;
                    switch(weatherConditionDeterminant){
                        case "Sunny":
                        case "Cloudy":
                        case "Rainy":
                        case "Snow":
                            removeAllWeatherImages();
                            weatherConditionDesc.setText(weatherConditionDeterminant);

                            String imagePath = (String) weatherImagePaths.get(weatherConditionDeterminant);
                            if(imagePath != null){
                                weatherBackground = new JLabel(loadImage(imagePath));
                                weatherBackground.setBounds(0, 0, 500, 382);
                            }
                            // Get the descriptor for the current weather condition
                            String descriptor = (String) descriptors.get(weatherConditionDeterminant);
                            if(descriptor != null) {
                                JLabel weatherDescriptor = new JLabel(descriptor);
                                weatherDescriptor.setBounds(10,248,500,65);
                                weatherDescriptor.setFont(new Font("Nintendo DS BIOS",Font.PLAIN,20));
                                //weatherDescriptor.setText(""); yuck
                                add(weatherConditionDesc);
                                add(weatherDescriptor);
                                add(weatherBackground);
                            }
                            break;
                    }
                    revalidate();
                    repaint();
                }
                // importatnt
                public void removeAllWeatherImages(){
                    Component[] components = getContentPane().getComponents();
                    for (Component component : components) {
                        if (component instanceof JLabel && !component.equals(temperatureText)
                                && !component.equals(humidText) && !component.equals(windspeedText)){
                            remove(component);
                        }
                    }
                }

            });
            add(searchButton);

    }
    // exception-handling-block to use images in the gui window
    private ImageIcon loadImage(String resourcePath){
        try{
            // read the file from given path
            BufferedImage image = ImageIO.read(new File(resourcePath));
            // returns the icon in a render-able format
            return new ImageIcon(image);
        }catch(IOException exception){
            exception.printStackTrace();
        }
        System.out.println("I cant find the image!");
        return null;
    }
}
