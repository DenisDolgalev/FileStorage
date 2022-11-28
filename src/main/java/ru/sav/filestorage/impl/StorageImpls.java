package ru.sav.filestorage.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface StorageImpls {
    boolean isKeyExists(Path path, String key);

    /**
     * Trying to read a value stored under the key and return it. If key is not found returns Optional.empty()
     * @param path
     * @param key
     * @return
     * @throws StorageException
     */
    Optional<String> readKey(Path path, String key) throws StorageException;

    /**
     * Updates (if optKey is passed) or creates new pseudo-unique key and stores value under that key.
     * @param path - work directory Path
     * @param value - value to store under the key name (provided or generated)
     * @param optKey - optional key name. If absent, then key name will be generated
     * @return - key that was used for storing value
     * @throws StorageException
     */
    String upsertKey(Path path, String value, String... optKey) throws StorageException;

}
