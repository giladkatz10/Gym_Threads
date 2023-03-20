
package java_threads;

public class App {

    public static void main(String[] args) {
        int num_machines = 2;

        Gym g = new Gym(5);
        g.addMachine(MachineType.BARBELL, num_machines);
        g.addMachine(MachineType.LEGCURLMACHINE, num_machines);
        g.addMachine(MachineType.SQUATMACHINE, 1);
        g.addWeight(Weight.SMALL_5LBS, 100);
        g.addWeight(Weight.MEDIUM_10LBS, 100);
        g.addWeight(Weight.LARGE_25LBS, 100);
        g.addMember(1);
        g.addMember(2);
        g.fillMembers();
        g.printGym();
        
        g.openForTheDay();
    }
}
