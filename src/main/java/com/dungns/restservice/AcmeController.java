/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dungns.restservice;

import com.dungns.logic.LogicHandler;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author dungns
 */
@RestController
public class AcmeController {
    @GetMapping("/getAvailable")
    public ResponseEntity<?> getAvailable(@RequestParam(value = "quantity", defaultValue = "0") int quantity) {
        try {
            List<List<Pair>> result = LogicHandler.getSeats(quantity);
            return new ResponseEntity<>(GetResponse.fromListPairs(result), HttpStatus.OK);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @PutMapping("/takeSeat")
    public ResponseEntity<?> takeSeat(@RequestBody List<Pair> seats) {
        try {
            LogicHandler.takeSeats(seats);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
