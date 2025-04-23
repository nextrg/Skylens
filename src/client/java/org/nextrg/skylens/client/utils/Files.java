package org.nextrg.skylens.client.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.nextrg.skylens.client.utils.Errors.logErr;

public class Files {
    public static JsonObject readJSONFromNeu(String path) {
        return readJSON("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO" + path);
    }
    public static JsonObject readJSON(String path) {
        JsonObject json = new JsonObject();
        try {
            URL url = new URI(path).toURL();
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = reader.read()) != -1) {
                sb.append((char) cp);
            }
            json = JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception e) {
            logErr(e, "Caught an error getting JSON from NEU-repo");
        }
        return json;
    }
    // Might be useful in the future, keeping it for later
    public static List<Path> readFolderFiles(String relativePath) {
        Path basePath = FabricLoader.getInstance().getGameDir().resolve(relativePath); // Make sure it's only in .minecraft folder, not anywhere else for safety
        List<Path> fileList = new ArrayList<>();
        try {
            java.nio.file.Files.createDirectories(basePath); // ensures the directory exists
            try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(basePath)) {
                for (Path path : stream) {
                    if (java.nio.file.Files.isRegularFile(path)) {
                        fileList.add(path);
                    }
                }
            }
        } catch (IOException e) {
            logErr(e, "Failed to read the file from directory");
        }
        return fileList;
    }
}
