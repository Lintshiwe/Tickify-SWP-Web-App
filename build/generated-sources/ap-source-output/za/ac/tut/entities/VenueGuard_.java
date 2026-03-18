package za.ac.tut.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Event;
import za.ac.tut.entities.EventManager;
import za.ac.tut.entities.QRCode;
import za.ac.tut.entities.Venue;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-18T03:17:23")
@StaticMetamodel(VenueGuard.class)
public class VenueGuard_ { 

    public static volatile SingularAttribute<VenueGuard, Venue> venue;
    public static volatile SingularAttribute<VenueGuard, String> firstname;
    public static volatile SingularAttribute<VenueGuard, String> password;
    public static volatile ListAttribute<VenueGuard, EventManager> eventManagers;
    public static volatile SingularAttribute<VenueGuard, QRCode> qrCode;
    public static volatile SingularAttribute<VenueGuard, Integer> venueGuardID;
    public static volatile SingularAttribute<VenueGuard, Event> event;
    public static volatile SingularAttribute<VenueGuard, String> email;
    public static volatile SingularAttribute<VenueGuard, String> lastname;

}