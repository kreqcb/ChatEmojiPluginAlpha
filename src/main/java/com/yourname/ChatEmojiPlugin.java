package com.yourname;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.bukkit.configuration.ConfigurationSection;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;

public class ChatEmojiPlugin extends JavaPlugin implements Listener {

    private Map<String, String> emojiMap = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Запускаем ChatEmojiPlugin...");
        try {
            getLogger().info("Сохраняем конфиг...");
            saveDefaultConfig();
            getLogger().info("Конфиг сохранён!");

            getLogger().info("Вызываем loadEmojis()...");
            loadEmojis();
            getLogger().info("loadEmojis() завершён!");

            getLogger().info("Регистрируем слушатель событий...");
            getServer().getPluginManager().registerEvents(this, this);
            getLogger().info("Слушатель зарегистрирован!");

            getLogger().info("Вызываем generateResourcePack()...");
            generateResourcePack();
            getLogger().info("generateResourcePack() завершён!");

            getLogger().info("ChatEmojiPlugin успешно запущен!");
        } catch (Exception e) {
            getLogger().severe("Ошибка при запуске плагина: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEmojis() {
        getLogger().info("Загружаем эмодзи из конфига...");
        try {
            ConfigurationSection emojisSection = getConfig().getConfigurationSection("emojis");
            if (emojisSection == null) {
                getLogger().warning("Секция 'emojis' не найдена в config.yml!");
                return;
            }
            for (String key : emojisSection.getKeys(false)) {
                String symbol = getConfig().getString("emojis." + key + ".symbol");
                if (symbol == null) {
                    getLogger().warning("Символ для эмодзи '" + key + "' не найден!");
                    continue;
                }
                emojiMap.put(":" + key + ":", symbol);
                getLogger().info("Добавлен эмодзи: " + key + " -> " + symbol);
            }
            getLogger().info("Эмодзи успешно загружены! Всего: " + emojiMap.size());
        } catch (Exception e) {
            getLogger().severe("Ошибка при загрузке эмодзи: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        getLogger().info("Обработка сообщения в чате...");
        try {
            String message = event.getMessage();
            getLogger().info("Исходное сообщение: " + message);

            for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }

            getLogger().info("Сообщение после замены: " + message);
            event.setMessage(message);
        } catch (Exception e) {
            getLogger().severe("Ошибка при обработке сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateResourcePack() {
        getLogger().info("Генерируем ресурспак...");
        try {
            File tempDir = new File(getDataFolder(), "temp");
            if (tempDir.exists()) {
                deleteDirectory(tempDir);
                getLogger().info("Очищена временная папка: " + tempDir.getAbsolutePath());
            }
            tempDir.mkdirs();
            getLogger().info("Создана временная папка: " + tempDir.getAbsolutePath());

            File assetsDir = new File(tempDir, "assets");
            File minecraftDir = new File(assetsDir, "minecraft");
            File texturesDir = new File(minecraftDir, "textures");
            File fontTexturesDir = new File(texturesDir, "font");
            File fontDir = new File(minecraftDir, "font");

            assetsDir.mkdirs();
            minecraftDir.mkdirs();
            texturesDir.mkdirs();
            fontTexturesDir.mkdirs();
            fontDir.mkdirs();
            getLogger().info("Создана структура папок для ресурспака");

            File imagesDir = new File(getDataFolder(), "images");
            if (!imagesDir.exists()) imagesDir.mkdirs();
            getLogger().info("Папка для картинок: " + imagesDir.getAbsolutePath());

            StringBuilder providers = new StringBuilder();
            providers.append("[\n");
            ConfigurationSection emojis = getConfig().getConfigurationSection("emojis");
            if (emojis == null) {
                getLogger().warning("Секция 'emojis' не найдена при генерации ресурспака!");
                return;
            }
            boolean first = true;
            for (String key : emojis.getKeys(false)) {
                String image = emojis.getString(key + ".image");
                String symbol = emojis.getString(key + ".symbol");

                if (image == null || symbol == null) {
                    getLogger().warning("Неполные данные для эмодзи '" + key + "': image=" + image + ", symbol=" + symbol);
                    continue;
                }

                File imageFile = new File(imagesDir, image);
                if (imageFile.exists()) {
                    Files.copy(imageFile.toPath(), new File(fontTexturesDir, image).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Скопирована картинка: " + image);
                } else {
                    getLogger().warning("Картинка не найдена: " + imageFile.getAbsolutePath());
                }

                String escapedSymbol = escapeUnicode(symbol);

                if (!first) providers.append(",\n");
                providers.append("        {\n");
                providers.append("            \"type\": \"bitmap\",\n");
                providers.append("            \"file\": \"minecraft:font/").append(image).append("\",\n");
                providers.append("            \"height\": 8,\n");
                providers.append("            \"ascent\": 7,\n");
                providers.append("            \"chars\": [\n");
                providers.append("                \"").append(escapedSymbol).append("\"\n");
                providers.append("            ]\n");
                providers.append("        }");
                first = false;
            }
            providers.append("\n    ]");

            File defaultJson = new File(fontDir, "default.json");
			String defaultJsonContent = "{\n    \"providers\": " + providers.toString() + "\n}";
			try (FileWriter writer = new FileWriter(defaultJson)) {
			writer.write(defaultJsonContent);
			}
			getLogger().info("Создан default.json");
			getLogger().info("Содержимое default.json:\n" + defaultJsonContent);

            File packMcmeta = new File(tempDir, "pack.mcmeta");
            try (FileWriter writer = new FileWriter(packMcmeta)) {
                writer.write("{\n    \"pack\": {\n        \"pack_format\": 15,\n        \"description\": \"Кастомные эмодзи для чата\"\n    }\n}");
            }
            getLogger().info("Создан pack.mcmeta");

            File zipFile = new File(getDataFolder(), "EmojiPack.zip");
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                addToZip(tempDir, "", zos);
            }
            getLogger().info("Создан ZIP: " + zipFile.getAbsolutePath());

            updateServerProperties(zipFile);
            getLogger().info("Обновлён server.properties");

            deleteDirectory(tempDir);
            getLogger().info("Удалены временные файлы");

            getLogger().info("Ресурспак успешно сгенерирован: " + zipFile.getAbsolutePath());
        } catch (Exception e) {
            getLogger().severe("Ошибка при генерации ресурспака: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeUnicode(String input) {
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 128) {
                escaped.append(String.format("\\u%04X", (int) c));
            } else {
                escaped.append(c);
            }
        }
        return escaped.toString();
    }

    private void addToZip(File directory, String parent, ZipOutputStream zos) throws IOException {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addToZip(file, parent + file.getName() + "/", zos);
                continue;
            }
            zos.putNextEntry(new ZipEntry(parent + file.getName()));
            Files.copy(file.toPath(), zos);
            zos.closeEntry();
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) deleteDirectory(file);
                else file.delete();
            }
        }
        directory.delete();
    }

