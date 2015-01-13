import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 	Some notes:
 *  The assignment said each worker needed to make its own list.
 *  Ran into heap space issues long before it'd max CPU utilization, so only using 1 list.
 *  
 *  Using Atomic Integer because everyone else used synchronized, just want to try something different.
 *  
 *  Used ExecutorService as the framework is made to optimize multiple threads.
 *  I used to mostly to learn different parts of the Java API and try different things / reference.
 *  
 * @author Daniel Wester
 */

public class Threads implements Runnable {

	// Size of the list that'll be searched.
	public static final int SIZE = 50000;
	public static LinkedList<Integer> list = new LinkedList<Integer>();

	// Made volatile since I set the transaction back to 0, and it could cause issues that way.
	public static volatile AtomicInteger transaction = new AtomicInteger();

	// How long the worker will sleep before doing another transaction.
	public int sleepWorker = 20;
	private Random random = new Random();

	public void run() {
		while(true) {
			try {
				// Searches through the list for a number.
				list.contains(random.nextInt(SIZE)+1);
				// Increases after the search has been done.
				transaction.incrementAndGet();
				// Sleeps for a set time.
				Thread.sleep(sleepWorker);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args){

		int totalThreads = 1000; // Total threads that'll be running.
		int threadsCreated = 10; // Threads created after each sleep.
		int threads = 0; // Current count of threads running.
		int sleepMaster = 1000; // This sleep is for the Master, also how often additional worker will be added.

		long resetTime = 10000; // How often the reset and transaction count will trigger, in milliseconds.
		long currentTime;
		long lastTime;
		
		int count = 1; // How many times the screen has printed.

		int processorCount = Runtime.getRuntime().availableProcessors();
		System.out.println("Processor Count: " + processorCount);

		// Making a list with an ordered number by a certain size.
		for (Integer i = 1; i <= SIZE; i++){
			list.add(i);
		}

		// Creates a Thread pool for additional threads to be created.
		ExecutorService executor = Executors.newCachedThreadPool();

		// Sets the timer for resetting transaction.
		lastTime = new Date().getTime() + resetTime;

		while (true) {
			try {
				// Getting the current time.
				currentTime = new Date().getTime();

				// Comparing the time with the needed time to print and reset.
				if (currentTime > lastTime) {
					// Print out the transactions done, and reset while I have it.
					int done = transaction.getAndSet(0);

					System.out.println("Print times: " + count++ +
							"\nThreads: " + threads +
							"\nTransactions done: " + done + 
							// Just for additional info / testing.
							"\nTransactions (done / second): " + 
							(done / (((double)resetTime / 1000) * (double)threads)) + 
							"\n");
					
					// Resets the lastTime for the next transaction wipe.
					lastTime = new Date().getTime() + resetTime;
				}

				// Will add another thread to the total until the amount needed are made
				// Doing it this way so they slowly start growing more and more.
				if (threads < totalThreads){
					for (int i = 0; i < threadsCreated; i++) {
						executor.execute(new Threads());
					}
					threads = threads + threadsCreated;
					if (threads == totalThreads)
						System.out.println("All workers are made!!!");
				}
				
				// Sleep the thread for a set amount of time.
				Thread.sleep(sleepMaster);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
