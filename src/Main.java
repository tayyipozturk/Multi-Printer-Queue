import java.util.*;

public class Main
{
    static class Producer implements Runnable
    {
        private int id;
        private PrinterRoom room;
        private PrintItem.PrintType type;

        public Producer(int id, PrinterRoom room, PrintItem.PrintType type)
        {
            this.id = id;
            this.room = room;
            this.type = type;
            SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, this.id,
                    String.format(SyncLogger.FORMAT_PRODUCER_LAUNCH, this.id));
        }

        @Override
        public void run() {
            PrintItem item = new PrintItem(new Random().nextInt(1000), type, id);
            if(room.SubmitPrint(item, id)) {
                SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, id,
                        String.format(SyncLogger.FORMAT_TERMINATING, id));
            }
            else{
                SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, id,
                        String.format(SyncLogger.FORMAT_ROOM_CLOSED, item));
                SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, id,
                        String.format(SyncLogger.FORMAT_TERMINATING, id));
            }
        }

        public void join()
        {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void test() throws InterruptedException{
        PrinterRoom room = new PrinterRoom(2, 8);
        List<Producer> producers = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            PrintItem.PrintType type = (i % 2 == 0) ? PrintItem.PrintType.STUDENT : PrintItem.PrintType.INSTRUCTOR;
            producers.add(new Producer(i, room, type));
        }

        for (Producer p : producers) {
            Thread t = new Thread(p);
            t.start();
            threads.add(t);
        }

        Thread.sleep((long)(3 * 1000));

        SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                "Closing Room");

        room.CloseRoom();

        for (Thread t : threads) {
            t.join();
        }

        SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                "Room is Closed");
    }

    public static void main(String args[]) throws InterruptedException {
        test();
    }
}