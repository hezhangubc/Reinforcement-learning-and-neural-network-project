package myrobot;

import robocode.*;

import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

public class MyRobot extends AdvancedRobot {
    //parameters
    static double gamma = 0.9;
    static double alpha = 0.1;
    static double epsilon = 0.1;

    //controls
    static boolean onPolicy = false;
    static int showWinRoundNum = 100;
    static boolean intermediateReward = false;
    static int qX = 8;
    static int qY = 6;
    static int qBearing = 12;
    static int qDistance = 10;
    static int qHealth = 2;
    static int qHeatgun = 2;
    static int actionNum = 9;
    static int roundNum = 0;
    static int stateNum = qX * qY * qBearing * qDistance * qHealth * qHeatgun;
    static LookUpTable lut = new LookUpTable(stateNum, actionNum);
    static String winFile = System.getProperty("user.dir")
            + "\\robots\\myrobot\\MyRobot.data\\winratio.txt";

    //states
    double realX = 800;
    double realY = 600;
    double reward = 0;

    // state action
    int states = 6;
    int currentAction = 0;
    long prevState;
    int prevAction;
    int index = 0;
    int maxIndex = 0;

    //absolute value and quantized value
    double absBearing;
    double absDistance = 0;
    double absHealth = 100;
    double absX;
    double absY;
    double absGunHeat;
    int quantizedBearing = 0;
    int quantizedDistance = 0;
    int quantizedHealth = 0;
    int quantizedX = 0;
    int quantizedY = 0;
    int quantizedGunHeat = 0;

    //after action
    boolean Action_finished = false;
    boolean Action_executed = false;
    boolean enemyLocked = false;
    boolean alreadyMoved = false;
    ArrayList<Long> stateList;

    public MyRobot() throws IOException {
        stateList = new ArrayList<>();
        for (int i = 0; i < states; i++) {
            stateList.add((long) 0);
        }
    }

    public void run() {
        setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
        setBodyColor(Color.BLACK);
        setGunColor(Color.BLACK);
        setRadarColor(Color.BLACK);
        setScanColor(Color.BLACK);
        setBulletColor(Color.BLACK);
        setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
        turnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right
    }


