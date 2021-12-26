package bgu.spl.mics;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import  java.util.*;


public class MessageBusImpl implements MessageBus {
    private ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> queuesByMicroService;//<Microservice,Queue of Message>

// need to make sure that queuesAndFutureByMessage keys are by types om messages and NOT instances of messages//
    private ConcurrentHashMap<Class<? extends Message>, RobinQueue<MicroService >> robinByTypeOfMessage; //<Message,RobinQueue>
    //microserviceByMessage stores for each message all the microservices that are currently working on that message
    private ConcurrentHashMap<Message,MicroService> microserviceBymessage; //<message,microservice>

    private ConcurrentHashMap<Event,Future> resultsByEvents; // each event and his future\resolved future//
//maybe add also broadCasts to this list//
    private static class MessageBusImplHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    private MessageBusImpl() {

        this.queuesByMicroService = new ConcurrentHashMap<>();
        this.robinByTypeOfMessage = new ConcurrentHashMap<>();
        this.microserviceBymessage = new ConcurrentHashMap<>();
        this.resultsByEvents = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static MessageBusImpl getInstance() {
        return MessageBusImplHolder.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {  //adds the event to the Robin of this type of message//

        if (robinByTypeOfMessage.containsKey(type)) {
            robinByTypeOfMessage.get(type).add(m);  //subscribes m to type//
        } else {    //create and subscribe//
            robinByTypeOfMessage.put(type,new RobinQueue<>());
            robinByTypeOfMessage.get(type).add(m);
        }
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {

        if (robinByTypeOfMessage.containsKey(type)) {
            robinByTypeOfMessage.get(type).add(m);
        } else {    //create and subscribe//

            robinByTypeOfMessage.put(type, new RobinQueue<>());
            robinByTypeOfMessage.get(type).add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        boolean containsEventKey = false;
        while (containsEventKey == false) {
            try {
                resultsByEvents.get(e).resolve(result);   // find the specific Future object connected to the event e and resolve it //
                containsEventKey = true;
                queuesByMicroService.get(microserviceBymessage.get(e)).remove(e); // remove the event from the microservise's tasks queue
            }catch (NullPointerException exception){System.out.println("no result yet");}
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        for (MicroService m: robinByTypeOfMessage.get(b.getClass())) {
            queuesByMicroService.get(m).addLast(b);
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        if(robinByTypeOfMessage.containsKey(e.getClass()) && !robinByTypeOfMessage.get(e.getClass()).isEmpty())  { // check if someone subscribed for e
            queuesByMicroService.get(robinByTypeOfMessage.get(e.getClass()).circularDequeue()).add(e);             // add e to the microService's queue
            microserviceBymessage.put(e, robinByTypeOfMessage.get(e.getClass()).getLast());                        // update the round robin queue
            resultsByEvents.put(e, new Future<T>());
            return resultsByEvents.get(e);
        }

        return null;
    }

    @Override
    public void register(MicroService m) {
        queuesByMicroService.putIfAbsent(m, new LinkedBlockingDeque<>());
    }

    @Override
    public void unregister(MicroService m) {

        for( Class<? extends Message> type : robinByTypeOfMessage.keySet()){
           robinByTypeOfMessage.get(type).remove(m); // remove only if present
       }
       for (Message message : microserviceBymessage.keySet()){
           microserviceBymessage.remove(message,m);
       }
        queuesByMicroService.remove(m,queuesByMicroService.get(m)); 
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        if(!queuesByMicroService.containsKey(m)) {  // if m is not registered
            throw new IllegalStateException();
        }
        return queuesByMicroService.get(m).take();                 // "take" waits for the element to become available
    }


}
