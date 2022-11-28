import ru.sav.filestorage.StorageDispatcher;

import java.util.concurrent.Callable;

public class NewKeyThreads implements Callable<String> {

    StorageDispatcher dispatcher;
    Integer threadNum;
    NewKeyThreads(StorageDispatcher dispatcher, Integer threadNum) {
        this.dispatcher = dispatcher;
        this.threadNum = threadNum;
    }

    @Override
    public String call() throws Exception {
        String valuePrefix = "Thread " + threadNum + ", write num: ";
        for (int i = 0; i < 400; i++) {
            dispatcher.writeValueOptKey( valuePrefix + i);
        }
        return null;
    }
}
