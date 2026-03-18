package za.ac.tut.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Event;
import za.ac.tut.entities.Venue;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-18T08:40:06")
@StaticMetamodel(TertiaryPresenter.class)
public class TertiaryPresenter_ { 

    public static volatile SingularAttribute<TertiaryPresenter, Venue> venue;
    public static volatile SingularAttribute<TertiaryPresenter, String> firstname;
    public static volatile SingularAttribute<TertiaryPresenter, String> password;
    public static volatile SingularAttribute<TertiaryPresenter, Integer> tertiaryPresenterID;
    public static volatile SingularAttribute<TertiaryPresenter, Event> event;
    public static volatile SingularAttribute<TertiaryPresenter, String> tertiaryInstitution;
    public static volatile SingularAttribute<TertiaryPresenter, String> email;
    public static volatile SingularAttribute<TertiaryPresenter, String> lastname;

}