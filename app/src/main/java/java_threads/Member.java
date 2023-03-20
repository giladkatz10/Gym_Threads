package java_threads;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Member {
    private final int id;
    private final List<Exercise> exercises;
    private final int routine_length = 4;

    public Member(int id, List<MachineType> machines, Map<Weight, Integer> gymWeights) {
        this.id = id;
        this.exercises = this.generateRoutine(machines, gymWeights);
    }

    public int getId() {
        return this.id;
    }
    public List<Exercise> getExercises()
    {
        return this.exercises;
    }

    public List<Exercise> generateRoutine(List<MachineType> machines, Map<Weight, Integer> gymWeights) {
        return IntStream.range(0, routine_length)
                    .mapToObj(_i -> new Exercise(machines, gymWeights))
                    .collect(Collectors.toList());
    }

    public void printExercises() {
        for (Exercise ex : this.exercises) {
            System.out.println(ex);
        }
    }

    /**
     * Perform the member's exercises routine 
     * @param gym: The gym instance
     * @throws InterruptedException
     */
    public void performRoutine(Gym gym) throws InterruptedException {
        this.exercises.forEach(exercise -> {
            System.out.println("CHECK - Gym Member: " + this.getId() 
                + " can performing the exercise: " + exercise.getMachineType() + "???");

            gym.performExercise(exercise, this);
            gym.releaseAll(exercise, this);
        });
    }
}
