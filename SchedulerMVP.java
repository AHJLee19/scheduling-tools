import java.io.*;
import java.util.*;

class Subject {
    String name;
    boolean isWestLafayette;
    int priority;
    List<int[]> availableWindows = new ArrayList<>(); // Each int[] is {startHour, endHour}

    public Subject(String name, boolean isWestLafayette) {
        this.name = name;
        this.isWestLafayette = isWestLafayette;
    }
}

public class SchedulerMVP {
    private static final String FILE_NAME = "currentSchedule";
    private static String[] masterSchedule = new String[24]; // 24-hour day (0 to 23)

    public static void main(String[] args) throws IOException {
        File scheduleFile = new File(FILE_NAME);

        // Requirement 5: If not the first visit, read and display the saved schedule table
        if (scheduleFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(scheduleFile));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            return;
        }

        // First-time visit setup
        Arrays.fill(masterSchedule, "Free");
        Scanner scanner = new Scanner(System.in);
        List<Subject> subjects = new ArrayList<>();

        System.out.println("=== First-Time Schedule Setup ===");
        while (true) {
            System.out.print("\nEnter subject name (e.g., CS180) or 'done' to finish adding classes: ");
            String name = scanner.nextLine().trim();
            if (name.equalsIgnoreCase("done")) break;

            System.out.print("Campus for " + name + " (1 = Indianapolis, 2 = West Lafayette): ");
            boolean isWL = scanner.nextLine().trim().equals("2");

            Subject subject = new Subject(name, isWL);

            System.out.print("How many available time windows does " + name + " have? ");
            int numWindows = Integer.parseInt(scanner.nextLine().trim());

            for (int i = 0; i < numWindows; i++) {
                System.out.print("  Window " + (i + 1) + " - Enter start hour and end hour (0-24, e.g., '10 11'): ");
                String[] parts = scanner.nextLine().trim().split("\\s+");
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                subject.availableWindows.add(new int[]{start, end});
            }
            subjects.add(subject);
        }

        // Requirement 5: Set priority levels after inputting all classes
        System.out.println("\n=== Set Class Priorities (1 = Highest Priority) ===");
        for (Subject s : subjects) {
            System.out.print("Enter priority for " + s.name + ": ");
            s.priority = Integer.parseInt(scanner.nextLine().trim());
        }

        // Sort classes so highest priority (lowest number) is scheduled first
        subjects.sort(Comparator.comparingInt(s -> s.priority));

        // Requirement 3: Recursively schedule each added class against masterSchedule
        System.out.println("\n=== Building Schedule ===");
        List<Subject> acceptedClasses = new ArrayList<>();
        scheduleNextSubject(subjects, 0, acceptedClasses);

        // Requirement 4: Save formatted table to "currentSchedule" and display it
        String tableOutput = formatScheduleTable(masterSchedule);
        FileWriter writer = new FileWriter(scheduleFile);
        writer.write(tableOutput);
        writer.close();

        System.out.println("\nSaved to '" + FILE_NAME + "':");
        System.out.print(tableOutput);
    }

    // Recursively loops through the user's subjects in priority order
    private static void scheduleNextSubject(List<Subject> allSubjects, int index, List<Subject> accepted) {
        if (index >= allSubjects.size()) return;

        Subject candidate = allSubjects.get(index);
        accepted.add(candidate);

        String[] tempSchedule = new String[24];
        Arrays.fill(tempSchedule, "Free");

        // Try to recursively fit all accepted classes plus the new candidate
        if (tryWindowsRecursively(accepted, 0, tempSchedule)) {
            masterSchedule = tempSchedule; // Lock in the updated valid schedule
        } else {
            accepted.remove(accepted.size() - 1);
            System.out.println("DROPPED: " + candidate.name);
        }

        scheduleNextSubject(allSubjects, index + 1, accepted);
    }

    // Recursively tests available windows for each subject against the schedule array
    private static boolean tryWindowsRecursively(List<Subject> subjects, int subIdx, String[] schedule) {
        if (subIdx == subjects.size()) return true;

        Subject current = subjects.get(subIdx);
        return testWindowOption(subjects, subIdx, current, 0, schedule);
    }

    private static boolean testWindowOption(List<Subject> subjects, int subIdx, Subject current, int winIdx, String[] schedule) {
        if (winIdx >= current.availableWindows.size()) return false;

        int[] window = current.availableWindows.get(winIdx);
        int start = window[0];
        int end = window[1];

        // Requirement 2: Account for 2-hour commute before and after West Lafayette classes
        int blockStart = current.isWestLafayette ? start - 2 : start;
        int blockEnd = current.isWestLafayette ? end + 2 : end;

        if (canPlace(schedule, blockStart, blockEnd)) {
            placeClass(schedule, current, start, end, false);
            if (tryWindowsRecursively(subjects, subIdx + 1, schedule)) {
                return true;
            }
            placeClass(schedule, current, start, end, true); // Backtrack if a later class fails
        }

        // Recursively try the next available window for this subject
        return testWindowOption(subjects, subIdx, current, winIdx + 1, schedule);
    }

    private static boolean canPlace(String[] schedule, int start, int end) {
        if (start < 0 || end > 24 || start >= end) return false;
        for (int h = start; h < end; h++) {
            if (!schedule[h].equals("Free")) return false;
        }
        return true;
    }

    private static void placeClass(String[] schedule, Subject s, int start, int end, boolean clear) {
        if (s.isWestLafayette) {
            for (int h = start - 2; h < start; h++) schedule[h] = clear ? "Free" : "shuttle time";
            for (int h = end; h < end + 2; h++) schedule[h] = clear ? "Free" : "shuttle time";
        }
        String label = clear ? "Free" : s.name + (s.isWestLafayette ? " (WL)" : "");
        for (int h = start; h < end; h++) {
            schedule[h] = label;
        }
    }

    private static String formatScheduleTable(String[] schedule) {
        StringBuilder sb = new StringBuilder();
        String divider = "+---------------+------------------------------+\n";
        sb.append(divider);
        sb.append(String.format("| %-13s | %-28s |\n", "Time Slot", "Scheduled Activity"));
        sb.append(divider);
        for (int h = 0; h < 24; h++) {
            String timeRange = String.format("%02d:00 - %02d:00", h, h + 1);
            sb.append(String.format("| %-13s | %-28s |\n", timeRange, schedule[h]));
        }
        sb.append(divider);
        return sb.toString();
    }
}