package me.eddiep.tinyhttp.net.http;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

public class MimeTypes {
    private static HashMap<String, String> mime = new HashMap<String, String>();
    private static boolean loaded;

    public static void loadMimeTypes() {
        if (loaded)
            throw new IllegalStateException("loadMimeTypes has already been invoked!");

        loaded = true;
        String[] lines = new Scanner(MimeTypes.class.getClassLoader().getResourceAsStream("mime.types"), "UTF-8").useDelimiter("\\A").next().split("\n");

        for (String line : lines) {
            if (line.startsWith("#"))
                continue;

            String[] temp = line.split("\t");

            String type = temp[0];
            String ext = null;
            for (int i = 1; i < temp.length; i++) {
                if (temp[i].equals(""))
                    continue;
                ext = temp[i].trim();
                break;
            }

            if (ext == null)
                continue;

            String[] possibleExts = ext.split(" ");
            for (String s : possibleExts) {
                mime.put(s, type);
            }
        }
    }

    public static String getMimeTypeFor(File file) {
        String path = file.getAbsolutePath();

        String[] temp = path.split("\\.");

        String ext = temp[temp.length - 1];


        return mime.get(ext);
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
