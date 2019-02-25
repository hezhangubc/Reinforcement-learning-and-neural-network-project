package myrobot2;
import java.io.*;

public class DataTransfer {

    public static void deleteOldFile(String oldfile) {
        File oldFile = new File(oldfile);
        oldFile.delete();
    }

    public static void printMatrix(double[][] matrix){
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[i].length;j++){
                System.out.print(matrix[i][j]+" ");
            }
            System.out.print("\r\n");
        }
    }

    public static void loadData(double[][] data,String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(
                System.getProperty("user.dir")
                        +System.getProperty("file.separator")
                        +filename));
        String line = reader.readLine();
        try {
            int i = 0;
            while (line != null) {
                String splitLine[] = line.split(",");
                for (int j = 0; j < splitLine.length; j++) {
                    data[i][j] = Double.valueOf(splitLine[j]).doubleValue();
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

    public static void saveValue(double single, String filename) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filename, true);
            String s = String.format("%f", single);
            fileWriter.write(s);
            fileWriter.write(System.getProperty("line.separator"));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void saveData(double[][] data,String filename) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(System.getProperty("user.dir")
                    +System.getProperty("file.separator")
                    +filename);
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    fileWriter.write(data[i][j]+"");
                    if(j<data[i].length-1){
                        fileWriter.write( ",");
                    }
                }
                if(i<data.length-1){
                    fileWriter.write(System.getProperty("line.separator"));
                }
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