    private String uploadToDropbox(File zipFile) throws Exception {
        getLogger().info("Загружаем EmojiPack.zip на Dropbox...");
        String accessToken = "sl.u.AFqhYClnxUDvUOdzJ6uDxks4okIjttQQe46MbAbUhTn51pYM5lcAd4kwyrt3T0MWHe5wgfEZ5nuuTXf8-0cAoyoO7cHje3IVLZ9wsCa1qbJhb8wHH5UVDf5jr27pg8Rrn2fvZI1CcJqKrRE0dKnI67YkDHnUt0kKKTHzshifi_gFhknAAnzoRIjyPs4Y3I0OEDtGbfPGz0FgQT0h32t7bh5B6oo9s7VVc0f1L9oUMAmsEs5HthDcaWwNTCqIfs6p_YJe-O38AKZvX-S-f2cNiTKp8cFfOK-3Ubab9bPh2H_-Vf7tDVUFmKLvpWt2FT1zz_bKLTTGYfC2k9N8uCf5BHx1A1boETQ5z36M3Ggj7-XojPz0ICt-Vc-OH-r8nPpb2PIx6WLZodqChto8jeQnRiL7y1rU2dBZkhGgWWS6-coqNMNktH9NHVgbUuuz-JRzhdjDcyOPIHxn9paZD2e-uyB-MRYcscrXmSBqG-cePfubbQgsxFionky6bDxp6yrekVFub-A7IhxhHVUPdC0SPacyi9rlxHQO5_c9Bj3L1qOnJekKHjaYApGVNOfudUMpGervT2w0zoEM9JveXTz6mEbIdD2aRjoDOLwkaniy8UkQaNU7bro0TXPNfb-6orEAAz0YLkZJQQ7yZLjBcs8uaF6yL6Ox6cejl8T8zDwE9KtSZfCKmHSkmiHYnPaV3NC8PBXYWPrtdKFf6CCZO84teSf65fjGZBrHEAUUufMTImWhCbjo99vr9pm_NMg_DPLuQvgyJE5_-fVAsDF4SNHNFQpKkrxgZlhaa8e4f5S49CGfqJFSC5Jwqq2pDx3KqTQHJ2c7uSTP4TP5ZjKqXBPlXly5xArpFWshfMQA7gbGlipekV0hEad91gyhvB3_V6FmbTHGDzZTkchdVOvCU_WrIA0OheDsKA48z29ThJxrf_NtrTnhnry0rvNrIDgUmVgAvqtoZxfoNxqKHcBvO1L-iahEXDwtD_yN7kUvW6_IRRPv3DQQjmb3E4KxRLvF_a9PCv70xSZp1R0eiBfywnkdOTlwuC6eTdqnpMSJ1oE9LsUbqjd50cYUHPLkfMWQR_78FjHfy4k3w3V1ZdJHLtK-VXm4xCsixlhNF2c9fQwqGWlCBon2pXfpBLMdk-4UUMOy3WIjnNXHfyl6Sf-agwxFac4tX5Uv5gllVzXsAAlw4YAUlohp4eYCPV1c73ZWSo8OzZEw1YUuHE8DQdajUQBTjSYutSKOjp8-NXX2IcAoh7VoE96YCxzQ_3nS2of9avsJhTcmeM7UACs3v6rXwt2RrZr-iUlgFVvLMEinLMpmVJnvDYT92vZ19ofq1HqLL9NcmN_F0WVqbhMQcJsh5ZueJ6CcAmC5igky5ZGOZdmMNZX_Sz11HmcHkg-rmOuOYY9pUDU"; // Замени на свой токен
        DbxRequestConfig config = DbxRequestConfig.newBuilder("ChatEmojiPlugin").build();
        DbxClientV2 client = new DbxClientV2(config, accessToken);

        try (FileInputStream fis = new FileInputStream(zipFile)) {
            client.files().uploadBuilder("/EmojiPack.zip")
                   .withMode(WriteMode.OVERWRITE)
                   .uploadAndFinish(fis);
        }
        getLogger().info("Файл загружен на Dropbox!");

        try {
            ListSharedLinksResult links = client.sharing().listSharedLinksBuilder()
                    .withPath("/EmojiPack.zip")
                    .withDirectOnly(true)
                    .start();
            if (links.getLinks().size() > 0) {
                String url = links.getLinks().get(0).getUrl().replace("dl=0", "dl=1");
                getLogger().info("Использована существующая ссылка: " + url);
                return url;
            }
        } catch (Exception e) {
            getLogger().warning("Ошибка при получении существующей ссылки: " + e.getMessage());
        }

        SharedLinkMetadata linkMetadata = client.sharing().createSharedLinkWithSettings("/EmojiPack.zip");
        String url = linkMetadata.getUrl().replace("dl=0", "dl=1");
        getLogger().info("Создана новая ссылка: " + url);
        return url;
    }

    private void updateServerProperties(File zipFile) throws Exception {
        getLogger().info("Обновляем server.properties...");
        String dropboxUrl = uploadToDropbox(zipFile);

        File serverProperties = new File("server.properties");
        if (!serverProperties.exists()) {
            getLogger().warning("server.properties не найден!");
            return;
        }

        StringBuilder content = new StringBuilder();
        boolean found = false;
        for (String line : Files.readAllLines(serverProperties.toPath())) {
            if (line.startsWith("resource-pack=")) {
                content.append("resource-pack=").append(dropboxUrl).append("\n");
                found = true;
            } else {
                content.append(line).append("\n");
            }
        }
        if (!found) {
            content.append("resource-pack=").append(dropboxUrl).append("\n");
        }

        Files.write(serverProperties.toPath(), content.toString().getBytes());
        getLogger().info("server.properties обновлён!");
    }
}