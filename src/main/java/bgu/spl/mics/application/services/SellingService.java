package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.messages.*;
/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{
private int currentTick = 1;
private int duration;
	public SellingService(String name) {
		super(name);

	}

	@Override
	protected void initialize() {
		System.out.println("SellingService" + getName() + " started");
		subscribeEvent(BookOrderEvent.class, bookOrderEvent -> {
			Future<Integer> bookPriceFuture = sendEvent(new CheckBookAvailabilityEvent<>(this.getName()));
			if(bookPriceFuture != null){    //maybe messageBuss returned null//
				if(!(bookPriceFuture.isDone() && bookPriceFuture.get() == null)){   //if the unregister issue//
				if (bookPriceFuture.get() != -1){  // if book is available(then it has a price)//
if(bookOrderEvent.getCustomer().getAvailableCreditAmount() >= bookPriceFuture.get()){  //if check if customer has enough money//
	Future<OrderResult> orderResultFuture = sendEvent(new TakeBookEvent(bookOrderEvent.getNameOfBook()));  //try to take book//
	if (orderResultFuture != null){    //maybe messageBuss returned null//
		if (!(orderResultFuture.isDone() && orderResultFuture.get() == null)) {   //if for the unregister issue//
			if (orderResultFuture.get() == OrderResult.SUCCSESFULLY_TAKEN) {  //book was taken//
				bookOrderEvent.getCustomer().setavAailableCreditAmount(bookOrderEvent.getCustomer().getAvailableCreditAmount()-bookPriceFuture.get());  //charge customer//
				//return receipt//
				complete(bookOrderEvent , new OrderReceipt(this.getName(), bookOrderEvent.getCustomer().getId(),bookOrderEvent.getNameOfBook(),currentTick,bookOrderEvent.getOrderTick()));
			}if(orderResultFuture.get() == OrderResult.NOT_IN_STOCK){
				complete(bookOrderEvent,null);  //if not in stock resolve null//
			}
		}
}else {
		complete(bookOrderEvent, null);   // if orderResultFuture is null//
	}
				}else {
					complete(bookOrderEvent, null);  // if customer doesn't have enough money//
				}

				}else{
					complete(bookOrderEvent,null);}   //if book is not in stock//
			}
			}else {
				complete(bookOrderEvent, null);   // if bookPriceFuture is null//
			}
		});
		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			this.currentTick = tickBroadcast.getTick();
			this.duration= tickBroadcast.getDuration();
			if(tickBroadcast.getTick() == duration){
				this.terminate();
			}
		});


		}

}
