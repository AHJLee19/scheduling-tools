import java.time.LocalTime;

/*
this the basic class for the 'Class' class, class class in the class\

'Class' refers to a class at purdue, this is the bare bones file
for the methods and such that will be used in the class

NOTE: Location will always be either 'IN' for Indianapolis or 'WL' for West Lafayette
 */

public class Class() {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    //Note: will always be "IN" or "WL" or will throw an error
    private String location;

    //constructor where 'time' is a LocalTime
    public Class(String name, LocalTime startTime, LocalTime endTime, String location) throws Exception {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime

        if (location.equals("IN") || location.equals("WL")) {
            this.location = location;
        } else {
            throw new Exception("Location must be either 'WL' or 'IN'");
        }
    }

    //constructor where 'time' is a String
    public Class(String name, String startTime, String endTime, String location) throws Exception {
        this.name = name;

        try {
            this.startTime = LocalTime.parse(startTime);
        } catch (DateTimeParseException e) {
            throw new (Exception("Error parsing time string to LocalTime: " + e.getMessage()));
        }

        try {
            this.endTime = LocalTime.parse(endTime);
        } catch (DateTimeParseException e) {
            throw new (Exception("Error parsing time string to LocalTime: " + e.getMessage()));
        }

        if (location.equals("IN") || location.equals("WL")) {
            this.location = location;
        } else {
            throw new Exception("Location must be either 'WL' or 'IN'");
        }
    }

    public String getName() {
        return this.name;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public String getLocation() {
        return this.location;
    }
}
