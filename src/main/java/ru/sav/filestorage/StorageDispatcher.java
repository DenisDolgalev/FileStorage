package ru.sav.filestorage;

import ru.sav.filestorage.impl.FileStorageImpl;
import ru.sav.filestorage.impl.StorageException;
import ru.sav.filestorage.impl.StorageImpls;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.*;

public class StorageDispatcher {
    private final Path path;
    private final ExecutorService executorService;
    private Boolean isDispatcherStopped = false;
    StorageImpls engine;

    public StorageDispatcher(Path path, Integer threadsCount, StorageImpls... optEngine) throws StorageException {
        if (!Files.isDirectory(path)) {
            throw new StorageException("Work directory " + path + " not found!");
        }
        this.path = path;
        executorService = Executors.newFixedThreadPool(threadsCount);

        if (optEngine.length > 0) {
            engine = optEngine[0];
        } else {
            //Default engine
            engine = new FileStorageImpl();
        }
    }

    public Optional<String> readKey(String key) throws DispatcherException {
        if (isDispatcherStopped) {
            throw  new DispatcherException("Read operation: dispatcher thread pool has been stopped");
        }
        Future<Optional<String>> fut = executorService.submit( () -> {
            try {
                return engine.readKey(path, key);
            } catch (StorageException e) {
                throw new RuntimeException(e);
            }
        });
        Optional<String> result;
        try {
            result = fut.get();
        } catch (Exception ex) {
            throw new DispatcherException("Could not read key " + key, ex);
        }

        return result;
    }

    public Optional<String> writeValueOptKey(String value, String... key) throws DispatcherException {
        if (isDispatcherStopped) {
            throw  new DispatcherException("Write operation: dispatcher thread pool has been stopped");
        }
        Future<String> fut = executorService.submit( () -> {
            try {
                return engine.upsertKey(path, value, key);
            } catch (StorageException e) {
                //System.out.println("Exception in a thread.");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });

        String result;
        try {
            result = fut.get();
        } catch (Exception ex) {
            throw new DispatcherException("Could not write key!", ex);
        }
        return Optional.of(result);
    }

    public void stopDispatcherThreadPool() {
        if (!isDispatcherStopped) {
            executorService.shutdown();
            isDispatcherStopped = true;
            try {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException iex) {
                //do nothing
            }
        }
    }

}
