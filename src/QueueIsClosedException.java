
public class QueueIsClosedException extends RuntimeException
{
    public QueueIsClosedException()
    {
        super("Queue is closed!");
    }

}