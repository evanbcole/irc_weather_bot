import com.google.gson.*;
import org.jibble.pircbot.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class API_access {
    public static void main(String[] args) throws IOException, IrcException {
        // create an instance of MyBot and connect it the "weatherman" channel at irc.freenode.net
        MyBot bot = new MyBot();
        bot.setVerbose(true);
        bot.connect("irc.freenode.net");
        bot.joinChannel("#weatherman");
    }
}

// calls OpenWeatherMap api and returns weather data based on entered zip code
class WeatherFunctions {
    private JsonObject obj;

    // json object to call functions is created in constructor
    WeatherFunctions(String zip) {

        // full call, including user provided zip code
        String fullCall = "http://api.openweathermap.org/data/2.5/weather?zip=" + zip +
                ",us&APPID=23dee912e2b97f3ac1857301d37e3c66";

        // try to make an HTTP call and get a JSON object
        try {
            URL url = new URL(fullCall);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String json = rd.lines().collect(Collectors.joining());

            this.obj = new JsonParser().parse(json).getAsJsonObject();
        }

        // catch an invalid url
        catch (MalformedURLException e) {
            System.out.println("Invalid URL");
        }

        // catch other IO errors
        catch (IOException e) {
            System.out.println("IO problem");
        }
    }

    // returns fahrenheit temp rounded to whole number
    public int getTemp() {
        JsonObject main = this.obj.getAsJsonObject("main");
        return (int)(1.8 * (main.get("temp").getAsDouble() - 273.15) + 32); // converting from kelvin
    }

    // return name of city (the location of closest weather station to entered zip)
    public String getName() {
        return this.obj.get("name").getAsString();
    }

    // returns percentage humidity
    public int getHumidity() {
        JsonObject main = this.obj.getAsJsonObject("main");
        return main.get("humidity").getAsInt();
    }
}

// this class contains 2 different APIs.
// one returns a random cat image, and the other returns a random dog image
class RandomAnimals {
    private JsonObject obj;

    RandomAnimals(String type) {
        String animal = "";
        if (type.equalsIgnoreCase("dog")) {      // if dog is passed in, call the dog api
            animal = "https://random.dog/woof.json";
        }
        else if (type.equalsIgnoreCase("cat")) { // if cat is passed in, call the cat api
            animal = "https://aws.random.cat/meow";
        }

        // try to make an HTTP call and get a JSON object
        try {
            URL url = new URL(animal);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String json = rd.lines().collect(Collectors.joining());

            this.obj = new JsonParser().parse(json).getAsJsonObject();
        }

        // catch an invalid url
        catch (MalformedURLException e) {
            System.out.println("Invalid URL");
        }

        // catch other IO errors
        catch (IOException e) {
            System.out.println("IO problem");
        }
    }

    // returns a link to a random picture of a cat
    public String getDog() {
        return obj.get("url").getAsString();
    }

    // returns a link to a random picture of a cat
    public String getCat() {
        return obj.get("file").getAsString();
    }
}

/*
IRC bot made using PircBot.
Commands include:
!hello -> sends a message saying "Hello, <username>!"
!help -> prints a list of commands and gives a brief description of what they do
!weather <zip code> -> when entered with a valid US zip code, returns the City, Temperature, and Humidity
!dog -> sends a message with a link to a random dog photo
!cat -> sends a message with a link to a random cat photo
 */
class MyBot extends PircBot {
    public MyBot() {
        this.setName("Weatherman");
    }

    // overwritten onMessage method from PircBot library. determines when bot responds to a message in chat
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        // !help
        if (message.equalsIgnoreCase("!help")) {
            sendMessage(channel, "List of commands:");
            sendMessage(channel, "!help: Displays this help message.");
            sendMessage(channel, "!hello: The bot tells you hello.");
            sendMessage(channel, "!weather: Enter the command followed by a valid US zip code for current" +
                    " weather data. EX: !weather 10001. Note: OpenWeatherMap returns the location of the nearest " +
                    "weather data station, not necessarily the exact location.");
            sendMessage(channel, "!dog: Sends a link to a random picture of a dog!");
            sendMessage(channel, "!cat: Sends a link to a random picture of a cat!");
        }

        // !hello
        else if (message.equalsIgnoreCase("!hello")) {
            sendMessage(channel, "Hello, " + sender + "!");
        }

        // !weather
        else if (message.contains("!weather")) {
            String[] numWords = message.split(" "); // split into array

            // if the length of array is 1, then no zip was added
            if (numWords.length == 1) {
                sendMessage(channel,"Try adding a zip code to receive current weather data for that area!");
            }

            // if the length of array was 2, and the second index is only numbers, then a zip was added
            else if (numWords.length == 2 && numWords[1].matches("[0-9]+")) {
                try {
                    WeatherFunctions obj = new WeatherFunctions(numWords[1]);
                    sendMessage(channel, "City: " + obj.getName());
                    sendMessage(channel, "Temperature: " + obj.getTemp() + "° F");
                    sendMessage(channel, "Humidity: " + obj.getHumidity() + "% humidity");

                }

                // catches invalid zip codes
                catch (NullPointerException e) {
                    sendMessage(channel,"Invalid zip code.");
                }
            }
        }

        // !dog
        else if (message.equalsIgnoreCase("!dog")) {
            RandomAnimals newDog = new RandomAnimals("dog");
            sendMessage(channel, "Here is a dog picture: " + newDog.getDog());
        }

        // !cat
        else if (message.equalsIgnoreCase("!cat")) {
            RandomAnimals newCat = new RandomAnimals("cat");
            sendMessage(channel, "Here is a cat picture: " + newCat.getCat());
        }
    }

    // overwrites the onJoin function from PircBot library. Sends a message when the bot joins the chat
    public void onJoin(String channel, String sender, String login, String hostname) {
        if (sender.equals("Weatherman")) {
            sendMessage(channel, "Hello! Type !help for a list of commands!");
        }
    }
}