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

### Google Calendar Event Emoji Prefixer

1. Build the project using the `./gradlew build` command.
2. Run the project using the `./gradlew run` command.

### Cron Job

1. Create a new cron job using the `crontab -e` command.
2. Add the following line to the cron job file:

    ```bash
    0 0 * * * /path/to/java -jar /path/to/google-calendar-event-emoji-prefixer-1.0.0.jar
    ```

3. Save the file.
4. Restart the cron job using the `service cron restart` command.
5. The script will now run every day at midnight.

## License

MIT License