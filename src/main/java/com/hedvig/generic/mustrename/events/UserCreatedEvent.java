package com.hedvig.generic.mustrename.events;

import lombok.Value;
import org.apache.tomcat.jni.Local;

import java.time.LocalDate;

@Value
public class UserCreatedEvent {

    private String id;
    private String name;
    private LocalDate birthDate;

}
