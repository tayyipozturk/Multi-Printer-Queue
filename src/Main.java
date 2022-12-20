import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main
{
   static class Producer implements Runnable {
       int id;
       PrintItem item;
       PrinterRoom room;

       public Producer(int id, PrintItem item, PrinterRoom room) {
           SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, id,
                   String.format(SyncLogger.FORMAT_PRODUCER_LAUNCH, id));
           this.id = id;
           this.item = item;
           this.room = room;
       }

       public void run() {
           try {
               room.SubmitPrint(item, id);
           } catch (QueueIsClosedExecption e) {
               e.printStackTrace();
           }
       }

       public void join() {

       }
   }

    public static void main(String args[]) throws InterruptedException {
        int printerCount = 2;
        int maxElementCount = 8;

        PrinterRoom room = new PrinterRoom(printerCount, maxElementCount);
        while (true) {
            Producer p1 = new Producer(0, new PrintItem(100, PrintItem.PrintType.INSTRUCTOR, 0), room);
            Producer p2 = new Producer(1, new PrintItem(50, PrintItem.PrintType.STUDENT, 1), room);
            Producer p3 = new Producer(2, new PrintItem(50, PrintItem.PrintType.STUDENT, 2), room);
            Producer p4 = new Producer(3, new PrintItem(2950, PrintItem.PrintType.INSTRUCTOR, 3), room);
            Producer p5 = new Producer(4, new PrintItem(50, PrintItem.PrintType.STUDENT, 4), room);
            ArrayList<Producer> producers = new ArrayList<>();
            producers.add(p1);
            producers.add(p2);
            producers.add(p3);
            producers.add(p4);
            producers.add(p5);
            ExecutorService executor = Executors.newFixedThreadPool(2);
            for (Producer value : producers) {
                value.run();
            }

            Thread.sleep((long)(3 * 1000));
            SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                    "Closing Room");
            room.CloseRoom();

            Producer p6 = new Producer(6, new PrintItem(300, PrintItem.PrintType.INSTRUCTOR, 5), room);
            Producer p7 = new Producer(7, new PrintItem(500, PrintItem.PrintType.STUDENT, 6), room);
            Producer p8 = new Producer(8, new PrintItem(50, PrintItem.PrintType.STUDENT, 7), room);
            Producer p9 = new Producer(9, new PrintItem(100, PrintItem.PrintType.INSTRUCTOR, 8), room);
            producers = new ArrayList<>();
            producers.add(p6);
            producers.add(p7);
            producers.add(p8);
            producers.add(p9);

            for (Producer producer : producers) {
                producer.run();
            }

            SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                    "Room is Closed");

            break;
        }
   }
}