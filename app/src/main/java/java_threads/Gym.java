package java_threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class Gym {
    private final int totalGymMembers;
    private Map<MachineType, Integer> machines;
    private Map<Weight, Integer> weights;
    private Map<Integer, Member> members;
    private ReentrantLock lock;
    private Condition performCondition;

    public Gym() {
        this(new Random().nextInt(7) + 1); // random num in range 1-7
    }

    public Gym(int totalGymMembers) {
        this.totalGymMembers = totalGymMembers;
        this.machines = new HashMap<>();
        this.members = new HashMap<>();
        this.weights = new HashMap<>();
        this.lock = new ReentrantLock();
        this.performCondition = this.lock.newCondition();

        for (Weight w : Weight.values()) {
            this.weights.put(w, 0);
        }
        for (MachineType mt : MachineType.values()) {
            this.machines.put(mt, 0);
        }
    }

    public void addMachine(MachineType machineType, Integer amount) {
        // add to existing machine
        if(machines.containsKey(machineType))
        {
            machines.replace(machineType, amount + this.machines.get(machineType));
        } else {
            this.machines.put(machineType, amount);
        }
    } 

    public void addWeight(Weight weight, Integer amount) {
        if(weights.containsKey(weight))
        {
            weights.replace(weight, amount + this.weights.get(weight));
        } else {
            this.weights.put(weight, amount);
        }
    } 

    public void addMember(int id) {
        if (this.members.containsKey(id)) {
            System.out.println("Member id already exists: " + id);
        }
        else if(this.members.size() < this.totalGymMembers) {
            List<MachineType> availableMachines = getAvailableMachines();
                    
            this.members.put(id, new Member(id, availableMachines, weights));
        } else {
            System.out.println("The Gym is full!");
        }   
    }

    private List<MachineType> getAvailableMachines() {
        List<MachineType> availableMachines = this.machines.entrySet().stream()
                .filter(m -> m.getValue() > 0)
                .map(m -> m.getKey())
                .collect(Collectors.toList());
        return availableMachines;
    }

    /**
     * Reach the maximum capacity of Members, provide unique id per one.
     */
    public void fillMembers() {
        if (this.members.size() == this.totalGymMembers) {
            System.out.println("[fillMembers] Gym is full of members, Sorry!");
        } else {
            try {
                Set<Integer> existingIds = new HashSet<>(this.members.keySet()); 
                List<MachineType> availableMachines = getAvailableMachines();

                Integer nextId = 1;
                while (this.members.size() < this.totalGymMembers) {
                    while (existingIds.contains(nextId)) {  // Ensure that the following ID is unique
                        nextId++;
                    }
                    // Add the next unique ID to the map
                    this.members.put(nextId, new Member(nextId, availableMachines, this.weights));  
                    existingIds.add(nextId);  // Add the next unique ID to the set of existing IDs
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void printGym ()  {
        for (Map.Entry<MachineType, Integer> pair: this.machines.entrySet()) {
            System.out.println("Machine:\t" + pair.getKey().getMachineName() + ",\tamount:\t" + pair.getValue());
        }
        for (Map.Entry<Weight, Integer> pair: this.weights.entrySet()) {
            System.out.println("Weight:\t" + pair.getKey() + ",\t amount:\t" + pair.getValue());
        }
        for (Map.Entry<Integer, Member> pair: this.members.entrySet()) {
            System.out.println("------------------------------");
            System.out.println("Id Member: " + pair.getKey()+" ,Exercises: ");
            pair.getValue().printExercises();
            System.out.println("------------------------------");
        }
    }

    /**
     * Opens the gym. Allows gym members to perform their routine
     */
    public void openForTheDay() {
        List<Thread> gymMembersRoutines = new ArrayList<>();
        
        this.members.forEach((id, member) -> {
            Thread thread = new Thread(() -> {
                try {
                    member.performRoutine(this);
                } catch (Exception e) {
                    System.out.println("[openForTheDay] An exception occurred during members' routine\n" 
                        + e.getMessage());
                }
            });
            gymMembersRoutines.add(thread);
        });

        Thread supervisor = this.createSupervisor(gymMembersRoutines);

        gymMembersRoutines.forEach(Thread::start);
        supervisor.start();
    }

    /**
     * @param threads: The gym members
     * @return supervisor Thread, which monitors the members' activity
     */
    private Thread createSupervisor(List<Thread> threads) {
        Thread supervisor = new Thread(() -> {
            while(true) {
                List<String> runningThreads = threads.stream()
                    .filter(Thread::isAlive)
                    .map(Thread::getName)
                    .collect(Collectors.toList());

                System.out.println(Thread.currentThread().getName() + 
                                    " reports: " + runningThreads.size() + 
                                    " people still working out: " + runningThreads);

                if (runningThreads.isEmpty()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println(Thread.currentThread().getName() + ": All Threads have completed!!!!");   
        });

        supervisor.setName("Gym Staff");

        return supervisor;
    }

    public void performExercise(Exercise exercise, Member member) {
        try {
            // System.out.println("Member " + member.getId() + " Inside Try -----");
            lock.lock();
            // System.out.println("Member " + member.getId() + " Inside Lock *****");
            while (!isMachineAvailable(exercise.getMachineType()) 
                    || !istWeightAvailable(exercise.getWeights())) {
                System.out.println("Member: " + member.getId() 
                + " is waiting for " + exercise.toString());
                performCondition.await();
            }
            System.out.println("Gym Member: " + member.getId() 
                                + " performing exercise: " + exercise);

            aquireMachine(exercise.getMachineType());
            aquireWeight(exercise.getWeights());
            
            printMachineAvailable("Aquire " + member.getId());
            printWeightsAvailability("Aquire " + member.getId());

        } catch (Exception exception) {
            System.out.println("EXCEPTION!!!! [performExercise]");
            exception.printStackTrace();
        } finally {
            // System.out.println("Member " + member.getId() + " Unlock -------");
            lock.unlock();
        }
        
        try {
            // System.out.println("Member " + member.getId() + " perfoming exercise &&&&&&&");
            Thread.sleep(exercise.getDuration());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public void releaseAll(Exercise exercise, Member member){
        try {
            lock.lock();
            // System.out.println("Member " + member.getId() + " Inside releaseAll Lock ^^^^^^");
            releaseMachine(exercise.getMachineType());
            releaseWeights(exercise.getWeights());
            System.out.println("Gym Member: " + member.getId() 
            + " Finished performing the exercise: " + exercise.getMachineType());
            
            printMachineAvailable("Release: " + member.getId());
            printWeightsAvailability("Release: " + member.getId());
            performCondition.signalAll();  

        } catch (Exception exception) {
            System.out.println("EXCEPTION!!!! [releaseAll]");
            exception.printStackTrace();
        } finally {
            // System.out.println("Member " + member.getId() + " releaseAll Unlock %%%%%%%%");
            lock.unlock();
        }
    }

    public boolean isMachineAvailable(MachineType machine) {
        return this.machines.get(machine) > 0;
    }

    public  void aquireMachine(MachineType machine) {
        this.machines.replace(machine, machines.get(machine) - 1);
    }

    public  void releaseMachine(MachineType machine) {
        this.machines.replace(machine, machines.get(machine) + 1);
    }

    /**
     * @param weight: map of the weights types and the amount needed for the exercise
     * @return true if there are enough weights for each weight type. false otherwise.
     */
    public boolean istWeightAvailable(Map<Weight, Integer> weight) {
        for (Map.Entry<Weight, Integer> pair: weight.entrySet()) {
            if (this.weights.get(pair.getKey()) < pair.getValue()) {
                return false;
            }
        }
                            
        return true;
    }

    public void aquireWeight(Map<Weight, Integer> weight) {
        for (Map.Entry<Weight, Integer> pair: weight.entrySet()) {
            this.weights.replace(pair.getKey(), this.weights.get(pair.getKey()) - pair.getValue());
        }
    }

    public void releaseWeights(Map<Weight, Integer> weight) {
        for (Map.Entry<Weight, Integer> pair: weight.entrySet()) {
            this.weights.replace(pair.getKey(), this.weights.get(pair.getKey()) + pair.getValue());
        }
    }

    public void printMachineAvailable(String id) {
        StringBuilder status = new StringBuilder();
        status.append("\n-------- MACHINE_AVAILABILITY_CHECK: " + id + " --------\n");
        for (Map.Entry<MachineType, Integer> pair: this.machines.entrySet()) {
            status.append(pair.getKey() + ": There are " + pair.getValue() + " Left.\n");
        }
        System.out.println(status.toString());
    }

    public void printWeightsAvailability(String id) {
        // synchronized (this.weights) {
        StringBuilder status = new StringBuilder();
        status.append("\n********* WEIGHTS_AVAILABILITY_CHECK: " + id + " *********\n");
        for (Map.Entry<Weight, Integer> pair: this.weights.entrySet()) {
            status.append(pair.getKey() + ": There are " + pair.getValue() + " Left.\n");
        }
        System.out.println(status.toString());
    }
    // }
}
