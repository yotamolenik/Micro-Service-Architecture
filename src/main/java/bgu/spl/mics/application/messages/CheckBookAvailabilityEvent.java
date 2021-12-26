package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.*;

public class CheckBookAvailabilityEvent<T> implements Event<T> {

    private String senderName;
    private OrderResult orderResult;
    private String bookTitle;
    private int bookPrice;


    public CheckBookAvailabilityEvent(String senderName){
        this.senderName = senderName;
    }

    public OrderResult getOrderResult() {
        return orderResult;
    }

    public void setOrderResult(OrderResult orderResult) {
        this.orderResult = orderResult;

    }

    public int getBookPrice() {
        return bookPrice;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}
