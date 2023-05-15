package com.HiWord9.RPRenames.configGeneration;

import com.HiWord9.RPRenames.Rename;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public class CITConfig {

    public static void propertiesToJson(Properties p, String outputPath) {
        String items = p.getProperty("matchItems");
        if (items == null) {
            items = p.getProperty("items");
        }
        if (items != null) {
            while (items.endsWith(" ")) {
                items = items.substring(0, items.length() - 1);
            }
            String item = null;
            boolean finish = false;
            while (!finish) {
                int i = 0;
                while (i < items.length()) {
                    if (String.valueOf(items.charAt(i)).equals(" ")) {
                        item = items.substring(0, i);
                        items = items.substring(i + 1);
                        finish = false;
                        break;
                    }
                    i++;
                    finish = true;
                }
                if (finish) {
                    item = items;
                }

                item = Objects.requireNonNull(item).replace("minecraft:", "");

                File currentFile = new File(outputPath + item + ".json");
                if (currentFile.exists() && p.getProperty("nbt.display.Name") != null) {
                    Rename alreadyExist = ConfigManager.configRead(currentFile);
                    String[] ae = alreadyExist.getName();
                    if (!Arrays.stream(ae).toList().contains(ConfigManager.getFirstName(p.getProperty("nbt.display.Name")))) {
                        int AEsize = ae.length;
                        String[] newConfig = new String[AEsize + 1];
                        int h = 0;
                        while (h < AEsize) {
                            newConfig[h] = ae[h];
                            h++;
                        }
                        newConfig[h] = ConfigManager.getFirstName(p.getProperty("nbt.display.Name"));

                        Rename newRename = new Rename(newConfig);
                        ArrayList<Rename> listFiles = new ArrayList<>();
                        listFiles.add(newRename);

                        try {
                            FileWriter fileWriter = new FileWriter(currentFile);
                            Gson gson = new Gson();
                            gson.toJson(listFiles, fileWriter);
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (p.getProperty("nbt.display.Name") != null) {
                        new File(outputPath).mkdirs();
                        try {
                            System.out.println("[RPR] Created new file for config: " + outputPath + item + ".json");
                            ArrayList<Rename> listNames = new ArrayList<>();
                            Rename name1 = new Rename(new String[]{ConfigManager.getFirstName(p.getProperty("nbt.display.Name"))});
                            listNames.add(name1);
                            FileWriter fileWriter = new FileWriter(currentFile);
                            Gson gson = new Gson();
                            gson.toJson(listNames, fileWriter);
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
