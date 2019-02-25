package myrobot2;
import java.io.IOException;

import static java.lang.Math.exp;

public class NeuralNetTest {

    static int stateNum = 192;
    static int actionNum = 9;
    static int states = 5;

    public static void sigmoidMatrix(double[][] mat){
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat[i].length;j++){
                mat[i][j]=1.0/(1.0+exp(-mat[i][j]));
            }
        }
    }

    private static void trainLUT1(){
        NeuralNet nn=new NeuralNet(5,
                20,
                9,
                0.01,
                0.0,
                1.0);
        DataTransfer.deleteOldFile("error.csv");

        double[][] Xs= new double[stateNum][states];
        double[][] ys=new double[stateNum][actionNum];
        try{
            DataTransfer.loadData(Xs,"states.csv");
            DataTransfer.loadData(ys,"LookUpTable.csv");
        }catch(IOException e){

        }

        //printMat(Xs);
        //sigmoidMat(ys);


        int it=nn.train(Xs,ys,100000,true,true);
        System.out.println(it);

        for(int i=0;i<stateNum;i++){
            ys[i]=nn.predict(Xs[i]);
        }
        DataTransfer.saveData(ys,"LookUpTable_generated.csv");
    }

    public static void main(String[] args){
        trainLUT1();
    }
}
