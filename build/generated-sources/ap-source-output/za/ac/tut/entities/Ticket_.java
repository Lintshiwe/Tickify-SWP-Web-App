package za.ac.tut.entities;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Attendee;
import za.ac.tut.entities.Event;
import za.ac.tut.entities.EventManager;
import za.ac.tut.entities.QRCode;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-17T22:32:10")
@StaticMetamodel(Ticket.class)
public class Ticket_ { 

    public static volatile SingularAttribute<Ticket, QRCode> qrCodeInverse;
    public static volatile ListAttribute<Ticket, EventManager> eventManagers;
    public static volatile SingularAttribute<Ticket, QRCode> qrCode;
    public static volatile SingularAttribute<Ticket, BigDecimal> price;
    public static volatile ListAttribute<Ticket, Attendee> attendees;
    public static volatile SingularAttribute<Ticket, String> name;
    public static volatile SingularAttribute<Ticket, Integer> ticketID;
    public static volatile ListAttribute<Ticket, Event> events;

}