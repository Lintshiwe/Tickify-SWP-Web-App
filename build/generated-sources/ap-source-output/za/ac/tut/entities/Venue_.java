package za.ac.tut.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Event;
import za.ac.tut.entities.TertiaryPresenter;
import za.ac.tut.entities.VenueGuard;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-18T08:40:06")
@StaticMetamodel(Venue.class)
public class Venue_ { 

    public static volatile ListAttribute<Venue, TertiaryPresenter> tertiaryPresenters;
    public static volatile SingularAttribute<Venue, String> address;
    public static volatile SingularAttribute<Venue, Integer> venueID;
    public static volatile SingularAttribute<Venue, String> name;
    public static volatile ListAttribute<Venue, VenueGuard> venueGuards;
    public static volatile ListAttribute<Venue, Event> events;

}