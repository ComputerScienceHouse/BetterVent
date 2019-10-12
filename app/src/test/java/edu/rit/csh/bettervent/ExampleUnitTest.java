package edu.rit.csh.bettervent;

import org.junit.Test;

import java.util.Date;

import edu.rit.csh.bettervent.view.Event;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void event_happening_now(){
        Date oneMinuteAgo = new Date(System.currentTimeMillis() - 60 * 1000);
        Date oneMinuteLater = new Date(System.currentTimeMillis() + 60 * 1000);
        Date oneHourAgo = new Date(System.currentTimeMillis() - 3600 * 1000);
        Date oneHourLater = new Date(System.currentTimeMillis() + 3600 * 1000);

        Event happeningNow = new Event("Summary", oneMinuteAgo, oneMinuteLater, "Location");
        Event alsoHappeningNow = new Event("Summary", oneHourAgo, oneHourLater, "Location");
        Event happeningLater = new Event("Summary", oneMinuteLater, oneHourLater, "Location");
        Event happenedEarlier = new Event("Summary", oneHourAgo, oneMinuteAgo, "Location");

        assertTrue(happeningNow.isHappeningNow());
        assertTrue(alsoHappeningNow.isHappeningNow());
        assertFalse(happeningLater.isHappeningNow());
        assertFalse(happenedEarlier.isHappeningNow());
    }
}