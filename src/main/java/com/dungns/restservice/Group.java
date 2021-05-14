/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dungns.restservice;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dungns
 */
public class Group {
    private List<Pair> coordinates = new ArrayList<>();
    
    public Group() {
        
    }
    
    public Group(List<Pair> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Pair> getCoordinates() {
        return coordinates;
    }
}