    public void onStatus(StatusEvent e) {
        RobotStatus St = e.getStatus();
        absX = St.getX();
        absY = St.getY();
        absHealth = getEnergy();
        absGunHeat = getGunHeat();

        // map raw input to quantized state space
        quantizedBearing = quantize(absBearing, -Math.PI, 3 * Math.PI, qBearing - 1);
        quantizedDistance = quantize(absDistance, 0, 1000, qDistance - 1);
        quantizedHealth = quantize(absHealth, 0, 100, qHealth - 1);
        quantizedGunHeat = quantize(absGunHeat, 0, 1.6, qHeatgun - 1);

        // divide X & Y
        quantizedX = quantize(absX, 0, 800, qX - 1);
        quantizedY = quantize(absY, 0, 600, qY - 1);

        stateList.set(0, (long) quantizedBearing);
        stateList.set(1, (long) quantizedDistance);
        stateList.set(2, (long) quantizedHealth);
        stateList.set(3, (long) quantizedX);
        stateList.set(4, (long) quantizedY);
        stateList.set(5, (long) quantizedGunHeat);
        for (int i = 0; i < stateList.size(); i++) {
            System.out.format("%d", stateList.get(i));
        }

        // start learning
        if (enemyLocked) {
            // initial the boolean value for the next round
            if (Action_finished && !Action_executed) {
                Action_finished = false;
                Action_executed = false;
                alreadyMoved = false;
            }

            // start action
            if (!Action_finished && !Action_executed) {
                // given current state, perform action
                prevState = index;
                prevAction = currentAction;

                //on policy sarsa
                if (onPolicy) {
                    index = (int) getIndex();
                    currentAction = e_greedy(index);
                    double temp1 = (1 - alpha) * lut.lookupTable.get((int) prevState).get(prevAction);
                    double temp2 = alpha * (reward + gamma * lut.lookupTable.get(index).get(currentAction));

                    lut.lookupTable.get((int) prevState).set(prevAction, temp1 + temp2);

                    reward = 0;
                }
                //off policy qlearning
                else {
                    index = (int) getIndex();
                    double maxVal = Double.NEGATIVE_INFINITY;
                    currentAction = e_greedy(index);
                    index = (int) getIndex();
                    for (int i = 0; i < lut.lookupTable.get(index).size(); i++) {
                        if (lut.lookupTable.get(index).get(i) > maxVal) {
                            maxVal = lut.lookupTable.get(index).get(i);
                            currentAction = i;
                        }
                    }

                    double temp1 = (1 - alpha) * lut.lookupTable.get((int) prevState).get(prevAction);
                    double temp2 = alpha * (reward + gamma * maxVal);

                    reward = 0;

                    lut.lookupTable.get((int) prevState).set(prevAction, temp1 + temp2);

                }

                Action_finished = false;
                Action_executed = true;

                switch (currentAction) {
                    // up
                    case 0:
                        double current_angle = St.getHeading();

                        if (current_angle <= 180) {
                            turnLeft(current_angle);
                        } else {
                            turnRight(360 - current_angle);
                        }


                        break;


                    // down
                    case 1:

                        current_angle = St.getHeading();

                        if (current_angle <= 180) {
                            turnRight(180 - current_angle);
                        } else {
                            turnLeft(current_angle - 180);
                        }

                        break;

                    // left
                    case 2:
                        current_angle = St.getHeading();

                        if (current_angle <= 90 && current_angle >= 0) {
                            turnLeft(90 + current_angle);
                        } else if (current_angle >= 270 && current_angle <= 359) {
                            turnLeft(current_angle - 270);
                        } else if (current_angle < 270 && current_angle > 90) {
                            turnRight(270 - current_angle);
                        }

                        break;

                    // right
                    case 3:
                        current_angle = St.getHeading();

                        if (current_angle <= 90 && current_angle >= 0) {
                            turnRight(90 - current_angle);
                        } else if (current_angle >= 270 && current_angle <= 359) {
                            turnRight((360 - current_angle) + 90);
                        } else if (current_angle < 270 && current_angle > 90) {
                            turnLeft(current_angle - 90);
                        }


                        break;

                    // northwest
                    case 4:

                        current_angle = St.getHeading();

                        if (current_angle <= 315 && current_angle >= 135) {
                            turnRight(315 - current_angle);
                        } else if (current_angle > 315 && current_angle <= 359) {
                            turnLeft((current_angle - 315));
                        } else if (current_angle < 135 && current_angle >= 0) {
                            turnRight((135 - current_angle) + 180);
                        }

                        break;

                    // northeast
                    case 5:

                        current_angle = St.getHeading();

                        if (current_angle <= 45 && current_angle >= 0) {
                            turnRight(45 - current_angle);
                        } else if (current_angle > 45 && current_angle <= 315) {
                            turnLeft((current_angle - 45));
                        } else if (current_angle > 315 && current_angle <= 359) {
                            turnRight((360 - current_angle) + 45);
                        }

                        break;

                    // southwest
                    case 6:

                        current_angle = St.getHeading();

                        if (current_angle <= 225 && current_angle >= 0) {
                            turnRight(225 - current_angle);
                        } else if (current_angle > 225 && current_angle <= 359) {
                            turnLeft((current_angle - 225));
                        }


                        break;


                    // southeast
                    case 7:

                        current_angle = St.getHeading();

                        if (current_angle <= 135 && current_angle >= 0) {
                            turnRight(135 - current_angle);
                        } else if (current_angle > 135 && current_angle <= 359) {
                            turnLeft((current_angle - 135));
                        }

                        break;


                    case 8:


                        if (getGunHeat() > 0) {
                            //shootingBlanks = true;
                            Action_executed = false;
                            Action_finished = true;
                            break;
                        }

                        fire(3.0);
                        Action_executed = false;
                        Action_finished = true;

                        break;

                }

            }

        }


        // complete action
        if (!Action_finished && Action_executed) {
            switch (currentAction) {
                case 0:
                    if (getTurnRemaining() == 0 && !alreadyMoved) {
                        double upY = ((realY / qY) * (quantizedY + 1)) + (realY / qY) / 2;
                        double dist = upY - St.getY();
                        setAhead(dist);

                        alreadyMoved = true;
                    }


                    if (getDistanceRemaining() == 0) {
                        Action_executed = false;
                        Action_finished = true;

                        break;
                    }

                    break;
                case 1:

                    if (getTurnRemaining() == 0 && !alreadyMoved) {
                        double upY = ((realY / qY) * (quantizedY - 1)) + (realY / qY) / 2;
                        double dist = upY - St.getY();
                        setAhead(dist);

                        alreadyMoved = true;

                    }


                    if (getDistanceRemaining() == 0) {
                        Action_executed = false;
                        Action_finished = true;

                    }

                    break;
                case 2:

                    if (getTurnRemaining() == 0 && !alreadyMoved) {
                        double upX = ((realX / qX) * (quantizedX - 1)) + (realX / qX) / 2;
                        double dist = upX - St.getX();
                        setAhead(-dist);

                        alreadyMoved = true;

                    }


                    if (getDistanceRemaining() == 0) {
                        Action_executed = false;
                        Action_finished = true;

                        break;
                    }

                    break;
                case 3:

                    if (getTurnRemaining() == 0 && !alreadyMoved) {
                        double upX = ((realX / qX) * (quantizedX + 1)) + (realX / qX) / 2;
                        double dist = upX - St.getX();
                        setAhead(dist);
                        //execute();

                        alreadyMoved = true;

                    }


                    if (getDistanceRemaining() == 0) {
                        Action_executed = false;
                        Action_finished = true;

                        break;
                    }

                    break;
                case 4:

                    if (getTurnRemaining() == 0 && !alreadyMoved) {
                        double upX = ((realX / qX) * (quantizedX + 1)) + (realX / qX) / 2;
                        double distX = upX - St.getX();
                        double upY = ((realY / qY) * (quantizedY + 1)) + (realY / qY) / 2;
                        double distY = upY - St.getY();

                        double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
                        setAhead(dist);

                        alreadyMoved = true;

                    }


                    if (getDistanceRemaining() == 0) {
                        Action_executed = false;
                        Action_finished = true;

                        break;
                    }

                    break;
                case 5:

                    if (getTurnRemaining() == 0 && !alreadyMoved) {
                        double upX = ((realX / qX) * (quantizedX - 1)) + (realX / qX) / 2;
                        double distX = upX - St.getX();
                        double upY = ((realY / qY) * (quantizedY + 1)) + (realY / qY) / 2;
                        double distY = upY - St.getY();

                        double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
                        setAhead(dist);

                        alreadyMoved = true;
                    }


                    if (getDistanceRemaining() == 0) {
                        Action_executed = false;
                        Action_finished = true;

                        break;
                    }

                    break;
                case 6:

                    if (getTurnRemaining() == 0 && !alreadyMoved) {
                        double upX = ((realX / qX) * (quantizedX - 1)) + (realX / qX) / 2;
                        double distX = upX - St.getX();
                        double upY = ((realY / qY) * (quantizedY - 1)) + (realY / qY) / 2;
                        double distY = upY - St.getY();

                        double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
                        setAhead(dist);

                        alreadyMoved = true;
                    }


                    if (getDistanceRemaining() == 0) {
                        Action_executed = false;
                        Action_finished = true;

                        break;
                    }

                    break;
                case 7:

                    if (getTurnRemaining() == 0 && !alreadyMoved) {
                        double upX = ((realX / qX) * (quantizedX + 1)) + (realX / qX) / 2;
                        double distX = upX - St.getX();
                        double upY = ((realY / qY) * (quantizedY - 1)) + (realY / qY) / 2;
                        double distY = upY - St.getY();

                        double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
                        setAhead(dist);

                        alreadyMoved = true;
                    }


                    if (getDistanceRemaining() == 0) {
                        Action_executed = false;
                        Action_finished = true;

                        break;
                    }

                    break;

            }
        }


    }

