import dao.*;
import model.*;
import service.CourseService;

import java.util.List;
import java.util.Scanner;

public class MainCLI {

    private static final CourseDAO courseDAO = new CourseDAO();
    // private static final TeacherDAO teacherDAO = new TeacherDAO();
    private static final TeachingCostDAO costDAO = new TeachingCostDAO();
    // private static final AllocationDAO allocationDAO = new AllocationDAO();
    private static final CourseService service = new CourseService();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n==== COURSE PLANNING SYSTEM ====");
            System.out.println("1. Show all course instances");
            System.out.println("2. Show teaching cost for instance (planned + actual)");
            System.out.println("3. Increase +100 students to instance");
            System.out.println("4. Allocate teacher to activity");
            System.out.println("5. Deallocate teacher from instance/activity");
            System.out.println("6. Add new activity 'Exercise' (planned + allocate)");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    showInstances();
                    break;
                case "2":
                    showCost(sc);
                    break;
                case "3":
                    increaseStudents(sc);
                    break;
                case "4":
                    allocate(sc);
                    break;
                case "5":
                    deallocate(sc);
                    break;
                case "6":
                    addExercise(sc);
                    break;
                case "0":
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Invalid");
                    break;
            }
        }
    }

    private static void showInstances() {
        System.out.println("\nAll course instances:");
        List<CourseInstance> list = courseDAO.getAllInstances();
        for (CourseInstance ci : list) {
            System.out
                    .println(ci.instanceId + " — " + ci.courseName + " (" + ci.period + ") students=" + ci.numStudents);
        }
    }

    private static void showCost(Scanner sc) {
        System.out.print("Instance ID: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        TeachingCost tc = costDAO.getCostForInstance(id);
        if (tc == null) {
            System.out.println("Not found");
            return;
        }
        System.out.printf("Course %s (instance %d, %s %d)%n", tc.courseCode, tc.instanceId, tc.period, tc.year);
        System.out.printf("Planned hours: %.2f -> Planned cost: %.2f KSEK%n", tc.plannedHours, tc.plannedKsek);
        System.out.printf("Actual  hours: %.2f -> Actual  cost: %.2f KSEK%n", tc.actualHours, tc.actualKsek);
    }

    private static void increaseStudents(Scanner sc) {
        System.out.print("Instance ID: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Increase by (enter 100 for task): ");
        int delta = Integer.parseInt(sc.nextLine().trim());
        boolean ok = service.increaseStudentsTransactional(id, delta);
        System.out.println(ok ? "Increased." : "Failed.");
    }

    private static void allocate(Scanner sc) {
        try {
            System.out.print("Employee ID: ");
            int emp = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Instance ID: ");
            int inst = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Activity ID: ");
            int act = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Hours: ");
            double hrs = Double.parseDouble(sc.nextLine().trim());
            boolean ok = service.allocateTeacherTransactional(emp, inst, act, hrs);
            System.out.println(ok ? "Allocated." : "Allocation failed.");
        } catch (Exception e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private static void deallocate(Scanner sc) {
        try {
            System.out.print("Employee ID: ");
            int emp = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Instance ID: ");
            int inst = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Activity ID: ");
            int act = Integer.parseInt(sc.nextLine().trim());
            boolean ok = service.deallocateTeacher(emp, inst, act);
            System.out.println(ok ? "Deallocated." : "Nothing removed.");
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private static void addExercise(Scanner sc) {
        try {
            System.out.print("Instance ID: ");
            int inst = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Employee (to allocate) ID: ");
            int emp = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Planned hours (e.g. 10): ");
            double ph = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Allocated hours (e.g. 5): ");
            double ah = Double.parseDouble(sc.nextLine().trim());
            boolean ok = service.addExerciseAndAllocate(inst, emp, ph, ah);
            System.out.println(ok ? "Exercise added & allocated." : "Failed.");
        } catch (Exception e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }
}
