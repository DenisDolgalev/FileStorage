import ru.sav.filestorage.StorageDispatcher;


import java.util.Optional;
import java.util.concurrent.Callable;

public class SameKeysThreads implements Callable<String> {

    StorageDispatcher dispatcher;
    Integer threadNum;
    SameKeysThreads(StorageDispatcher dispatcher, Integer threadNum) {
        this.dispatcher = dispatcher;
        this.threadNum = threadNum;
    }

    @Override
    public String call() throws Exception {
        //Store values to test1.txt and test2.txt
        //Check they are the same
        Optional<String> test1, test2;
        for (int i = 0; i < 100; i++) {
            dispatcher.writeValueOptKey("qwe", "test1.txt");
            dispatcher.writeValueOptKey("zxc", "test2.txt");
            dispatcher.writeValueOptKey("zxc", "test1.txt");
            dispatcher.writeValueOptKey("qwe", "test2.txt");

            test1 = dispatcher.readKey("test1.txt");
            test2 = dispatcher.readKey("test2.txt");
            if (!checkTestValues(test1) || !checkTestValues(test2)) {
                throw new Exception("Wrong values read!");
            }
        }
        //System.out.println("Thread " + String.valueOf(threadNum) + " finished");
        return null;
    }

    private boolean checkTestValues(Optional<String> value) {
        if (!value.isEmpty()) {
            String result = value.get();
            return result.equals("qwe") || result.equals("zxc");
        }
        return false;
    }

}
