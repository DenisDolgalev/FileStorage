package ru.sav.filestorage.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FileStorageImpl implements StorageImpls {

    @Override
    public boolean isKeyExists(Path path, String key) {
        Path file = path.resolve(key);
        return Files.exists(file) && Files.isRegularFile(file);
    }

    //Function description is in interface StorageImpls
    public Optional<String> readKey(Path path, String key) throws StorageException {
        StringBuilder value = new StringBuilder();
        if (isKeyExists(path, key)) {
            List<String> keyData;
            //Reading is synchronized over String key to avoid simultaneous write and read (see writeKey)
            synchronized (key) {
                try {
                    keyData = Files.readAllLines(path.resolve(key), StandardCharsets.UTF_8);
                } catch (IOException ioe) {
                    throw new StorageException("Error reading key " + key, ioe);
                }
            }
            for (String line: keyData) {
                if (value.isEmpty()) {
                    value.append(line);
                } else {
                    value.append("\n").append(line);
                }
            }
            return Optional.of(value.toString());
        }
        return Optional.empty();
    }

    //Function description is in interface StorageImpls
    public String upsertKey(Path path, String value, String... optKey) throws StorageException{
        String key = "";
        if (optKey.length > 0) {
            key = optKey[0];
            if (isAllowedSymbolsOnly(key)) {
                writeKey(path, key, value);
            }  else {
                throw new StorageException("Wrong symbol set of key " + key);
            }
            return key;
        }
        key = createKey(path, value);
        return key;
    }

    private static String createKey(Path path, String value) throws StorageException {
        String key = FileNameGenerator.generateFileName();
        writeKey(path, key, value);
        return key;
    }

    private static void writeKey(Path path, String key, String value) throws StorageException{
        //Writing is synchronized over String key to avoid simultaneous write and read (see readKey)
        synchronized (key) {
            try {
                Files.writeString(path.resolve(key), value);
            } catch (IOException iex) {
                throw new StorageException("Error writing key: " + key, iex);
            }
        }
    }

    private Boolean isAllowedSymbolsOnly(String toCheck) {
        return !toCheck.isBlank() && toCheck.matches("^[0-9a-zA-Z _-[.]]*$");
    }
}
