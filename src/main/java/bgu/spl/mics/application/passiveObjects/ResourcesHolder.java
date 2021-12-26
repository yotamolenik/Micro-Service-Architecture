package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import javafx.util.Pair;

import java.util.concurrent.*;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {

    private ConcurrentLinkedDeque<DeliveryVehicle> availableVehicles;
    private ConcurrentLinkedDeque<Future<DeliveryVehicle>> futures;
    private ResourcesHolder() {
        this.availableVehicles = new ConcurrentLinkedDeque<>();
        this.futures = new ConcurrentLinkedDeque<>();

    }

    private static class ResourcesHolderHolder {
        private static ResourcesHolder instance = new ResourcesHolder();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static ResourcesHolder getInstance() {

        return ResourcesHolderHolder.instance;
    }

    /**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     *
     * @return {@link Future<DeliveryVehicle>} object which will resolve to a
     * {@link DeliveryVehicle} when completed.
     */
    public Future<DeliveryVehicle> acquireVehicle() {
        Future<DeliveryVehicle> vehicle = new Future<>();
        try {
            if (!this.availableVehicles.isEmpty()) {
                vehicle.resolve(availableVehicles.poll());
                System.out.println("resolved future with was returned from acquireVehicle()");
                return vehicle;  //returns the Future of the next available vehicle and removes it from the queue//

            } else {
                futures.add(vehicle);
                System.out.println("unresolved future was returned from acquireVehicle()");
                return vehicle;
            }
        } catch (NullPointerException e) {
            System.out.println("if you see that it means that two threads competed over one vehicle "
                    + "\n" + "and one of them got a future instead");

        } finally {
            futures.add(vehicle);
            System.out.println("after a NullPointerException an unresolved future was returned from acquireVehicle()");
            return vehicle;
        }
    }

    /**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     *
     * @param vehicle {@link DeliveryVehicle} to be released.
     */
    public void releaseVehicle(DeliveryVehicle vehicle) {
        try {
            if (!futures.isEmpty()) {
                futures.pop().resolve(vehicle);    //poll might return null//
            } else {
                if (!availableVehicles.contains(vehicle)) {   //here two threads might add vehicle//
                    availableVehicles.add(vehicle);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("two threads tried two resolve a future but only one was available in futures");
            availableVehicles.add(vehicle);    //should I put finally?//
        }
        System.out.println("vehicle number " + vehicle.getLicense() + " was released");
    }

    /**
     * Receives a collection of vehicles and stores them.
     * <p>
     *
     * @param vehicles Array of {@link DeliveryVehicle} instances to store.
     */
    public void load(DeliveryVehicle[] vehicles) {
        for (DeliveryVehicle vehicle : vehicles) {
           if(!availableVehicles.contains(vehicle)) {   //might have concurrency problem here//
               availableVehicles.add(vehicle);
           }
        }
    }

}

