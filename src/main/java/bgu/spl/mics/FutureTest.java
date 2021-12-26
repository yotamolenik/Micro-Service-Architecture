package bgu.spl.mics;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)

public class FutureTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Future.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    Future f = null;
    Object result = null;

    @Before
    void setUp() {
        this.result = new Object();
        f=new Future();
    }

    void tearDown() {
    }
    @Test
    void testResolve() {


        assertTrue(f.get(30, TimeUnit.SECONDS) ==null);
        f.resolve(result);
        assertTrue(f.get(30,TimeUnit.SECONDS) == result);


        try{f.resolve(result);}
        catch (Exception e) {
            System.out.println("if you see this message it means that exception was being thrown and everithing is fine");}

    }

    @Test
    void testGet() {

        //test blocking part//

        Thread t = new Thread(()->{
            f.get();
        });
        t.start();
        assertTrue(t.getState().toString().equals("BLOCKED") );
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        f.resolve(result);
        t = new Thread(()->{
            f.get();
        });
        t.start();
        assertTrue(t.getState().toString().equals("TERMINATED") );

        //tests return part//
        assertEquals(f.get(), result);

    }

    @Test
    void testIsDone() {
        assertTrue(! f.isDone());
        f.resolve(result);
        assertTrue(f.isDone());
    }

    void testGet1() {
        assertTrue(f.get(30, TimeUnit.SECONDS)==null);
        f.resolve(result);
        assertTrue(f.get(30, TimeUnit.SECONDS)==result);
    }

}

