## What is Google Calendar Event Emoji Prefixer?

Google Calendar Event Emoji Prefixer is a simple Kotlin script that adds an emoji prefix to the title of your Google Calendar events.
The best way of using this script is to run it as a cron job or a scheduled task.


## How to use

### Google Cloud Platform

1. Go to the [Google Cloud Platform](https://console.cloud.google.com/) and create a new project.
2. Enable the **Google Calendar API**.
3. Create a new **OAuth 2.0 client ID**.
4. Download the `credentials.json` file and place it in the `resources` folder.

### Google Calendar

1. Go to the [Google Calendar](https://calendar.google.com/) and create a new calendar.
2. Add the emoji you want to use as a prefix for your events to your calendar's name. For example, if you want to use the 📅 emoji, name your calendar `📅 My Calendar`.
3. The script will use the emoji as a prefix for your events for that calendar.

### Mailjet API for Notifications

This is an optional step. If you want to receive notifications when something goes wrong with authorization, you can use the Mailjet API.

1. Go to the [Mailjet](https://app.mailjet.com/) and create a new account.
2. Get your API key and secret.
3. Copy the `local.properties.example` file to the `local.properties` file.
3. Add the API key and secret to the `local.properties` file.
4. Add your email address to the `local.properties` file.
5. The script will send you an email if something goes wrong with authorization.

### Google Calendar Event Emoji Prefixer

1. Build the project using the `./gradlew shadowJar` command.
   
   *If you are missing the permissions to run the command, use the `chmod +x gradlew` command.*

2. Run the project using the `java -jar build/libs/emojiprefixer.jar` command.

### Cron Job

1. Create a new cron job using the `crontab -e` command.
2. Add the following line to the cron job file:

    ```bash
    0 0 * * * /path/to/java -jar /path/to/emojiprefixer.jar
    ```

3. Save the file.
4. Restart the cron job using the `service cron restart` command.
5. The script will now run every day at midnight.

## Troubleshooting

### My token is expired after 7 days

Make sure to go into the [Google Cloud OAuth Consent Screen](https://console.cloud.google.com/apis/credentials/consent) and publish your app. 
NOTE: It is not required to verify the app. It just needs to say "In production" under Publishing status

## License

MIT License