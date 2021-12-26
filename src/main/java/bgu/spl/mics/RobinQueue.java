package bgu.spl.mics;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.Iterator;

//maybe chang to Blocking queue if necessary//
public class RobinQueue<T> implements Iterable<T> {    //added by Yotam and Alon

    private ConcurrentLinkedDeque <T> q;
    public RobinQueue(){
        this.q = new ConcurrentLinkedDeque<T>();
    }
    synchronized public T circularDequeue() {    //moves the head to the tail of the queue//
        if (peek() != null) {        //TODO maybe get help in SHEOT KABALA to do this atomic?
            add(poll());
            return getLast();
        }
        return null;
    }
    synchronized public T peek(){
        return q.peek();
    }
    synchronized public T poll(){
        return this.q.poll();
    }
    synchronized void add(T element){
         this.q.add(element);
    }
    synchronized T getLast(){
        return this.q.getLast();
    }
    synchronized void remove(){
        this.q.remove();
    }
    synchronized boolean isEmpty(){
        return this.q.isEmpty();
    }

    public Iterator<T> iterator() {
        return this.q.iterator();
    }
    public Iterator<T> descendingIterator(){
        return this.q.descendingIterator();
    }
    public synchronized void remove(T element){
        this.q.remove(element);
    }

    public synchronized void forEach(Consumer<?super T> action){
        this.q.forEach(action);
    }
    public synchronized Spliterator<T> spliterator(){
        return  this.q.spliterator();
    }
}
