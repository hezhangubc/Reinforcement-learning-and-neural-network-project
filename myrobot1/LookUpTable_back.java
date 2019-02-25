package myrobot1;

import java.util.ArrayList;

public class LookUpTable_back {
    public ArrayList<ArrayList<Double>> lookupTable;
    public int States;
    public int Actions;
    public int cumWinNum;

    LookUpTable_back(int States, int Actions) {
        this.States = States;
        this.Actions = Actions;
        initialiseLUT();
    }

    public void initialiseLUT() {
        lookupTable = new ArrayList<>();

        // add rows to QTable (rows are states)
        for (int i = 0; i < States; i++) {
            lookupTable.add(new ArrayList<>());
        }

        for (int i = 0; i < States; i++) {
            for (int j = 0; j < Actions; j++) {
                lookupTable.get(i).add(0.0);
            }
        }
    }
}
