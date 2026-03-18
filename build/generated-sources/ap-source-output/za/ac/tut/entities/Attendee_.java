package za.ac.tut.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Event;
import za.ac.tut.entities.QRCode;
import za.ac.tut.entities.Ticket;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-18T08:40:06")
@StaticMetamodel(Attendee.class)
public class Attendee_ { 

    public static volatile SingularAttribute<Attendee, String> firstname;
    public static volatile SingularAttribute<Attendee, String> password;
    public static volatile ListAttribute<Attendee, Ticket> tickets;
    public static volatile SingularAttribute<Attendee, QRCode> qrCode;
    public static volatile SingularAttribute<Attendee, Integer> attendeeID;
    public static volatile SingularAttribute<Attendee, String> tertiaryInstitution;
    public static volatile SingularAttribute<Attendee, String> email;
    public static volatile ListAttribute<Attendee, Event> events;
    public static volatile SingularAttribute<Attendee, String> lastname;

}