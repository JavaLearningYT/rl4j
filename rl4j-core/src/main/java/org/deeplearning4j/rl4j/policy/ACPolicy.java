package org.deeplearning4j.rl4j.policy;

import org.deeplearning4j.rl4j.learning.Learning;
import org.deeplearning4j.rl4j.network.ac.ActorCriticCompGraph;
import org.deeplearning4j.rl4j.network.ac.ActorCriticSeparate;
import org.deeplearning4j.rl4j.network.ac.IActorCritic;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;
import java.util.Random;

/**
 * @author rubenfiszel (ruben.fiszel@epfl.ch) on 8/5/16.
 *
 * A stochastic policy thats explore the environment based on
 * the softmax output of the actor critic, but objects constructed
 * without a {@link Random} argument return the max only.
 */
public class ACPolicy<O extends Encodable> extends Policy<O, Integer> {

    final private IActorCritic IActorCritic;
    Random rd;

    public ACPolicy(IActorCritic IActorCritic) {
        this(IActorCritic, null);
    }
    public ACPolicy(IActorCritic IActorCritic, Random rd) {
        this.IActorCritic = IActorCritic;
        this.rd = rd;
    }

    public static <O extends Encodable> ACPolicy<O> load(String path) throws IOException {
        return load(path, (Random)null);
    }
    public static <O extends Encodable> ACPolicy<O> load(String path, Random rd) throws IOException {
        return new ACPolicy<O>(ActorCriticCompGraph.load(path), rd);
    }

    public static <O extends Encodable> ACPolicy<O> load(String pathValue, String pathPolicy) throws IOException {
        return load(pathValue, pathPolicy, null);
    }
    public static <O extends Encodable> ACPolicy<O> load(String pathValue, String pathPolicy, Random rd) throws IOException {
        return new ACPolicy<O>(ActorCriticSeparate.load(pathValue, pathPolicy), rd);
    }

    public IActorCritic getNeuralNet() {
        return IActorCritic;
    }

    public Integer nextAction(INDArray input) {
        INDArray output = IActorCritic.outputAll(input)[1];
        if (rd == null) {
            return Learning.getMaxAction(output);
        }
        float rVal = rd.nextFloat();
        for (int i = 0; i < output.length(); i++) {
            //System.out.println(i + " " + rVal + " " + output.getFloat(i));
            if (rVal < output.getFloat(i)) {
                return i;
            } else
                rVal -= output.getFloat(i);
        }

        throw new RuntimeException("Output from network is not a probability distribution: " + output);
    }

    public void save(String filename) throws IOException {
        IActorCritic.save(filename);
    }

    public void save(String filenameValue, String filenamePolicy) throws IOException {
        IActorCritic.save(filenameValue, filenamePolicy);
    }

}
