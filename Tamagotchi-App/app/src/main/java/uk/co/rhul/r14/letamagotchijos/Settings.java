package uk.co.rhul.r14.letamagotchijos;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores the settings for the app
 *
 * @author Danny
 * @version 1.0
 */
public class Settings {

    private final static String MAC_ADDR_FIELD = "EV3_MAC_ADDR", BOT_NAME = "BOT_NAME";
    private String ev3MAC, tamagotchiName;
    private final boolean isNewBot;

    /**
     * Creates the settings object, reading the settings from the file if it exists and is readable
     *
     * @param context -> app context
     * @since 1.0
     */
    public Settings(Context context) {
        File settingsFile = getFile(context);

        boolean success = false;
        if (settingsFile.exists()) {
            try {
                //Read yaml
                DataInputStream dataStream = new DataInputStream(new BufferedInputStream(new FileInputStream(settingsFile)));

                List<String> lines = new LinkedList<>();

                char c;
                StringBuilder sb = new StringBuilder();
                while ((c = dataStream.readChar()) != -1) {
                    if (c != '\n') {
                        sb.append(c);
                    } else {
                        lines.add(sb.toString());
                        sb = new StringBuilder();
                    }
                }

                if (sb.length() > 0)
                    lines.add(sb.toString());

                // Parse yaml
                for (String line : lines) {
                    String[] data = line.split("=");
                    if (data.length >= 2) {
                        String field = data[0];

                        sb = new StringBuilder();
                        for (int i = 1; i < data.length; i++) {
                            if (i > 1) {
                                sb.append("=");
                            }

                            sb.append(data[i]);
                        }

                        String value = sb.toString();

                        if (field.equals(MAC_ADDR_FIELD)) {
                            this.ev3MAC = value;
                        } else if (field.equals(BOT_NAME)) {
                            this.tamagotchiName = value;
                        }
                    }
                }

                //close the data stream before checking the data is valid
                dataStream.close();

                //assert valid file
                if (this.ev3MAC == null)
                    throw new IOException("Invalid yaml file - no MAC_ADDR_FIELD declared.");

                if (this.tamagotchiName == null)
                    throw new IOException("Invalid yaml file - no BOT_NAME declared.");

                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.isNewBot = !success;
    }

    /**
     * Gets the same file for the settings reader and writer
     *
     * @param context -> app context
     * @return settings file
     * @since 1.0
     */
    private static File getFile(Context context) {
        File dir = context.getFilesDir();
        File settingsFile = new File(dir, "settings.yaml");
        return settingsFile;
    }

    /**
     * @return EV3 mac address
     * @since 1.0
     */
    public String getEv3MAC() {
        return ev3MAC;
    }

    /**
     * Sets the mac address
     *
     * @param ev3MAC -> address to set
     * @since 1.0
     */
    public void setEv3MAC(String ev3MAC) {
        this.ev3MAC = ev3MAC;
    }

    /**
     * @return name of the robot
     * @since 1.0
     */
    public String getTamagotchiName() {
        return tamagotchiName;
    }

    /**
     * Sets the bot name
     *
     * @param tamagotchiName -> the name to set
     * @since 1.0
     */
    public void setTamagotchiName(String tamagotchiName) {
        this.tamagotchiName = tamagotchiName;
    }

    /**
     * @return if the settings object represents a new bot (null values)
     * @since 1.0
     */
    public boolean isNewBot() {
        return isNewBot;
    }

    /**
     * Saves the settings file
     *
     * @param context -> app context
     * @throws IOException -> thrown if an error occurs whilst making the file
     * @since 1.0
     */
    public void saveSettings(Context context) throws IOException {
        File settingsFile = getFile(context);

        if (settingsFile.exists())
            settingsFile.delete();
        settingsFile.createNewFile();

        DataOutputStream dataOutputStream = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(settingsFile)));

        dataOutputStream.writeChars(String.format("%s=%s\n%s=%s", MAC_ADDR_FIELD, this.ev3MAC,
                BOT_NAME, this.tamagotchiName));
        dataOutputStream.close();
    }

}
