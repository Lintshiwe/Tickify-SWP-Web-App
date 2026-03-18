package za.ac.tut.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Attendee;
import za.ac.tut.entities.Ticket;
import za.ac.tut.entities.VenueGuard;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-18T08:40:06")
@StaticMetamodel(QRCode.class)
public class QRCode_ { 

    public static volatile SingularAttribute<QRCode, Integer> number;
    public static volatile SingularAttribute<QRCode, Integer> qrCodeID;
    public static volatile SingularAttribute<QRCode, Attendee> attendee;
    public static volatile SingularAttribute<QRCode, String> barstring;
    public static volatile SingularAttribute<QRCode, Ticket> ticket;
    public static volatile ListAttribute<QRCode, VenueGuard> venueGuards;

}