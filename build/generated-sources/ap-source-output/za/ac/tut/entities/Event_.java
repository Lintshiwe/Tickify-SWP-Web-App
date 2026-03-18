package za.ac.tut.entities;

import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Attendee;
import za.ac.tut.entities.EventManager;
import za.ac.tut.entities.TertiaryPresenter;
import za.ac.tut.entities.Ticket;
import za.ac.tut.entities.Venue;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-18T08:40:06")
@StaticMetamodel(Event.class)
public class Event_ { 

    public static volatile SingularAttribute<Event, Timestamp> date;
    public static volatile SingularAttribute<Event, Integer> eventID;
    public static volatile SingularAttribute<Event, Venue> venue;
    public static volatile ListAttribute<Event, EventManager> eventManagers;
    public static volatile ListAttribute<Event, TertiaryPresenter> tertiaryPresenters;
    public static volatile ListAttribute<Event, Ticket> tickets;
    public static volatile ListAttribute<Event, Attendee> attendees;
    public static volatile SingularAttribute<Event, String> name;
    public static volatile SingularAttribute<Event, String> type;

}