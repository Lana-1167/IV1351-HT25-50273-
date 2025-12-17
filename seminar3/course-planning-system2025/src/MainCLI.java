import model.CourseInstance;
import model.ExerciseAllocationInfo;
import model.TeachingCost;
import service.CourseService;

import java.util.List;
import java.util.Scanner;

public class MainCLI {

    private static final CourseService service = new CourseService();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n==== COURSE PLANNING SYSTEM ====");
            System.out.println("1. Show all course instances");
            System.out.println("2. Show teaching cost for instance");
            System.out.println("3. Increase +100 students");
            System.out.println("4. Allocate teacher");
            System.out.println("5. Deallocate teacher");
            System.out.println("6. Add new activity 'Exercise'");
            System.out.println("7. Show Exercise allocations for teacher");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            switch (sc.nextLine().trim()) {
                case "1" -> showInstances();
                case "2" -> showCost(sc);
                case "3" -> increaseStudents(sc);
                case "4" -> allocate(sc);
                case "5" -> deallocate(sc);
                case "6" -> addExercise(sc);
                case "7" -> showExerciseAllocations(sc);

                case "0" -> {
                    System.out.println("Bye!");
                    return;
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }

    private static void showInstances() {
        List<CourseInstance> list = service.getAllInstances();
        for (CourseInstance ci : list) {
            System.out.println(ci.instanceId + " — " + ci.courseName +
                    " (" + ci.period + "), students=" + ci.numStudents);
        }
    }

    private static void showCost(Scanner sc) {
        System.out.print("Instance ID: ");
        int id = Integer.parseInt(sc.nextLine());
        TeachingCost tc = service.getTeachingCost(id);

        if (tc == null) {
            System.out.println("Not found");
            return;
        }

        System.out.printf("Course %s (%s %d)%n", tc.courseCode, tc.period, tc.year);
        System.out.printf("Planned: %.2f h → %.2f KSEK%n", tc.plannedHours, tc.plannedKsek);
        System.out.printf("Actual : %.2f h → %.2f KSEK%n", tc.actualHours, tc.actualKsek);
    }

    private static void increaseStudents(Scanner sc) {
        System.out.print("Instance ID: ");
        int id = Integer.parseInt(sc.nextLine());
        boolean ok = service.increaseStudentsTransactional(id, 100);
        System.out.println(ok ? "Students increased." : "Failed.");
    }

    private static void allocate(Scanner sc) {
        System.out.print("Employee ID: ");
        int emp = Integer.parseInt(sc.nextLine());
        System.out.print("Instance ID: ");
        int inst = Integer.parseInt(sc.nextLine());
        System.out.print("Activity ID: ");
        int act = Integer.parseInt(sc.nextLine());
        System.out.print("Hours: ");
        double hrs = Double.parseDouble(sc.nextLine());

        boolean ok = service.allocateTeacherTransactional(emp, inst, act, hrs);
        System.out.println(ok ? "Allocated." : "Allocation failed.");
    }

    private static void deallocate(Scanner sc) {
        System.out.print("Employee ID: ");
        int emp = Integer.parseInt(sc.nextLine());
        System.out.print("Instance ID: ");
        int inst = Integer.parseInt(sc.nextLine());
        System.out.print("Activity ID: ");
        int act = Integer.parseInt(sc.nextLine());

        boolean ok = service.deallocateTeacher(emp, inst, act);
        System.out.println(ok ? "Deallocated." : "Nothing removed.");
    }

    private static void addExercise(Scanner sc) {
        System.out.print("Instance ID: ");
        int inst = Integer.parseInt(sc.nextLine());
        System.out.print("Employee ID: ");
        int emp = Integer.parseInt(sc.nextLine());
        System.out.print("Planned hours: ");
        double ph = Double.parseDouble(sc.nextLine());
        System.out.print("Allocated hours: ");
        double ah = Double.parseDouble(sc.nextLine());

        boolean ok = service.addExerciseAndAllocate(inst, emp, ph, ah);
        System.out.println(ok ? "Exercise added." : "Failed.");
    }

    private static void showExerciseAllocations(Scanner sc) {

        System.out.print("Employee ID: ");
        int empId = Integer.parseInt(sc.nextLine().trim());

        List<ExerciseAllocationInfo> list = service.getExerciseAllocationsForTeacher(empId);

        if (list.isEmpty()) {
            System.out.println("No Exercise allocations found.");
            return;
        }

        System.out.println("\nExercise allocations:");
        for (ExerciseAllocationInfo info : list) {
            System.out.printf(
                    "Teacher: %s | Course: %s | Instance: %d | %s %d | Hours: %.2f%n",
                    info.teacherName,
                    info.courseCode,
                    info.instanceId,
                    info.period,
                    info.year,
                    info.allocatedHours);
        }
    }

}
