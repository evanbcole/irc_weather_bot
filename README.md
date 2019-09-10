# IRC Weather bot
This is a bot designed to be active in an IRC channel using PircBot and to make calls to the OpenWeatherMap api to provide weather data based on the provided United States zip code.

As previously mentioned, the bot makes use of the PircBot library to provide the framework for the bot, in addition to using the Gson library to parse and convert the Json objects provided by the APIs to Java objects. This data is then fed to the PircBot implementation and output in the IRC channel.

### List of Commands
- !hello -> sends a message saying "Hello, 'username'!"
- !help -> prints a list of commands and gives a brief description of what they do
- !weather 'zip code' -> when entered with a valid US zip code, returns the City, Temperature, and Humidity


As a bonus the bot also makes calls to *random.dog/woof.json* and *aws.random.cat/meow* which return return a link to a random image of a dog or cat respectively!
- !dog -> sends a message with a link to a random dog photo
- !cat -> sends a message with a link to a random cat photo
