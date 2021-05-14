/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dungns.logic;

import com.dungns.restservice.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * defines Acme seat reservation logic
 * @author dungns
 */
public class LogicHandler {
    public static final int FlagBanned = -1;
    public static final int FlagTaken = 0;
    
    private static int cols = Integer.parseInt(System.getProperty("cols", "1"));
    private static int rows = Integer.parseInt(System.getProperty("rows", "1"));
    private static int space = Integer.parseInt(System.getProperty("space", "0"));
    
    /**
     * init grid arrays
     */
    public static void createGrid(int[][] seatGrid, String[][] idGrid) {
        //set available flag
        for(int i = 0; i < cols; i++)
            for(int j = 0; j < rows; j++) {
                seatGrid[i][j] = cols * rows;
                idGrid[i][j] = "0-0";
            }
    }
    
    /**
     * get available seats groups for a quantity of seat
     */
    public static List<List<Pair>> getSeats(int count) throws Exception {
        int[][] seatGrid = new int[cols][rows];
        String[][] idGrid = new String[cols][rows];
        //get stored data
        boolean fileExisted = FileHandler.readFile(seatGrid, idGrid);
        if(!fileExisted)
            createGrid(seatGrid, idGrid);

        System.out.println("==== after create grid");
        logSeatGrid(seatGrid, idGrid);
        logIdGrid(seatGrid, idGrid);

        List<List<Pair>> res = new ArrayList<>();

        //find available zones
        Set<String> setZoneId = new HashSet<>();     //list ids of available zones
        for(int i = 0; i < cols; i++)
            for(int j = 0; j < rows; j++) {
                //a set of seats available for more than count
                if(seatGrid[i][j] >= count)
                    setZoneId.add(idGrid[i][j]);
            }

        //export zones to seats
        Map<String, List<Pair>> mapAvailableZones = new HashMap<>();
        for(int i = 0; i < cols; i++)
            for(int j = 0; j < rows; j++) {
                String zoneId = idGrid[i][j];
                if(!setZoneId.contains(zoneId))
                    continue;
                //list all seats had same available zoneId
                if(mapAvailableZones.get(zoneId) == null)
                    mapAvailableZones.put(zoneId, new ArrayList<>());
                mapAvailableZones.get(zoneId).add(new Pair(i, j));
            }

        for(List<Pair> zoneSeats : mapAvailableZones.values()) {
            res.add(zoneSeats);
            System.out.println("==== visualizeGrid result: ");
            visualizeGrid(zoneSeats);
        }

        return res;
    }
    
    /**
     * update seat positions taken
     */
    public static void takeSeats(List<Pair> seats) throws Exception {
        int[][] seatGrid = new int[cols][rows];
        String[][] idGrid = new String[cols][rows];
        //get stored data
        boolean fileExisted = FileHandler.readFile(seatGrid, idGrid);
        if(!fileExisted)
            createGrid(seatGrid, idGrid);

        Set<String> setModifiedZoneId = new HashSet<>();
        boolean res = markTakenSeats(seatGrid, idGrid, seats, setModifiedZoneId);
        if(!res) {
            System.out.println("==== markTakenSeats failed");
            return;
        }
        System.out.println("==== after markTakenSeats");
        logSeatGrid(seatGrid, idGrid);
        logIdGrid(seatGrid, idGrid);
        recalcEmptySeats(seatGrid, idGrid, setModifiedZoneId);
        System.out.println("==== after recalcEmptySeats");
        logSeatGrid(seatGrid, idGrid);
        logIdGrid(seatGrid, idGrid);

        //stored new data
        FileHandler.writeFile(seatGrid, idGrid);
    }
    
    /**
     * mark Taken seats. mark banned seats.
     */
    public static boolean markTakenSeats(int[][] seatGrid, String[][] idGrid, List<Pair> coords, Set<String> setModifiedZoneId) {
        if(coords == null || setModifiedZoneId == null)
            return true;
        
        //check if one coords banned or taken, return fail
        for(Pair coord : coords) {
            if(seatGrid[coord.x][coord.y] == FlagBanned
                    || seatGrid[coord.x][coord.y] == FlagTaken)
                return false;
        }
        
        for(Pair coord : coords) {
            if(seatGrid[coord.x][coord.y] == FlagTaken)
                continue;
            //mark taken seat
            seatGrid[coord.x][coord.y] = FlagTaken;
            idGrid[coord.x][coord.y] = null;
            
            //mark banned seats
            for(int i = Math.max(coord.x - space + 1, 0); i <= Math.min(coord.x + space - 1, cols - 1); i++)
                for(int j = Math.max(coord.y - space + 1, 0); j <= Math.min(coord.y + space - 1, rows - 1); j++) {
                    //not ban banned or taken seat
                    if(seatGrid[i][j] == FlagBanned
                            || seatGrid[i][j] == FlagTaken)
                        continue;
                    //not ban enough space seat
                    if(Math.abs(coord.x - i) + Math.abs(coord.y - j) >= space)
                        continue;
                    
                    setModifiedZoneId.add(idGrid[i][j]);
                    seatGrid[i][j] = FlagBanned;
                    idGrid[i][j] = null;
                }
        }
        return true;
    }
    
