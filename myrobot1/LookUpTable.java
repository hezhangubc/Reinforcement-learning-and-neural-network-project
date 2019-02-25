package myrobot1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LookUpTable {
    public double[][] lookupTable;
    public int States;
    public int Actions;
    public int cumWinNum;

    LookUpTable(int States, int Actions, boolean initial, String oldLUT) {
        this.States = States;
        this.Actions = Actions;

        if(initial){
            initialiseLUT();
        }else{
            try {
                loadLUT(oldLUT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void initialiseLUT() {
        lookupTable = new double[States][Actions];

        //initialize to 0
        for (int i = 0; i < States; i++) {
            for (int j = 0; j < Actions; j++) {
                lookupTable[i][j] = 0;
            }
        }
    }

    public void loadLUT(String oldLUT) throws IOException {
        lookupTable = new double[States][Actions];
        BufferedReader reader = new BufferedReader(new FileReader(oldLUT));
        String line = reader.readLine();
        try {
            int i = 0;
            while (line != null) {
                String splitLine[] = line.split(",");
                for (int j = 0; j < splitLine.length; j++) {
                    lookupTable[i][j] = Double.valueOf(splitLine[j]).doubleValue();
                }
                i++;
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
    }

}
