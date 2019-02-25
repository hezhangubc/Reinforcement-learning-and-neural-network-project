package myrobot2;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.*;

public class NeuralNet {
    int argNumInputs;
    int argNumHidden;
    int argNumOutputs;
    double argLearningRate;
    double argMomentumTerm;
    double argBias;

    double[] expandedInput;
    double[] expandedHidden;
    double[][] inputWeight;
    double[][] hiddenWeight;
    double[] hiddenY;
    double[][] inputDelta;
    double[] outputY;
    double[][] hiddenDelta;


    public NeuralNet(
            int inputNodes,
            int hiddenNodes,
            int outputNodes,
            double learningRate,
            double momentumTerm,
            double bias) {
        argNumInputs = inputNodes;
        argNumHidden = hiddenNodes;
        argNumOutputs = outputNodes;
        argLearningRate = learningRate;
        argMomentumTerm = momentumTerm;
        argBias = bias;

        expandedInput = new double[argNumInputs + 1];
        expandedHidden = new double[argNumHidden + 1];
        inputWeight = new double[argNumInputs + 1][argNumHidden];
        hiddenWeight = new double[argNumHidden + 1][argNumOutputs];
        inputDelta = new double[argNumInputs + 1][argNumHidden];
        hiddenDelta = new double[argNumHidden + 1][argNumOutputs];
        hiddenY = new double[argNumHidden];
        outputY = new double[argNumOutputs];

    }

    public static void showResult(NeuralNet nn, double[] Xhat) {
        System.out.println(Arrays.toString(Xhat) + "Result: " + Arrays.toString(nn.predict(Xhat)));
    }

    public double sigmoid(double x) {
        return 1.0 / (1.0 + exp(-x));
    }

    public double bipolarSigmoid(double x){
        return 2.0/(1.0+exp(-x))-1.0;
    }

    public double customSigmoid(double x, double a, double b) {
        return (b - a) * sigmoid(x) + a;
    }

    public double rand(double a, double b) {
        return (b - a) * random() + a;
    }

    public double gaussian(double a) {
        Random r = new Random();
        return r.nextGaussian() / a;
    }

    public void initializeWeights(double a, double b) {
        for (int i = 0; i < inputWeight.length; i++) {
            for (int j = 0; j < inputWeight[0].length; j++) {
                inputWeight[i][j] = gaussian(1000);
                //inputWeight[i][j]=gaussian((argNumInputs+argNumHidden)/2);
                //inputWeight[i][j]=rand(-sqrt(6/(argNumInputs+argNumHidden)),sqrt(6/(argNumInputs+argNumHidden)));
                inputDelta[i][j] = 0;
            }
        }

        for (int i = 0; i < hiddenWeight.length; i++) {
            for (int j = 0; j < hiddenWeight[0].length; j++) {
                hiddenWeight[i][j] = gaussian(1000);
                //hiddenWeight[i][j]=gaussian((argNumOutputs+argNumHidden)/2);
                //hiddenWeight[i][j]=rand(-sqrt(6/(argNumOutputs+argNumHidden)),sqrt(6/(argNumOutputs+argNumHidden)));
                hiddenDelta[i][j] = 0;
            }
        }
    }

    public void forwardPropagation(double[] X) {
        System.arraycopy(X, 0, expandedInput, 0, X.length);
        expandedInput[argNumInputs] = argBias;

        //forward propagation
        for (int i = 0; i < argNumHidden; i++) {
            double tmp = 0;
            for (int j = 0; j < argNumInputs + 1; j++) {
                tmp += expandedInput[j] * inputWeight[j][i];
            }
            hiddenY[i] = bipolarSigmoid(tmp);
        }

        //expand hidden layer
        for (int i = 0; i < argNumHidden; i++) {
            expandedHidden[i] = hiddenY[i];
        }
        System.arraycopy(hiddenY, 0, expandedHidden, 0, hiddenY.length);
        expandedHidden[argNumHidden] = bipolarSigmoid(argBias);


        for (int i = 0; i < argNumOutputs; i++) {
            double tmp = 0;
            for (int j = 0; j < argNumHidden + 1; j++) {
                tmp += expandedHidden[j] * hiddenWeight[j][i];
            }
            outputY[i] = bipolarSigmoid(tmp);
        }
    }

    public double[] predict(double[] X) {
        forwardPropagation(X);
        return outputY.clone();
    }

    public double backPropagation(double[] C) {
        double error = 0;
        for (int i = 0; i < argNumOutputs; i++) {
            error += 0.5 * (C[i] - outputY[i])
                    * (C[i] - outputY[i]);
        }

        //update hidden weight
        for (int i = 0; i < argNumHidden + 1; i++) {
            for (int j = 0; j < argNumOutputs; j++) {
                double change = (C[j] - outputY[j])
                        * 0.5 * (1 - outputY[j] * outputY[j]);
                hiddenDelta[i][j] = argMomentumTerm * hiddenDelta[i][j] +
                        argLearningRate * change * expandedHidden[i];
                hiddenWeight[i][j] += hiddenDelta[i][j];
            }
        }

        //update input weight
        for (int i = 0; i < argNumInputs + 1; i++) {
            for (int j = 0; j < argNumHidden; j++) {
                double change = 0;
                for (int k = 0; k < argNumOutputs; k++) {
                    change += (C[k] - outputY[k])
                            * 0.5 * (1 - outputY[k] * outputY[k])
                            * hiddenWeight[j][k];
                }
                change *= 0.5 * (1 - hiddenY[j] * hiddenY[j]);
                inputDelta[i][j] = argMomentumTerm * inputDelta[i][j] +
                        argLearningRate * change * expandedInput[i];
                inputWeight[i][j] += inputDelta[i][j];

            }
        }
        return error;
    }

    public int train(double[][] Xs, double[][] ys, int iteration, boolean fromScratch, boolean saveError) {
        if(fromScratch){
            initializeWeights(-0.5, 0.5);
        }else{
            try{
                loadParameters();
            }catch (IOException e){

            }
        }
        double pastError = Double.POSITIVE_INFINITY;
        for (int it = 0; it < iteration; it++) {
            double error = 0;
            for (int index = 0; index < Xs.length; index++) {
                double[] X = Xs[index];
                double[] y = ys[index];
                forwardPropagation(X);
                error += backPropagation(y);
            }

            if (pastError < error) {
                //argLearningRate *= 0.95;
            }
            pastError = error;

            if (saveError) {
                DataTransfer.saveValue(error, "error.csv");
            }

            System.out.println(error);
            if (error < 0.05) {
                saveParameters();
                return it;
            }

        }
        saveParameters();
        return 0;
    }

    public void saveParameters() {
        DataTransfer.saveData(inputWeight, "weight1.csv");
        DataTransfer.saveData(hiddenWeight, "weight2.csv");
        DataTransfer.saveData(inputDelta, "delta1.csv");
        DataTransfer.saveData(hiddenDelta, "delta2.csv");

    }

    public void loadParameters() throws IOException {
        try {
            DataTransfer.loadData(inputWeight, "weight1.csv");
            DataTransfer.loadData(hiddenWeight, "weight2.csv");
            DataTransfer.loadData(inputDelta, "delta1.csv");
            DataTransfer.loadData(hiddenDelta, "delta2.csv");
        }
        catch (IOException e){
            e.printStackTrace();
        }


}

}