    /**
     * calculate and mark available seats on modified zone
     */
    public static void recalcEmptySeats(int[][] seatGrid, String[][] idGrid, Set<String> setModifiedZoneId) {
        boolean[][] passedFlags = new boolean[cols][rows];
        List<Pair> stack = new ArrayList<>();
        List<Pair> adjacentSeats = new ArrayList<>();
        
        for(String zoneId : setModifiedZoneId) {
            //recalc on each modified zone
            for(int i = 0; i < cols; i++)
                for(int j = 0; j < rows; j++) {
                    if(!zoneId.equals(idGrid[i][j]))
                        continue;
                    
                    //traverse current zone to find new adjacent seat positions
                    stack.clear();
                    adjacentSeats.clear();
                    traverseAvailableAdjacentSeats(seatGrid, idGrid, zoneId, new Pair(i, j), passedFlags, stack, adjacentSeats);
                    if(adjacentSeats.isEmpty())
                        continue;
                    for(Pair coord : adjacentSeats) {
                        //update flag of seat
                        seatGrid[coord.x][coord.y] = adjacentSeats.size();
                        //update zoneId of seat
                        idGrid[coord.x][coord.y] = i + "-" + j;
                    }
                }
        }
    }
    
    /**
     * traverse all available adjacent seats from first coordinate
     */
    public static void traverseAvailableAdjacentSeats(int[][] seatGrid, String[][] idGrid, String zoneId, Pair startCoord, boolean[][] passedFlags, 
            List<Pair> stack, List<Pair> adjcentSeats) {
        if(passedFlags == null || stack == null || adjcentSeats == null)
            return;
        
        if(idGrid[startCoord.x][startCoord.y].equals(zoneId)
                && !passedFlags[startCoord.x][startCoord.y]) {
            Pair seatPair = new Pair(startCoord.x, startCoord.y);
            stack.add(seatPair);
            adjcentSeats.add(seatPair);
            passedFlags[startCoord.x][startCoord.y] = true;
        }
        
        while(!stack.isEmpty()) {
            //pop stack
            Pair currentCoord = stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
            
            //traverse next adjacent seat had same zoneId
            //top
            if(currentCoord.y + 1 < rows 
                    && zoneId.equals(idGrid[currentCoord.x][currentCoord.y + 1])
                    && !passedFlags[currentCoord.x][currentCoord.y + 1]) {
                Pair seatPair = new Pair(currentCoord.x, currentCoord.y + 1);
                stack.add(seatPair);
                adjcentSeats.add(seatPair);
                passedFlags[currentCoord.x][currentCoord.y + 1] = true;
            }
            //right
            if(currentCoord.x + 1 < cols 
                    && zoneId.equals(idGrid[currentCoord.x + 1][currentCoord.y])
                    && !passedFlags[currentCoord.x + 1][currentCoord.y]) {
                Pair seatPair = new Pair(currentCoord.x + 1, currentCoord.y);
                stack.add(seatPair);
                adjcentSeats.add(seatPair);
                passedFlags[currentCoord.x + 1][currentCoord.y] = true;
            }
            //bottom
            if(currentCoord.y - 1 >= 0
                    && zoneId.equals(idGrid[currentCoord.x][currentCoord.y - 1])
                    && !passedFlags[currentCoord.x][currentCoord.y - 1]) {
                Pair seatPair = new Pair(currentCoord.x, currentCoord.y - 1);
                stack.add(seatPair);
                adjcentSeats.add(seatPair);
                passedFlags[currentCoord.x][currentCoord.y - 1] = true;
            }
            //left
            if(currentCoord.x - 1 >= 0
                    && zoneId.equals(idGrid[currentCoord.x - 1][currentCoord.y])
                    && !passedFlags[currentCoord.x - 1][currentCoord.y]) {
                Pair seatPair = new Pair(currentCoord.x - 1, currentCoord.y);
                stack.add(seatPair);
                adjcentSeats.add(seatPair);
                passedFlags[currentCoord.x - 1][currentCoord.y] = true;
            }
        }
    }
    
    public static void logSeatGrid(int[][] seatGrid, String[][] idGrid) {
        System.out.println("==== seatGrid:");
        for(int i = 0; i < rows; i++) {
            StringBuilder builder = new StringBuilder();
            for(int j = 0; j < cols; j++) {
                builder.append(seatGrid[j][i]).append("    ");
            }
            System.out.println(builder.toString());
        }
    }
    
    public static void logIdGrid(int[][] seatGrid, String[][] idGrid) {
        System.out.println("==== idGrid:");
        for(int i = 0; i < rows; i++) {
            StringBuilder builder = new StringBuilder();
            for(int j = 0; j < cols; j++) {
                builder.append(idGrid[j][i]).append("    ");
            }
            System.out.println(builder.toString());
        }
    }
    
    public static void visualizeGrid(List<Pair> coords) {
        boolean[][] flags = new boolean[cols][rows];
        for(Pair coord : coords)
            flags[coord.x][coord.y] = true;
        
        for(int i = 0; i < rows; i++) {
            StringBuilder builder = new StringBuilder();
            for(int j = 0; j < cols; j++) {
                if(flags[j][i])
                    builder.append("o").append("    ");
                else
                    builder.append("-").append("    ");
            }
            System.out.println(builder.toString());
        }
    }
}
