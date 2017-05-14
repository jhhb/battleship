/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author jamesboyle
 */
public class Board {
    
    private final int NUMBER_OF_ROWS = 10;
    private final int NUMBER_OF_COLUMNS = 10;
    private final int shipLengths [] = {5,4,3,3,2};
    private final Random rand = new Random();  
    private List<List<Integer>> gameBoard = new ArrayList<List<Integer>>();
    
    public Board(){
        this.initializeEmptyBoard();
        this.setShips(this.shipLengths);
    }
    
    
    private void initializeEmptyBoard(){
        
        if(this.gameBoard.isEmpty()){
            for(int i = 0; i < NUMBER_OF_ROWS; i++){
            
            List list = new ArrayList<Integer>();
            
            for(int j = 0; j < NUMBER_OF_COLUMNS; j++){
                list.add(0);
               // this.gameBoard.get(i).set(j, 0);
            }
            this.gameBoard.add(list);
            }
          
        }
        else{
            for(int i = 0; i < NUMBER_OF_ROWS; i++){
                for(int j = 0; j < NUMBER_OF_COLUMNS; j++){
                    this.gameBoard.get(i).set(j, 0);
                }
            }
        }
        
        
    } 
    
    private int setShips(int [] ships){
        ArrayList<List<Integer>> startPositions;
        
        for(int i = 0; i < ships.length; i++){
            boolean horizontalOrientation = false;
        
            if(rand.nextDouble() <= 0.5){
                horizontalOrientation = true;
            }
        
            if(horizontalOrientation){
                startPositions =  collectStartPositionsHorizontal(ships[i]);  //collectStartPositionsHorizontal(ships[i]);
            }
            else{
                startPositions = collectStartPositionsVertical(ships[i]); // collectStartPositionsHorizontal(ships[i]); // collectStartPositionsVertical(ships[i]); //collectStartPositionsVertical(ships[i]);
            }
            
            int index = rand.nextInt(startPositions.size());
            
                
            
            
            if(horizontalOrientation){
                int rowStart = startPositions.get(index).get(0);
                int colStart = startPositions.get(index).get(1);
                
                for(int z = 0; z < ships[i]; z++){
                    this.gameBoard.get(rowStart).set(colStart + z + 1, 1);
                }
                
            }
            else{
                int rowStart = startPositions.get(index).get(0);
                int colStart = startPositions.get(index).get(1);
                
                for(int z = 0; z < ships[i]; z++){
                    this.gameBoard.get(rowStart + z + 1).set(colStart, 1);
                }
            }
            
        }  
        int sum = 0;
        return sum;
    }
        
    private ArrayList<List<Integer>> collectStartPositionsHorizontal(int shipLength){
        
        ArrayList<List<Integer>> startPositions = new ArrayList<List<Integer>>();
        
        int consecutiveZeroes = 0;
        
        for(int i = 0; i < this.NUMBER_OF_ROWS; i++){
            for(int j = 0; j < this.NUMBER_OF_COLUMNS; j++){
                if(this.gameBoard.get(i).get(j) == 0){
                    consecutiveZeroes+=1;
                }
                else{
                    consecutiveZeroes = 0;
                }
                
                if(consecutiveZeroes >= shipLength){
                    //System.out.println("adding " + i + j);
                    startPositions.add(Arrays.asList(i,j - shipLength));
                }
            }
            consecutiveZeroes = 0;
        }
        
        return startPositions;
    }
    
    private ArrayList<List<Integer>> collectStartPositionsVertical(int shipLength){
        ArrayList<List<Integer>> startPositions = new ArrayList<List<Integer>>();
        
        int consecutiveZeroes = 0;
        
        for(int i = 0; i < this.NUMBER_OF_ROWS; i++){
            for(int j = 0; j < this.NUMBER_OF_COLUMNS; j++){
                if(this.gameBoard.get(j).get(i) == 0){
                    consecutiveZeroes+=1;
                }
                else{
                    consecutiveZeroes = 0;
                }
                
                if(consecutiveZeroes >= shipLength){
                    startPositions.add(Arrays.asList(j - shipLength, i));
                }
            }
            consecutiveZeroes = 0;
        }
        
        return startPositions;
        
    }
    
    /* This is bad; dont do twice */
    public ArrayList<Integer> getSerializedBoard(){
        
        ArrayList<Integer> list = new ArrayList<Integer>();
        
        for(int i = 0; i < this.gameBoard.size(); i++){
            for(int j = 0; j < this.gameBoard.get(i).size(); j++){
                list.add(this.gameBoard.get(i).get(j));
                
            }
            
        }
        return list;
    }
}