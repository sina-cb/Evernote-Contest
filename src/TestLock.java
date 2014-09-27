import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class TestLock implements Runnable{
	
	public static void main(String[] args) {
		TestLock obj = new TestLock();
		new Thread(obj).start();
	}

	static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	Lock readLock = lock.readLock();
	Lock writeLock = lock.writeLock();
	
	static String buffer = "";
	
	@Override
	public void run() {

		(new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					writeLock.lock();
					buffer = Calendar.getInstance().getTime().toString() + "   " + (new Random()).nextInt(1000);
					writeLock.unlock();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		})).start();
		
		(new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					writeLock.lock();
					buffer = "" + (new Random()).nextInt(1000);
					writeLock.unlock();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		})).start();

		(new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					readLock.lock();
					System.out.println(buffer);
					readLock.unlock();
				}				
			}
		})).start();
		
	}
	
	
	
}
