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
public class GetResponse {
    private List<Group> groups = new ArrayList<>();
    
    public GetResponse() {
        
    }
    
    public GetResponse(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return groups;
    }
    
    /**
     * construct response from pair groups
     */
    public static GetResponse fromListPairs(List<List<Pair>> lstPairs) {
        if(lstPairs == null)
            return null;
        GetResponse res = new GetResponse();
        for(List<Pair> set : lstPairs) {
            Group group = new Group(set);
            res.getGroups().add(group);
        }
        return res;
    }
}
