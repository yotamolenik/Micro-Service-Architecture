package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class getVehicleEvent<T> implements Event<T> {

    private String senderName;

    public getVehicleEvent(String senderName){
        this.senderName = senderName;
    }


    public String getSenderName() {
        return senderName;
    }
}
