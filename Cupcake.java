import java.util.concurrent.atomic.AtomicBoolean;

public class Cupcake
{
  public static void main(String[] args)
  {
    int n = 100;
    Maze maze = new Maze(n);

    Thread guests[] = new Thread[n];
    for (int i = 0; i < n; i++)
        guests[i] = new Thread(maze, Integer.toString(i+1));
    for (int i = 0; i < n; i++)
        guests[i].start();
  }
}

class Maze implements Runnable
{
  AtomicBoolean mazeStillOccupied; // Is the maze (critical section) occupied by another guest (thread)?
  AtomicBoolean someoneStillHungry; // To keep the while loop going.
  AtomicBoolean cupcake; // A cupcake either is or isn't being served in the maze.
  int n; // number of guests
  int count = 0; // How many cupcakes have been eaten so far?

  public Maze(int n)
  {
    this.n = n;
    cupcake = new AtomicBoolean(true); // Starts true or the leader might count an extra one by accident at the beginning.
    someoneStillHungry = new AtomicBoolean(true);
    mazeStillOccupied = new AtomicBoolean(false);
  }
  
  public void run()
  {
    boolean hadCupcake = false; // Initially, the guest (thread) hasn't consumed anything.

    while(someoneStillHungry.get())
    {
        while(mazeStillOccupied.getAndSet(true)) {}; // spin lock

      try
      {
        if (cupcake.get())
        {
          if (!hadCupcake) // The guest hasn't had a cupcake yet, so eat this one.
          {
            hadCupcake = true; // Belly is full.
            cupcake.set(false);
            System.out.println(String.format("Guest %2s has eaten a cupcake.", Thread.currentThread().getName()));
          }
        }
        else
        {
          if(Thread.currentThread().getName().equals("1")) // "1" because we made the first thread the leader.
          {
            cupcake.set(true);
            count++;
            if (count == n)
            {
              System.out.println("It looks like everyone has eaten a cupcake. All done!");
              someoneStillHungry.set(false);
              break;
            }
          }
        }
      }
      finally
      {
        mazeStillOccupied.set(false);
      }
    }
  }
}