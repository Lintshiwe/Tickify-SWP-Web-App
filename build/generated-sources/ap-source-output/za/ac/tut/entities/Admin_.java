package za.ac.tut.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import za.ac.tut.entities.Event;

@Generated(value="EclipseLink-2.7.10.v20211216-rNA", date="2026-03-18T08:40:06")
@StaticMetamodel(Admin.class)
public class Admin_ { 

    public static volatile SingularAttribute<Admin, String> firstname;
    public static volatile SingularAttribute<Admin, String> password;
    public static volatile SingularAttribute<Admin, Integer> adminID;
    public static volatile SingularAttribute<Admin, Event> event;
    public static volatile SingularAttribute<Admin, String> email;
    public static volatile SingularAttribute<Admin, String> lastname;

}