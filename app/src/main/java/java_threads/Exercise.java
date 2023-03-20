package java_threads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Represents a single exercise in the gym
public class Exercise {
    private final MachineType machine; // Machine type used for the exercise
    private final Map<Weight, Integer> weightSet; // Amount of weights for the exercise
    private final int duration; 

    //Creates a random exercise
    public Exercise(List<MachineType> machines, Map<Weight, Integer> gymWeights) {
        this.machine = getRandomItem(machines);
        this.weightSet = new HashMap<>();
        insertWeights(gymWeights);

        this.duration = 1000 + new Random().nextInt(2000);
        }

    private void insertWeights(Map<Weight, Integer> gymWeights) {
        // insert weightSet for each type of weight.
        for (Map.Entry<Weight, Integer> pair: gymWeights.entrySet()){
            // if there is no weight for MachineType
            if (pair.getValue() == 0) {
                weightSet.put(pair.getKey(), 0);
                continue;
            }
            int randomWeightAmout = new Random().nextInt(gymWeights.get(pair.getKey()));
            weightSet.put(pair.getKey(), (int) Math.ceil(randomWeightAmout / 2)); 
            // weightSet.put(pair.getKey(), randomWeightAmout); 
        }
    }

    // Returns a random piece of gym equipment
    // Tried Generics, just for fun
   public static <T> T getRandomItem(List<T> availableMachine) {
        return availableMachine.get(new Random().nextInt(availableMachine.size()));
    }

    public int getDuration() {
        return duration;
    }

    public Map<Weight, Integer> getWeights() {
        return this.weightSet;
    }

    public MachineType getMachineType() {
        return this.machine;
    }

    @Override
    public String toString() {
        int totalWeight = (this.weightSet.get(Weight.SMALL_5LBS) * 5) 
                            + (this.weightSet.get(Weight.MEDIUM_10LBS) * 10) 
                            + (this.weightSet.get(Weight.LARGE_25LBS) * 25);
        String weightsMessage = totalWeight + " lbs of total weightSet consisting of " 
            + this.weightSet.get(Weight.SMALL_5LBS) + " x 5 lbs weights, " 
            + this.weightSet.get(Weight.MEDIUM_10LBS) + " x 10 lbs weights, " 
            + this.weightSet.get(Weight.LARGE_25LBS) + " x 25 lbs weights.";
        return this.machine.getMachineName() + " with " + weightsMessage + "\n";
    }
}