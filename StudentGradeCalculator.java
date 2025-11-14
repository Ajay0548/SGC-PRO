import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Student Grade Calculator
 * Single-file console application (Java 8+)
 *
 * Features:
 * - Add students
 * - Add subjects and marks for each student
 * - Calculate total, average and letter grade
 * - Print student report
 * - Export report to CSV
 */
public class StudentGradeCalculator {
    // ----- Student model -----
    static class Student {
        private final String id;
        private final String name;
        private final Map<String, Double> marks = new LinkedHashMap<>(); // subject -> mark

        public Student(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }

        public void setMark(String subject, double mark) {
            marks.put(subject, mark);
        }

        public Map<String, Double> getMarks() {
            return Collections.unmodifiableMap(marks);
        }

        public double total() {
            double t = 0;
            for (double m : marks.values()) t += m;
            return t;
        }

        public double average() {
            if (marks.isEmpty()) return 0;
            return total() / marks.size();
        }

        public String grade() {
            return GradeCalculator.toLetterGrade(average());
        }
    }

    // ----- Grade rules & helpers -----
    static class GradeCalculator {
        // Example scale (you can modify)
        public static String toLetterGrade(double avg) {
            if (avg >= 90) return "A+";
            if (avg >= 80) return "A";
            if (avg >= 70) return "B";
            if (avg >= 60) return "C";
            if (avg >= 50) return "D";
            return "F";
        }
    }

    // ----- Application -----
    private final Map<String, Student> students = new LinkedHashMap<>();
    private final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        new StudentGradeCalculator().run();
    }

    private void run() {
        System.out.println("=== Student Grade Calculator ===");
        boolean quit = false;
        while (!quit) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> addStudent();
                case "2" -> addOrEditMarks();
                case "3" -> printStudentReport();
                case "4" -> printAllReports();
                case "5" -> exportCSV();
                case "6" -> quit = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("\nChoose an option:");
        System.out.println("1. Add student");
        System.out.println("2. Add / Edit marks for a student");
        System.out.println("3. Print report for a student");
        System.out.println("4. Print report for all students");
        System.out.println("5. Export all reports to CSV");
        System.out.println("6. Exit");
        System.out.print("Enter choice: ");
    }

    private void addStudent() {
        System.out.print("Enter student id: ");
        String id = sc.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("ID cannot be empty.");
            return;
        }
        if (students.containsKey(id)) {
            System.out.println("Student ID already exists.");
            return;
        }
        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) name = "Unknown";
        students.put(id, new Student(id, name));
        System.out.println("Student added: " + id + " - " + name);
    }

    private void addOrEditMarks() {
        Student s = chooseStudent();
        if (s == null) return;

        System.out.println("Enter subject name (or empty to finish):");
        while (true) {
            System.out.print("Subject: ");
            String subject = sc.nextLine().trim();
            if (subject.isEmpty()) break;

            Double mark = null;
            while (mark == null) {
                System.out.print("Mark for " + subject + " (0-100): ");
                String mStr = sc.nextLine().trim();
                try {
                    double m = Double.parseDouble(mStr);
                    if (m < 0 || m > 100) {
                        System.out.println("Mark must be between 0 and 100.");
                    } else {
                        mark = m;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number. Try again.");
                }
            }
            s.setMark(subject, mark);
            System.out.println("Saved: " + subject + " -> " + mark);
        }
    }

    private void printStudentReport() {
        Student s = chooseStudent();
        if (s == null) return;
        printReport(s);
    }

    private void printAllReports() {
        if (students.isEmpty()) {
            System.out.println("No students available.");
            return;
        }
        for (Student s : students.values()) {
            printReport(s);
            System.out.println("-------------------------------------");
        }
    }

    private void printReport(Student s) {
        System.out.println("\nReport for: " + s.getName() + " (ID: " + s.getId() + ")");
        if (s.getMarks().isEmpty()) {
            System.out.println("No marks recorded.");
            return;
        }
        System.out.printf("%-20s %8s%n", "Subject", "Mark");
        System.out.println("-------------------------------");
        for (var e : s.getMarks().entrySet()) {
            System.out.printf("%-20s %8.2f%n", e.getKey(), e.getValue());
        }
        System.out.println("-------------------------------");
        System.out.printf("Total: %.2f%n", s.total());
        System.out.printf("Average: %.2f%n", s.average());
        System.out.println("Grade: " + s.grade());
    }

    private Student chooseStudent() {
        if (students.isEmpty()) {
            System.out.println("No students found. Add a student first.");
            return null;
        }
        System.out.println("Available students:");
        for (Student st : students.values()) {
            System.out.println(st.getId() + " -> " + st.getName());
        }
        System.out.print("Enter student id: ");
        String id = sc.nextLine().trim();
        Student s = students.get(id);
        if (s == null) {
            System.out.println("Student not found.");
            return null;
        }
        return s;
    }

    private void exportCSV() {
        if (students.isEmpty()) {
            System.out.println("No students to export.");
            return;
        }
        // Collect all subjects across students to create header columns
        LinkedHashSet<String> allSubjects = new LinkedHashSet<>();
        for (Student s : students.values()) allSubjects.addAll(s.getMarks().keySet());

        String filename = "student_report.csv";
        try (FileWriter fw = new FileWriter(filename)) {
            // Header
            fw.append("ID,Name");
            for (String subj : allSubjects) fw.append(",").append(escapeCsv(subj));
            fw.append(",Total,Average,Grade\n");

            // Rows
            for (Student s : students.values()) {
                fw.append(escapeCsv(s.getId())).append(",").append(escapeCsv(s.getName()));
                for (String subj : allSubjects) {
                    Double m = s.getMarks().get(subj);
                    fw.append(",");
                    if (m != null) fw.append(String.format(Locale.ROOT, "%.2f", m));
                }
                fw.append(",");
                fw.append(String.format(Locale.ROOT, "%.2f", s.total()));
                fw.append(",");
                fw.append(String.format(Locale.ROOT, "%.2f", s.average()));
                fw.append(",").append(s.grade()).append("\n");
            }
            System.out.println("Exported to: " + filename);
        } catch (IOException e) {
            System.out.println("Failed to write CSV: " + e.getMessage());
        }
    }

    // Minimal CSV escaping for commas / quotes
    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
