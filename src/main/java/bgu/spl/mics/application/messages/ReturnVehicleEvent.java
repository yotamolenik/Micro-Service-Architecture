package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class ReturnVehicleEvent<T> implements Event<T> {
    private DeliveryVehicle v;

    public ReturnVehicleEvent(DeliveryVehicle v) {
        this.v = v;
    }

    public void setV(DeliveryVehicle v) {
        this.v = v;
    }

    public DeliveryVehicle getV() {
        return v;
    }
}
