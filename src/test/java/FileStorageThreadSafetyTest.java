import org.junit.jupiter.api.*;
import ru.sav.filestorage.StorageDispatcher;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileStorageThreadSafetyTest {

    StorageDispatcher dispatcher;
    Path workFolder = Paths.get("FileStorage");

    @BeforeAll
    void prepareFoldersAndFiles() {
        System.out.println("Running BeforeAll");
        if(Files.isDirectory(workFolder)) {
            assertDoesNotThrow(() ->
                    deleteWorkDirectoryRecursive(),
                    "IOException occured!");

        }
        assertDoesNotThrow(() ->
                Files.createDirectory(workFolder),
                "Work directory not created!");
    }

    @AfterAll
    void cleanEverything() {
        //Moved to @BeforeAll to view test results
        //assertDoesNotThrow(() -> Files.delete(workFolder), "IOException occured!");
    }

    @BeforeEach
    void createNewDispatcher() {
        assertDoesNotThrow(() ->
                dispatcher = new StorageDispatcher(workFolder, 10),
                "Could not create dispatcher!");
    }

    @AfterEach
    void stopDispatcher() {
        dispatcher.stopDispatcherThreadPool();
    }

    //Create and overwrite 2 keys (test1.txt and text2.txt)
    //20 Threads * 100 cycles * 4 overwrite/cycle = 8000 writes
    @Test
    void concurrencyToTheSameKeys() {
        List<Callable<String>> usersList = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            usersList.add(new SameKeysThreads(dispatcher, i));
        }

        long startTime = System.currentTimeMillis();

        ExecutorService exec = Executors.newFixedThreadPool(20);
        List<Future<String>> resultList = new LinkedList<>();
        assertDoesNotThrow(() -> {
                    for (Future<String> fut: exec.invokeAll(usersList)) {
                        resultList.add(fut);
                    }
                },
                "Could not start user threads!");

        exec.shutdown();
        assertDoesNotThrow(() ->
                exec.awaitTermination(4, TimeUnit.SECONDS),
                "Users thread pool was interrupted!");
        System.out.println("Concurrent test execution time: " + (System.currentTimeMillis() - startTime));

        //Checking users threads for an errors
        for (Future<String> fut: resultList) {
            assertDoesNotThrow(() -> fut.get(), "Error in a user's thread!");
        }

    }

    //Unique keys for each write
    //20 Threads * 400 writes = 8000 writes
    @Test
    void allNewKeys() {
        List<Callable<String>> usersList = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            usersList.add(new NewKeyThreads(dispatcher, i));
        }

        long startTime = System.currentTimeMillis();

        ExecutorService exec = Executors.newFixedThreadPool(20);
        List<Future<String>> resultList = new LinkedList<>();
        assertDoesNotThrow(() -> {
                    for (Future<String> fut: exec.invokeAll(usersList)) {
                        resultList.add(fut);
                    }
                },
                "Could not start user threads!");

        exec.shutdown();
        assertDoesNotThrow(() ->
                        exec.awaitTermination(4, TimeUnit.SECONDS),
                "Users thread pool was interrupted!");

        System.out.println("New keys test execution time: " + (System.currentTimeMillis() - startTime));

        //Checking users threads for an errors
        for (Future<String> fut: resultList) {
            assertDoesNotThrow(() -> fut.get(), "Error in a user's thread!");
        }
    }

    private void deleteWorkDirectoryRecursive() throws IOException {
        Files.walkFileTree(workFolder,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult postVisitDirectory(
                            Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                            Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }
}
