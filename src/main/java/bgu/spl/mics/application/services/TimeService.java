package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;
import bgu.spl.mics.application.passiveObjects.*;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast Broadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {
    private int tick = 1;
    private Object lock;
    private int millisPerTick;
    private int duration;
    Timer timer;

    public TimeService(int millisPerTick, int duration) {
        super("timeService");
        this.millisPerTick = millisPerTick;
        this.duration = duration;
        this.timer = new Timer();
    }

    @Override
    protected void initialize() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                    sendBroadcast(new TickBroadcast(tick, duration));
                    tick++;

                if (tick > duration){
                    timer.cancel();
                    timer.purge();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, millisPerTick);
        terminate();

    }

}
