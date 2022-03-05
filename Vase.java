import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

class Vase
{
  public static void main(String[] args)
  {
    int n = 100; // Number of guests.
    Queue queue = new Queue(n);
    
    Thread guests[] = new Thread[n];
    for (int i = 0; i < n; i++)
        guests[i] = new Thread(queue, Integer.toString(i+1));
    for (int i = 0; i < n; i++)
        guests[i].start();
  }
}

class Queue implements Runnable
{
  ALock lock;

  public Queue(int n)
  {
      lock = new ALock(n);
  }

  public void run()
  {
    boolean hasSeenVase = false;
    while(!hasSeenVase)
    {
        lock.lock();
        try
        {
            Thread.sleep(50); // Simulate time spent looking at vase.
            System.out.println(String.format("Guest %2s has seen the vase.", Thread.currentThread().getName()));
        }
        catch(InterruptedException e)
        {
            // ignore sleep command
        }
        finally
        {
            lock.unlock();
            hasSeenVase = true;
        }
    }
  }
}

class ALock implements Lock {

    // Needed to override these Lock methods to avoid errors.
    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {}

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    ThreadLocal<Integer> mySlotIndex = new ThreadLocal<Integer> (){
        protected Integer initialValue() { 
            return 0;
        }
    };
    AtomicInteger tail;
    volatile boolean[] flag;
    int size;
    public ALock(int capacity) {
        size = capacity;
        tail = new AtomicInteger(0); 
        flag = new boolean[capacity]; 
        flag[0] = true;
    }
    public void lock() {
        int slot = tail.getAndIncrement() % size; 
        mySlotIndex.set(slot);
        while (!flag[slot]) {};
    }
    public void unlock() {
        int slot = mySlotIndex.get(); 
        flag[slot] = false;
        flag[(slot + 1) % size] = true;
    } 
}