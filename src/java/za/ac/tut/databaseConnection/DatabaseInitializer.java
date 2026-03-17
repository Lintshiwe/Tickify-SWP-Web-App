/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.tut.databaseConnection;

import java.sql.*;

/**
 *
 * @author ntoam
 */
public class DatabaseInitializer {
    
    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            createTables(conn);
            seedData(conn);
            System.out.println("Tickify DB initialized successfully.");
        } catch (SQLException e) {
            System.err.println("DB initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
 
    // ----------------------------------------------------------------
    // DDL
    // ----------------------------------------------------------------
    private static void createTables(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
 
        // venue
        if (!tableExists(conn, "VENUE")) {
            st.execute(
                "CREATE TABLE venue (" +
                "  venueID     INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  name        VARCHAR(45)," +
                "  address     VARCHAR(205)" +
                ")"
            );
        }
 
        // event
        if (!tableExists(conn, "EVENT")) {
            st.execute(
                "CREATE TABLE event (" +
                "  eventID  INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  name     VARCHAR(45)," +
                "  type     VARCHAR(45)," +
                "  date     TIMESTAMP," +
                "  venueID  INT NOT NULL," +
                "  FOREIGN KEY (venueID) REFERENCES venue(venueID)" +
                ")"
            );
        }
 
        // qrcode
        if (!tableExists(conn, "QRCODE")) {
            st.execute(
                "CREATE TABLE qrcode (" +
                "  QRcodeID  INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  barstring VARCHAR(255)," +
                "  number    INT NOT NULL" +
                ")"
            );
        }
 
        // admin
        if (!tableExists(conn, "ADMIN")) {
            st.execute(
                "CREATE TABLE admin (" +
                "  adminID   INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  firstname VARCHAR(45)," +
                "  lastname  VARCHAR(45)," +
                "  email     VARCHAR(45) UNIQUE," +
                "  password  VARCHAR(100)," +
                "  eventID   INT NOT NULL," +
                "  FOREIGN KEY (eventID) REFERENCES event(eventID)" +
                ")"
            );
        }
 
        // attendee
        if (!tableExists(conn, "ATTENDEE")) {
            st.execute(
                "CREATE TABLE attendee (" +
                "  attendeeID           INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  tertiaryInstitution  VARCHAR(45)," +
                "  firstname            VARCHAR(45)," +
                "  lastname             VARCHAR(45)," +
                "  email                VARCHAR(45) UNIQUE," +
                "  password             VARCHAR(100)," +
                "  qrcode_QRcodeID      INT NOT NULL," +
                "  FOREIGN KEY (qrcode_QRcodeID) REFERENCES qrcode(QRcodeID)" +
                ")"
            );
        }
 
        // venue_guard
        if (!tableExists(conn, "VENUE_GUARD")) {
            st.execute(
                "CREATE TABLE venue_guard (" +
                "  venueGuardID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  firstname    VARCHAR(45)," +
                "  lastname     VARCHAR(45)," +
                "  email        VARCHAR(45) UNIQUE," +
                "  password     VARCHAR(100)," +
                "  eventID      INT NOT NULL," +
                "  venueID      INT NOT NULL," +
                "  QRcodeID     INT NOT NULL," +
                "  FOREIGN KEY (eventID)  REFERENCES event(eventID)," +
                "  FOREIGN KEY (venueID)  REFERENCES venue(venueID)," +
                "  FOREIGN KEY (QRcodeID) REFERENCES qrcode(QRcodeID)" +
                ")"
            );
        }
 
        // event_manager
        if (!tableExists(conn, "EVENT_MANAGER")) {
            st.execute(
                "CREATE TABLE event_manager (" +
                "  eventManagerID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  firstname      VARCHAR(45)," +
                "  lastname       VARCHAR(45)," +
                "  email          VARCHAR(45) UNIQUE," +
                "  password       VARCHAR(100)," +
                "  venueGuardID   INT NOT NULL," +
                "  FOREIGN KEY (venueGuardID) REFERENCES venue_guard(venueGuardID)" +
                ")"
            );
        }
 
        // tertiary_presenter
        if (!tableExists(conn, "TERTIARY_PRESENTER")) {
            st.execute(
                "CREATE TABLE tertiary_presenter (" +
                "  tertiaryPresenterID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  firstname           VARCHAR(45)," +
                "  lastname            VARCHAR(45)," +
                "  email               VARCHAR(45) UNIQUE," +
                "  password            VARCHAR(100)," +
                "  tertiaryInstitution VARCHAR(45)," +
                "  eventID             INT NOT NULL," +
                "  venueID             INT NOT NULL," +
                "  FOREIGN KEY (eventID) REFERENCES event(eventID)," +
                "  FOREIGN KEY (venueID) REFERENCES venue(venueID)" +
                ")"
            );
        }
 
        // ticket
        if (!tableExists(conn, "TICKET")) {
            st.execute(
                "CREATE TABLE ticket (" +
                "  ticketID  INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "  name      VARCHAR(45)," +
                "  price     DECIMAL(6,2)," +
                "  QRcodeID  INT NOT NULL," +
                "  FOREIGN KEY (QRcodeID) REFERENCES qrcode(QRcodeID)" +
                ")"
            );
        }
 
        // junction tables
        if (!tableExists(conn, "ATTENDEE_HAS_EVENT")) {
            st.execute(
                "CREATE TABLE attendee_has_event (" +
                "  attendeeID INT NOT NULL," +
                "  eventID    INT NOT NULL," +
                "  PRIMARY KEY (attendeeID, eventID)," +
                "  FOREIGN KEY (attendeeID) REFERENCES attendee(attendeeID)," +
                "  FOREIGN KEY (eventID)    REFERENCES event(eventID)" +
                ")"
            );
        }
 
        if (!tableExists(conn, "ATTENDEE_HAS_TICKET")) {
            st.execute(
                "CREATE TABLE attendee_has_ticket (" +
                "  attendeeID INT NOT NULL," +
                "  ticketID   INT NOT NULL," +
                "  PRIMARY KEY (attendeeID, ticketID)," +
                "  FOREIGN KEY (attendeeID) REFERENCES attendee(attendeeID)," +
                "  FOREIGN KEY (ticketID)   REFERENCES ticket(ticketID)" +
                ")"
            );
        }
 
        if (!tableExists(conn, "EVENT_HAS_TICKET")) {
            st.execute(
                "CREATE TABLE event_has_ticket (" +
                "  eventID  INT NOT NULL," +
                "  ticketID INT NOT NULL," +
                "  PRIMARY KEY (eventID, ticketID)," +
                "  FOREIGN KEY (eventID)  REFERENCES event(eventID)," +
                "  FOREIGN KEY (ticketID) REFERENCES ticket(ticketID)" +
                ")"
            );
        }
 
        if (!tableExists(conn, "EVENT_HAS_MANAGER")) {
            st.execute(
                "CREATE TABLE event_has_manager (" +
                "  eventID        INT NOT NULL," +
                "  eventManagerID INT NOT NULL," +
                "  PRIMARY KEY (eventID, eventManagerID)," +
                "  FOREIGN KEY (eventID)        REFERENCES event(eventID)," +
                "  FOREIGN KEY (eventManagerID) REFERENCES event_manager(eventManagerID)" +
                ")"
            );
        }
 
        if (!tableExists(conn, "EVENTMANAGER_HAS_TICKET")) {
            st.execute(
                "CREATE TABLE eventmanager_has_ticket (" +
                "  eventManagerID INT NOT NULL," +
                "  ticketID       INT NOT NULL," +
                "  PRIMARY KEY (eventManagerID, ticketID)," +
                "  FOREIGN KEY (eventManagerID) REFERENCES event_manager(eventManagerID)," +
                "  FOREIGN KEY (ticketID)       REFERENCES ticket(ticketID)" +
                ")"
            );
        }
 
        st.close();
    }
 
    // ----------------------------------------------------------------
    // SEED DATA  (5+ records per main table)
    // ----------------------------------------------------------------
    private static void seedData(Connection conn) throws SQLException {
        if (rowCount(conn, "VENUE") > 0) return; // already seeded
 
        // venues
        int[] venueIDs = new int[5];
        String[] venueNames = {"DUT Sports Hall", "UKZN Great Hall", "Wits Amphitheatre", "UJ Auditorium", "CPUT Stadium"};
        String[] venueAddrs  = {"1 Steve Biko Rd, Durban", "Howard College, Durban", "1 Jan Smuts Ave, Johannesburg",
                                "55 Kingsway Ave, Auckland Park", "Symphony Way, Bellville"};
        PreparedStatement pv = conn.prepareStatement("INSERT INTO venue(name,address) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < 5; i++) {
            pv.setString(1, venueNames[i]);
            pv.setString(2, venueAddrs[i]);
            pv.executeUpdate();
            try (ResultSet rs = pv.getGeneratedKeys()) { if (rs.next()) venueIDs[i] = rs.getInt(1); }
        }
        pv.close();
 
        // events
        int[] eventIDs = new int[5];
        String[][] events = {
            {"Tech Summit 2026","Conference","2026-06-15 09:00:00"},
            {"Music Fiesta","Concert","2026-07-20 18:00:00"},
            {"Science Expo","Exhibition","2026-08-05 10:00:00"},
            {"Cultural Day","Festival","2026-09-01 08:00:00"},
            {"Sports Gala","Sports","2026-10-10 14:00:00"}
        };
        PreparedStatement pe = conn.prepareStatement("INSERT INTO event(name,type,date,venueID) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < 5; i++) {
            pe.setString(1, events[i][0]); pe.setString(2, events[i][1]);
            pe.setTimestamp(3, Timestamp.valueOf(events[i][2])); pe.setInt(4, venueIDs[i]);
            pe.executeUpdate();
            try (ResultSet rs = pe.getGeneratedKeys()) { if (rs.next()) eventIDs[i] = rs.getInt(1); }
        }
        pe.close();
 
        // qrcodes
        int[] qrIDs = new int[10];
        PreparedStatement pq = conn.prepareStatement("INSERT INTO qrcode(barstring,number) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < 10; i++) {
            pq.setString(1, "QR-" + System.currentTimeMillis() + "-" + i); pq.setInt(2, 1000 + i);
            pq.executeUpdate();
            try (ResultSet rs = pq.getGeneratedKeys()) { if (rs.next()) qrIDs[i] = rs.getInt(1); }
        }
        pq.close();
 
        // admin
        PreparedStatement pa = conn.prepareStatement("INSERT INTO admin(firstname,lastname,email,password,eventID) VALUES(?,?,?,?,?)");
        String[][] admins = {
            {"System","Admin","admin@tickify.ac.za","admin123"},
            {"Thabo","Nkosi","thabo@tickify.ac.za","pass1234"},
            {"Lerato","Dlamini","lerato@tickify.ac.za","pass5678"},
            {"Sipho","Mokoena","sipho@tickify.ac.za","pass9012"},
            {"Nomsa","Khumalo","nomsa@tickify.ac.za","pass3456"}
        };
        for (int i = 0; i < 5; i++) {
            pa.setString(1,admins[i][0]); pa.setString(2,admins[i][1]);
            pa.setString(3,admins[i][2]); pa.setString(4,admins[i][3]); pa.setInt(5,eventIDs[i]);
            pa.executeUpdate();
        }
        pa.close();
 
        // attendees
        PreparedStatement pat = conn.prepareStatement("INSERT INTO attendee(tertiaryInstitution,firstname,lastname,email,password,qrcode_QRcodeID) VALUES(?,?,?,?,?,?)");
        String[][] attendees = {
            {"DUT","Lekwene","L","lekwene@student.dut.ac.za","att001"},
            {"UKZN","Ntoampi","LP","ntoampi@student.ukzn.ac.za","att002"},
            {"Wits","Mokoena","M","mokoena@student.wits.ac.za","att003"},
            {"UJ","Sosiba","Z","sosiba@student.uj.ac.za","att004"},
            {"CPUT","Mngadi","AM","mngadi@student.cput.ac.za","att005"}
        };
        int[] attIDs = new int[5];
        PreparedStatement patg = conn.prepareStatement("INSERT INTO attendee(tertiaryInstitution,firstname,lastname,email,password,qrcode_QRcodeID) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < 5; i++) {
            patg.setString(1,attendees[i][0]); patg.setString(2,attendees[i][1]);
            patg.setString(3,attendees[i][2]); patg.setString(4,attendees[i][3]);
            patg.setString(5,attendees[i][4]); patg.setInt(6,qrIDs[i]);
            patg.executeUpdate();
            try (ResultSet rs = patg.getGeneratedKeys()) { if (rs.next()) attIDs[i] = rs.getInt(1); }
        }
        patg.close();
 
        // venue_guard
        int[] guardIDs = new int[5];
        PreparedStatement pg = conn.prepareStatement(
            "INSERT INTO venue_guard(firstname,lastname,email,password,eventID,venueID,QRcodeID) VALUES(?,?,?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS);
        String[][] guards = {
            {"Guard","Alpha","guard1@tickify.ac.za","guard001"},
            {"Guard","Bravo","guard2@tickify.ac.za","guard002"},
            {"Guard","Charlie","guard3@tickify.ac.za","guard003"},
            {"Guard","Delta","guard4@tickify.ac.za","guard004"},
            {"Guard","Echo","guard5@tickify.ac.za","guard005"}
        };
        for (int i = 0; i < 5; i++) {
            pg.setString(1,guards[i][0]); pg.setString(2,guards[i][1]);
            pg.setString(3,guards[i][2]); pg.setString(4,guards[i][3]);
            pg.setInt(5,eventIDs[i]); pg.setInt(6,venueIDs[i]); pg.setInt(7,qrIDs[5+i]);
            pg.executeUpdate();
            try (ResultSet rs = pg.getGeneratedKeys()) { if (rs.next()) guardIDs[i] = rs.getInt(1); }
        }
        pg.close();
 
        // event_manager
        int[] managerIDs = new int[5];
        PreparedStatement pm = conn.prepareStatement(
            "INSERT INTO event_manager(firstname,lastname,email,password,venueGuardID) VALUES(?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS);
        String[][] managers = {
            {"Manager","One","mgr1@tickify.ac.za","mgr001"},
            {"Manager","Two","mgr2@tickify.ac.za","mgr002"},
            {"Manager","Three","mgr3@tickify.ac.za","mgr003"},
            {"Manager","Four","mgr4@tickify.ac.za","mgr004"},
            {"Manager","Five","mgr5@tickify.ac.za","mgr005"}
        };
        for (int i = 0; i < 5; i++) {
            pm.setString(1,managers[i][0]); pm.setString(2,managers[i][1]);
            pm.setString(3,managers[i][2]); pm.setString(4,managers[i][3]); pm.setInt(5,guardIDs[i]);
            pm.executeUpdate();
            try (ResultSet rs = pm.getGeneratedKeys()) { if (rs.next()) managerIDs[i] = rs.getInt(1); }
        }
        pm.close();
 
        // tertiary_presenter
        PreparedStatement ptp = conn.prepareStatement(
            "INSERT INTO tertiary_presenter(firstname,lastname,email,password,tertiaryInstitution,eventID,venueID) VALUES(?,?,?,?,?,?,?)");
        String[][] presenters = {
            {"Prof","Zulu","pzulu@dut.ac.za","pres001","DUT"},
            {"Dr","Naidoo","dnaidoo@ukzn.ac.za","pres002","UKZN"},
            {"Prof","Smith","psmith@wits.ac.za","pres003","Wits"},
            {"Dr","Baloyi","dbaloyi@uj.ac.za","pres004","UJ"},
            {"Prof","Jacobs","pjacobs@cput.ac.za","pres005","CPUT"}
        };
        for (int i = 0; i < 5; i++) {
            ptp.setString(1,presenters[i][0]); ptp.setString(2,presenters[i][1]);
            ptp.setString(3,presenters[i][2]); ptp.setString(4,presenters[i][3]);
            ptp.setString(5,presenters[i][4]); ptp.setInt(6,eventIDs[i]); ptp.setInt(7,venueIDs[i]);
            ptp.executeUpdate();
        }
        ptp.close();
 
        // tickets
        int[] ticketIDs = new int[5];
        PreparedStatement ptk = conn.prepareStatement(
            "INSERT INTO ticket(name,price,QRcodeID) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
        String[] tNames  = {"General Admission","VIP","Student","Early Bird","Group"};
        double[] tPrices = {150.00, 350.00, 80.00, 120.00, 200.00};
        for (int i = 0; i < 5; i++) {
            ptk.setString(1,tNames[i]); ptk.setBigDecimal(2,new java.math.BigDecimal(tPrices[i])); ptk.setInt(3,qrIDs[i]);
            ptk.executeUpdate();
            try (ResultSet rs = ptk.getGeneratedKeys()) { if (rs.next()) ticketIDs[i] = rs.getInt(1); }
        }
        ptk.close();
 
        // junction rows
        PreparedStatement jahe = conn.prepareStatement("INSERT INTO attendee_has_event(attendeeID,eventID) VALUES(?,?)");
        PreparedStatement jaht = conn.prepareStatement("INSERT INTO attendee_has_ticket(attendeeID,ticketID) VALUES(?,?)");
        PreparedStatement jeht = conn.prepareStatement("INSERT INTO event_has_ticket(eventID,ticketID) VALUES(?,?)");
        PreparedStatement jehm = conn.prepareStatement("INSERT INTO event_has_manager(eventID,eventManagerID) VALUES(?,?)");
        for (int i = 0; i < 5; i++) {
            jahe.setInt(1,attIDs[i]); jahe.setInt(2,eventIDs[i]); jahe.executeUpdate();
            jaht.setInt(1,attIDs[i]); jaht.setInt(2,ticketIDs[i]); jaht.executeUpdate();
            jeht.setInt(1,eventIDs[i]); jeht.setInt(2,ticketIDs[i]); jeht.executeUpdate();
            jehm.setInt(1,eventIDs[i]); jehm.setInt(2,managerIDs[i]); jehm.executeUpdate();
        }
        jahe.close(); jaht.close(); jeht.close(); jehm.close();
 
        System.out.println("Seed data inserted successfully.");
    }
 
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
 
    private static int rowCount(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
}
