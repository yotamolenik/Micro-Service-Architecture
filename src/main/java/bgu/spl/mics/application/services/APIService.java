package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;
import bgu.spl.mics.application.passiveObjects.OrderTick;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService {
    private List<OrderTick> orderScheduale;
    private int currentTick = 1;
    private int duration;
    private Customer customer;
    private ConcurrentLinkedDeque<Future<OrderReceipt>> futureReceipts;

    public APIService(Customer customer, String name) {
        super(name);
        this.customer = customer;
        this.orderScheduale = customer.getOrderSchedule();

    }

    @Override
    protected void initialize() {
        System.out.println("WebAPI service" + getName() + " started");

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            this.currentTick = tickBroadcast.getTick();
            this.duration= tickBroadcast.getDuration();
            if(tickBroadcast.getTick() == duration){
                this.terminate();
            }
        });

        while (currentTick <= duration) {     //this is why wee need duration//

            //while we haven't reached duration, for each order which fits the current tick,//
            // send it to selling service and restore the future receipt in "futureReceipts"//
            // after that try to resolve all the receipts//
            //repeat process//

            for (OrderTick orderTick : orderScheduale) {
                if (orderTick.getTick()== currentTick) {
                    Future<OrderReceipt> futureOrderReceipt = sendEvent(new BookOrderEvent(orderScheduale.get(orderTick.getTick()).getBookTitle(), customer, this.getName(), this.currentTick));
                    futureReceipts.add(futureOrderReceipt);
                        }
                    }
            for (Future<OrderReceipt> futureReceipt: futureReceipts) {
                if(!(futureReceipt.isDone() && futureReceipt.get() == null) ){
                if (futureReceipt != null && futureReceipt.isDone() ){
                    customer.getCustomerReceiptList().add(futureReceipt.get());
                    sendEvent(new DeliveryEvent(customer.getId(),customer.getAddress(),customer.getDistance(),this.getName()));
                    futureReceipts.remove(futureReceipt);
                }else{
                    if(currentTick == duration) {
                   if (futureReceipt != null) {
                       customer.getCustomerReceiptList().add(futureReceipt.get());
                       futureReceipts.remove(futureReceipt);
                       sendEvent(new DeliveryEvent(customer.getId(),customer.getAddress(),customer.getDistance(),this.getName()));
                   }

                    }
                }

                }else {
                    futureReceipts.remove(futureReceipt);   //if the futureReceipt result is null we should discard it//
                }

            }
        }
    }
}

