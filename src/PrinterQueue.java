import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PrinterQueue implements IMPMCQueue<PrintItem> {
    private static ArrayBlockingQueue<PrintItem> instructorQueue;
    private static ArrayBlockingQueue<PrintItem> studentQueue;
    private int size;
    private int count;
    private static boolean isClosed;

    private static Lock lock = new ReentrantLock();
    private static Condition notFull = lock.newCondition();
    private static Condition notEmpty = lock.newCondition();

    public PrinterQueue(int maxElementCount) {
        size = maxElementCount;
        instructorQueue = new ArrayBlockingQueue<>(size);
        studentQueue = new ArrayBlockingQueue<>(size);
        isClosed = false;
        count = 0;
    }

    @Override
    public void Add(PrintItem item) throws QueueIsClosedExecption {
        lock.lock();
        try {
            while (RemainingSize() == 0) {
                notFull.await();
            }
            if (isClosed) {
                throw new QueueIsClosedExecption();
            }
            if (item.getPrintType() == PrintItem.PrintType.INSTRUCTOR) {
                instructorQueue.add(item);
            } else {
                studentQueue.add(item);
            }
            count++;
            notEmpty.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public PrintItem Consume() throws QueueIsClosedExecption {
        lock.lock();
        try {
            while (RemainingSize() == size) {
                if (isClosed) {
                    throw new QueueIsClosedExecption();
                }
                notEmpty.await();
            }
            PrintItem item = null;
            if (instructorQueue.size() > 0) {
                item = instructorQueue.remove();
            } else if(studentQueue.size() > 0){
                item = studentQueue.remove();
            }
            count--;
            notFull.signal();
            return item;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public int RemainingSize() {
        return size - count;
    }

    @Override
    public void CloseQueue() {
        lock.lock();
        isClosed = true;
        lock.unlock();
    }
}