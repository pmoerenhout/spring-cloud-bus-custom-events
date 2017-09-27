package com.tndavidson.example;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.springframework.cloud.bus.event.RemoteApplicationEvent;

/**
 * Custom event that will be published on the bus
 *
 * @author Tim Davidson
 */
public class MyCustomRemoteEvent extends RemoteApplicationEvent {
    private String message;
    private Instant instant;
    private LocalDate localDate;
    private LocalDateTime localDateTime;
    private ZonedDateTime zonedDateTime;

    // Must supply a default constructor and getters/setters for deserialization
    public MyCustomRemoteEvent() {
    }

    public MyCustomRemoteEvent(Object source, String originService, String message, Instant instant, LocalDate localDate, LocalDateTime localDateTime, ZonedDateTime zonedDateTime) {
        // source is the object that is publishing the event
        // originService is the unique context ID of the publisher
        super(source, originService);
        this.message = message;
        this.instant = instant;
        this.localDate = localDate;
        this.localDateTime = localDateTime;
        this.zonedDateTime = zonedDateTime;
    }

    public String getMessage() {
        return message;
    }

    public MyCustomRemoteEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(final Instant instant) {
        this.instant = instant;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(final LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(final LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public void setZonedDateTime(final ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }
}
