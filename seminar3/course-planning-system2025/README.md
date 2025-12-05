Compile all files
javac -d bin -cp lib/postgresql-42.7.4.jar src/controller/*.java src/integration/*.java src/model/*.java src/view/*.java

Run the program
java -cp "bin;lib/postgresql-42.7.4.jar" view.MainCLI

In Visual Studio Code, from the course-planning-system2025 folder:
Run MainCLI (Run -> Run Java) or click the Run icon next to public static void main.

==== COURSE PLANNING SYSTEM ====
1. Show all course instances
2. Show teaching cost for instance (planned + actual)
3. Increase +100 students to instance
4. Allocate teacher to activity
5. Deallocate teacher from instance/activity
6. Add new activity 'Exercise' (planned + allocate)
0. Exit
