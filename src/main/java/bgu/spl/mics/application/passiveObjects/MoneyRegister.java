package bgu.spl.mics.application.passiveObjects;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing the store finance management. 
 * It should hold a list of receipts issued by the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class MoneyRegister {
	private ConcurrentLinkedDeque<OrderReceipt> recipts;
	private int fortune;

	private static class MoneyRegisterHolder {
		private static MoneyRegister instance = new MoneyRegister();
	}
	private MoneyRegister() {
		recipts = new ConcurrentLinkedDeque<>();
		fortune = 0;
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static MoneyRegister getInstance() {
		return MoneyRegisterHolder.instance;
	}
	
	/**
     * Saves an order receipt in the money register.
     * <p>   
     * @param r		The receipt to save in the money register.
     */
	public void file (OrderReceipt r) {
		recipts.add(r);
		fortune = fortune + r.getPrice();
	}
	
	/**
     * Retrieves the current total earnings of the store.  
     */
	public int getTotalEarnings() {
		return fortune;
	}
	
	/**
     * Charges the credit card of the customer a certain amount of money.
     * <p>
     * @param amount 	amount to charge
     */
	public void chargeCreditCard(Customer c, int amount) {
		c.setavAailableCreditAmount(c.getAvailableCreditAmount() - amount);
	}
	
	/**
     * Prints to a file named @filename a serialized object List<OrderReceipt> which holds all the order receipts 
     * currently in the MoneyRegister
     * This method is called by the main method in order to generate the output.
     */
	public void printOrderReceipts(String filename) {
		FileOutputStream customersFile = null;
		try {
			customersFile = new FileOutputStream(filename);
			try {
				ObjectOutputStream output = new ObjectOutputStream(customersFile);
				output.writeObject(recipts);
				output.close();
				customersFile.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
