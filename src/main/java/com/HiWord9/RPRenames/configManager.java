package com.HiWord9.RPRenames;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class configManager {

    private static ArrayList<Rename> theList;

    public static String configPath = "config/renames/";
    public static File configFolder = new File(configPath);

    public static void jsonManage() {
        if (configFolder.exists()) {
            System.out.println("Config's folder is already exist. Starting recreate");
            configDeleter(configPath);
            startConfigCreate();
        } else {
            startConfigCreate();
        }
    }

    private static void startConfigCreate() {
        configFolder.mkdir();

        String resourcePacks = null;
        try {
            File file = new File("options.txt");
            FileReader options;
            options = new FileReader(file.getAbsolutePath());
            Properties p = new Properties();
            p.load(options);
            resourcePacks = p.getProperty("resourcePacks");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int h = 0;
        String currentRP = "";
        while (h < resourcePacks.length()) {
            if (String.valueOf(resourcePacks.charAt(h)).equals("[") || String.valueOf(resourcePacks.charAt(h)).equals("\"")) {
                h++;
            } else {
                if (String.valueOf(resourcePacks.charAt(h-1)).equals("\"")) {
                    while (h < resourcePacks.length()) {
                        currentRP = currentRP + resourcePacks.charAt(h);
                        h++;
                        if (String.valueOf(resourcePacks.charAt(h)).equals("\"")) {
                            if (currentRP.startsWith("file/")) {
                                currentRP = currentRP.substring(5);
                                configCreator("resourcepacks/" + currentRP + "/assets/minecraft/optifine/cit");
                            }
                            h = h + 3;
                            currentRP = "";
                        }
                    }
                }
            }
        }
    }

    public static void configCreator(String directoryName) {

        File directory = new File(directoryName);

        File[] fList = directory.listFiles();

        if (directory.exists()) {
            for (File file : fList) {
                if (file.isFile()) {
                    if (file.getAbsolutePath().endsWith(".properties")) {
                        try {
                            FileReader properties = new FileReader(file.getAbsolutePath());
                            Properties p = new Properties();
                            p.load(properties);

                            String items = p.getProperty("matchItems");
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

                                File currentFile = new File(configPath + item + ".json");
                                boolean nameExist = false;
                                if (currentFile.exists() && p.getProperty("nbt.display.Name") != null) {
                                    Rename alreadyExist = configRead(currentFile);
                                    String[] ae = alreadyExist.getName();
                                    for (String s : ae) {
                                        if (getFirstName(p.getProperty("nbt.display.Name")).equals(s)) {
                                            nameExist = true;
                                        }
                                    }
                                    if (!nameExist) {
                                        int AEsize = ae.length;
                                        String[] newConfig = new String[AEsize + 1];
                                        int h = 0;
                                        while (h < AEsize) {
                                            newConfig[h] = ae[h];
                                            h++;
                                        }
                                        newConfig[h] = getFirstName(p.getProperty("nbt.display.Name"));

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
                                        try {
                                            System.out.println("Current file does not exist, creating new one");
                                            ArrayList<Rename> listNames = new ArrayList<>();
                                            Rename name1 = new Rename(new String[]{getFirstName(p.getProperty("nbt.display.Name"))});
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
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (file.isDirectory()) {
                    configCreator(file.getAbsolutePath());
                }
            }
        }
    }

    public static void configDeleter(String directoryName) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    public static Rename configRead(File theFile) {
        try {
            FileReader fileReader = new FileReader(theFile);
            Type type = new TypeToken<ArrayList<Rename>>(){}.getType();
            Gson gson = new Gson();
            theList = gson.fromJson(fileReader, type);
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return theList.get(0);
    }

    public static String getFirstName(String nbtDisplayName) {
        int a = 0;
        while (!(String.valueOf(nbtDisplayName.charAt(a)).equals(":")) && a != nbtDisplayName.length()-1) {
            a++;
        }
        nbtDisplayName = nbtDisplayName.substring(a+1);
        if (String.valueOf(nbtDisplayName.charAt(0)).equals("(")) {
            nbtDisplayName = nbtDisplayName.substring(1);
        }
        int b = 0;
        while (!(String.valueOf(nbtDisplayName.charAt(b)).equals("|")) && b != nbtDisplayName.length()-1) {
            b++;
        }
        if (String.valueOf(nbtDisplayName.charAt(b)).equals("|") || String.valueOf(nbtDisplayName.charAt(b)).equals(")")) {
            nbtDisplayName = nbtDisplayName.substring(0,b);
        }
        return nbtDisplayName;
    }
}
