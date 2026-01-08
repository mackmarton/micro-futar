package org.bme.micro_futar.logistics.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DepoChangedEvent extends ApplicationEvent {

    private final ChangeType changeType;

    public DepoChangedEvent(Object source, ChangeType changeType) {
        super(source);
        this.changeType = changeType;
    }

    public enum ChangeType {
        CREATED,
        UPDATED,
        DELETED
    }
}

