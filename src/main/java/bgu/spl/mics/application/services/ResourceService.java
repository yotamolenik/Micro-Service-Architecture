package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.ReturnVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.getVehicleEvent;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
//imports in order to compile
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.Inventory;

import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService {
    private  int currentTick = 1;
    private ConcurrentLinkedDeque<DeliveryVehicle> vehiclesToBeReleased;
    private ConcurrentLinkedDeque<Future<DeliveryVehicle>> futureVehicles;
    private int duration;

    public ResourceService(String name) {
        super(name);
        this.vehiclesToBeReleased = new ConcurrentLinkedDeque<>();
    }

    @Override
    protected void initialize() {

        System.out.println("ResourceService" + getName() + " started");
        subscribeEvent(getVehicleEvent.class, getVehicleEvent -> {
           complete(getVehicleEvent,ResourcesHolder.getInstance().acquireVehicle());

            System.out.println("ResourceService got a new getVehicle mission from " + getVehicleEvent.getSenderName() + "\n" +
                    "and sent a request for getting it back");



        });
        subscribeEvent(ReturnVehicleEvent.class, returnVehicleEvent -> {
            ResourcesHolder.getInstance().releaseVehicle(returnVehicleEvent.getV());
            System.out.println("vehicle was returned to Resource holder");
        });
        subscribeBroadcast(TickBroadcast.class, tickBroadcast->{
            this.currentTick = tickBroadcast.getTick();
            this.duration =tickBroadcast.getDuration();
            System.out.println("current currentTick at" + this.getName() + " " + currentTick);
        });

    }
}
