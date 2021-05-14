/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dungns.logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

/**
 * defines database communication logic
 * @author dungns
 */
public class FileHandler {
    private static int cols = Integer.parseInt(System.getProperty("cols", "1"));
    private static int rows = Integer.parseInt(System.getProperty("rows", "1"));
    private static String filePath = System.getProperty("filePath", "data.txt");
    
    /**
     * write grids data to file
     */
    public static void writeFile(int[][] seatGrid, String[][] idGrid) throws Exception {
        String storedStr = dataToString(seatGrid, idGrid);
        OutputStream os = new FileOutputStream(filePath);
        if(os == null)
            return;
        try {
            os.write(storedStr.getBytes());
        }
        finally {
            os.close();
        }
    }
    
    /**
     * read stored value from file to grids data
     */
    public static boolean readFile(int[][] seatGrid, String[][] idGrid) throws Exception {
        String seatGridString = null;
        String idGridString = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String curLine;
            int i = 0;
            while((curLine = reader.readLine()) != null) {
                if(i == 0)
                    seatGridString = curLine;
                else if(i == 1)
                    idGridString = curLine;
                i++;
            }
            dataFromString(seatGridString, idGridString, seatGrid, idGrid);
            return true;
        }
        catch(FileNotFoundException fnfe) {
            return false;
        }
    }
    
    /**
     * convert grids data to string
     */
    public static String dataToString(int[][] seatGrid, String[][] idGrid) {
        StringBuilder builder = new StringBuilder();
        
        //build from seatGrid
        for(int j = 0; j < rows; j++)
            for(int i = 0; i < cols; i++) {
                if(i == 0 && j == 0)
                    builder.append(seatGrid[i][j]);
                else
                    builder.append(";").append(seatGrid[i][j]);
            }
        
        builder.append("\n");
        
        //build from idGrid
        for(int j = 0; j < rows; j++)
            for(int i = 0; i < cols; i++) {
                if(i == 0 && j == 0)
                    builder.append(idGrid[i][j] == null ? "" : idGrid[i][j]);
                else
                    builder.append(";").append(idGrid[i][j] == null ? "" : idGrid[i][j]);
            }
        
        return builder.toString();
    }
    
    /**
     * parse stored string to grids data
     */
    public static void dataFromString(String seatGridStr, String idGridStr, int[][] seatGrid, String[][] idGrid) throws Exception {
        if(seatGrid == null || idGrid == null)
            throw new Exception();
        
        //split stored values by ";"
        String[] seatGridVals = seatGridStr.split(";");
        String[] idGridVals = idGridStr.split(";");
        if(seatGridVals.length < cols * rows || idGridVals.length < cols * rows)
            throw new Exception();
        
        //grant values to grid arrays
        for(int i = 0; i < cols * rows; i++) {
            try {
                seatGrid[i % cols][i / cols] = Integer.parseInt(seatGridVals[i]);
                if(!idGridVals[i].isEmpty())
                    idGrid[i % cols][i / cols] = idGridVals[i];
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