    public void onScannedRobot(ScannedRobotEvent e) {

        enemyLocked = true;
        // read raw sensor input upon enemy scanned
        absBearing = e.getBearingRadians() + getHeadingRadians();//absolute bearing of enemies
        absDistance = e.getDistance(); // enemies distance
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);//later velocity of enemies

        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
        // turn gun to face enemy

        double gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 22);//amount to turn our gun, lead just a little bit
        setTurnGunRightRadians(gunTurnAmt); //turn our gun

    }


    //rewards
    public void onBulletHit(BulletHitEvent e) {
        if (intermediateReward) {
            reward = 30;
        }
    }


    public void onHitRobot(HitRobotEvent e) {
        if (intermediateReward) {
            reward = -30;
        }
    }


    public void onBulletMissed(BulletMissedEvent e) {
        if (intermediateReward) {
            reward = -10;
        }

    }


    public void onHitByBullet(HitByBulletEvent e) {
        if (intermediateReward) {
            reward = -30;
        }

    }


    public void onHitWall(HitWallEvent e) {
        if (intermediateReward) {
            reward = -10;
        }
    }

    public void onDeath(DeathEvent e) {
        double temp1 = (1 - alpha) * lut.lookupTable.get(index).get(currentAction);
        double temp2 = alpha * (-100);
        lut.lookupTable.get(index).set(currentAction, temp1 + temp2);
        reward = 0;
        roundNum++;
        if (roundNum % showWinRoundNum == 0) {
            double winRatio = (double) lut.cumWinNum / showWinRoundNum;
            saveWinRatio(winRatio);
            lut.cumWinNum = 0;
        }
    }

    public void onWin(WinEvent e) {
        double temp1 = (1 - alpha) * lut.lookupTable.get(index).get(currentAction);
        double temp2 = alpha * (100);
        lut.lookupTable.get(index).set(currentAction, temp1 + temp2);
        reward = 0;
        lut.cumWinNum += 1;
        roundNum++;
        if (roundNum % showWinRoundNum == 0) {
            double winRatio = (double) lut.cumWinNum / showWinRoundNum;
            saveWinRatio(winRatio);
            lut.cumWinNum = 0;
        }
    }

    public void onBattleEnded(BattleEndedEvent event) {
    }

    public void saveWinRatio(double winRatio) {
        PrintStream w = null;
        try {
            RobocodeFileWriter fileWriter = new RobocodeFileWriter(winFile, true);
            String s = String.format("%f", winRatio);
            fileWriter.write(s);
            fileWriter.write(System.getProperty("line.separator"));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int e_greedy(int state_index) {
        Random r = new Random();
        double rand = r.nextDouble();
        double maxVal = Double.NEGATIVE_INFINITY;
        if (rand <= 1 - epsilon) {
            for (int i = 0; i < lut.lookupTable.get(state_index).size(); i++) {
                if (lut.lookupTable.get(state_index).get(i) > maxVal) {
                    maxVal = lut.lookupTable.get(state_index).get(i);
                    maxIndex = i;
                }
            }
            currentAction = maxIndex;
        } else {
            int randAction = r.nextInt(actionNum);
            currentAction = randAction;
        }
        return currentAction;
    }

    public int quantize(double in, double min, double max, double quantization) {
        double temp = (in - min) / (max - min);
        return (int) Math.round(temp * quantization);
    }

    public long getIndex() {
        return stateList.get(0)
                + stateList.get(1) * (qBearing)
                + stateList.get(2) * (qBearing * qDistance)
                + stateList.get(3) * (qBearing * qDistance * qHealth)
                + stateList.get(4) * (qBearing * qDistance * qHealth * qX)
                + stateList.get(5) * (qBearing * qDistance * qHealth * qX * qY);
    }


}



