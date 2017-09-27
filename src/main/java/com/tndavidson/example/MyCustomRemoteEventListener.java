package com.tndavidson.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event handler for {@link MyCustomRemoteEvent}
 *
 * @author Tim Davidson
 */
@Component
public class MyCustomRemoteEventListener {
    private static final Logger logger = LoggerFactory.getLogger(MyCustomRemoteEventListener.class);

  @EventListener
    public void onApplicationEvent(MyCustomRemoteEvent myCustomRemoteEvent) {
        logger.info("Received MyCustomRemoteEvent - message:{} instant:{} localDate:{} localDateTime:{} zonedDateTime:{}",
            myCustomRemoteEvent.getMessage(),
            myCustomRemoteEvent.getInstant(),
            myCustomRemoteEvent.getLocalDate(),
            myCustomRemoteEvent.getLocalDateTime(),
            myCustomRemoteEvent.getZonedDateTime());
    }
}
