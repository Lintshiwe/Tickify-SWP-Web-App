package za.ac.tut.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Event;
import za.ac.tut.entities.Ticket;
import za.ac.tut.entities.VenueGuard;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-18T08:40:06")
@StaticMetamodel(EventManager.class)
public class EventManager_ { 

    public static volatile SingularAttribute<EventManager, String> firstname;
    public static volatile SingularAttribute<EventManager, String> password;
    public static volatile SingularAttribute<EventManager, Integer> eventManagerID;
    public static volatile ListAttribute<EventManager, Ticket> tickets;
    public static volatile SingularAttribute<EventManager, VenueGuard> venueGuard;
    public static volatile SingularAttribute<EventManager, String> email;
    public static volatile ListAttribute<EventManager, Event> events;
    public static volatile SingularAttribute<EventManager, String> lastname;

}