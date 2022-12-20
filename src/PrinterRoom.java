import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrinterRoom {
    private class Printer implements Runnable {
        int id;
        IMPMCQueue<PrintItem> queue;

        public Printer(int id, IMPMCQueue<PrintItem> roomQueue) {
            this.id = id;
            this.queue = roomQueue;
        }

        public void run() {
            while (true) {
                PrintItem item = roomQueue.Consume();
                item.print();
                SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, this.id,
                        String.format(SyncLogger.FORMAT_PRINT_DONE, item));
            }
        }
    }

    private IMPMCQueue<PrintItem> roomQueue;
    private final List<Printer> printers;

    public PrinterRoom(int printerCount, int maxElementCount)
    {
        // Instantiating the shared queue
        roomQueue = new PrinterQueue(maxElementCount);

        // Let's try streams
        // Printer creation automatically launches its thread
        printers = Collections.unmodifiableList(IntStream.range(0, printerCount)
                                                         .mapToObj(i -> new Printer(i, roomQueue))
                                                         .collect(Collectors.toList()));
        // Printers are launched using the same queue
        for(Printer p : printers) {
            new Thread(p).start();
            SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, p.id,
                    String.format(SyncLogger.FORMAT_PRINTER_LAUNCH, p.id));
        }
    }

    public boolean SubmitPrint(PrintItem item, int producerId)
    {
        SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerId,
                String.format(SyncLogger.FORMAT_ADD, item));;
        try{
            roomQueue.Add(item);
        }
        catch(QueueIsClosedExecption e){
            SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerId,
                    String.format(SyncLogger.FORMAT_ROOM_CLOSED, item));
            return false;
        }
        return true;
    }

    public void CloseRoom() {
        roomQueue.CloseQueue();
    }
}
