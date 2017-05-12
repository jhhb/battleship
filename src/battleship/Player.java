/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jamesboyle
 */
public class Player {
    
    private Board board = new Board();
    private List<Integer> playerBoard = new ArrayList<Integer>();
    private ArrayList<Integer> opponentBoard = new ArrayList<Integer>();
    
    private final int NUMBER_OF_PARASITES = 25;
    //array that stores the index of defeated parasites based 
    //on their index in the parasite array

    //this is used to calculate fitness. To determine an individual's fitness you need to know all the parasites it defeated and all 
    //the other hosts that defeated a parasite. 
    private ArrayList<Integer> victories; // = new ArrayList<Integer>();
    
    private int numberOfEnemyShipSquaresRemaining = 17;
    
    //Number of losses if parasite. I think it is easy to keep this here so that we can just check the loss counter when calculating the sum.
    private int lossCounterAsParasite = 0;
    
    //keeps track of edge weights
    private ArrayList<Double> edgeWeights = new ArrayList();
    
    /*  
    *********************************************************************************************************    
    TESTING
    *********************************************************************************************************    
    */
    private ArrayList<Integer> indexes = new ArrayList();

        
    public Player(){
        setupOpponentBoard();
        this.playerBoard = getPlayerBoard();
        this.victories = initializeVictories();
        
        /*  
        *********************************************************************************************************    
        TESTING
        *********************************************************************************************************    
        */
        setEdgeWeightsDummy();
        resetIndexes();

    }
    
    public Player(ArrayList<Double> newEdgeWeights){
        setupOpponentBoard();
        this.playerBoard = getPlayerBoard();
        this.victories = initializeVictories();
        
        //important difference
        
        this.edgeWeights = newEdgeWeights;
        
        /*  
        *********************************************************************************************************    
        TESTING
        *********************************************************************************************************    
        */
        resetIndexes();
       
    }
    
    public void updateVictories(int parasiteIndex){
        this.victories.add(parasiteIndex);
    }
    
    private ArrayList<Integer> initializeVictories(){
        ArrayList<Integer> temp = new ArrayList<Integer>();
        return temp;
    }
    
    //this method creates a new board of 100 0s or replaces the values already there if the board is not empty. 
    //uses 1-D array, NOT 2D
    
    private List<Integer> getPlayerBoard(){
        return this.board.getSerializedBoard();
    }
    
    private void setupOpponentBoard(){
        if(this.opponentBoard.isEmpty()){
            for(int i = 0; i < 100; i++){
                this.opponentBoard.add(0);
            }
        }
        else{
            for(int i = 0; i < 100; i++){
                this.opponentBoard.set(i, 0);
            }
        }
    }
    
    public void incrementLossCounterAsParasite(){
        this.lossCounterAsParasite +=1;
    }
    
    public int getAttackIndex(){
        if(this.numberOfEnemyShipSquaresRemaining == 0){
            return -1;
        }
        
        int indexToAttack = getIndexToAttackFromNeuralNet(this.opponentBoard);
        
        return indexToAttack;
        
    }
    
    //Attack gets called from the attacker and "attacks" a target with a given index.
    //The function assumes that the index it gets from the neural net is a valid one that has not already been hit.
    public void attack(Player target, int indexToAttackEnemy){
        
        //can be 0 or 1 since a player only has 0 or 1 for their own board
        int enemyStateAtPosition = target.getValueAtBoardIndex(indexToAttackEnemy);
        
        //Got a hit on a ship!!!
        if(enemyStateAtPosition == 1){
            this.numberOfEnemyShipSquaresRemaining-=1;
            this.opponentBoard.set(indexToAttackEnemy, 1);
            //DO NOTHING TO target's board
        }
        //No hit, so we set a -1 and do nothing else.
        else{
            this.opponentBoard.set(indexToAttackEnemy, -1);
        }
                  
        //attack a target and update own board, their board, update numberOfShipsRemaining
        //add in evolve shit
    }
    
    //the instance that this method is called on returns the value of the board for that instance at the given index.
    //so, this method gets called on the parasite and returns the value for the parasite there.
    public int getValueAtBoardIndex(int index){
        return this.playerBoard.get(index);
    }
    
    /* THIS IS A DUMMY FUNCTION RIGHT NOW */
    /* You guys need to fill in this part */
    /* I am using these indexes from 0 to 100 to test */
    private int getIndexToAttackFromNeuralNet(ArrayList<Integer> enemyBoard){
        
        //HOW DO WE AVOID LOOKING AT SPACES WE HAVE ALREADY HIT?
        if(!this.indexes.isEmpty()){
            int result = this.indexes.get(0);
            this.indexes.remove(0);
            return result; 
        // -100000;
            
        }
        else{
            System.out.println("indexes empty");
            return -1;
        }   
    }

    public ArrayList<Integer> getVictories(){
        return this.victories;
    }
    
    public double getLossCounterAsParasite(){
        return (double) this.lossCounterAsParasite;
    }
    
    public ArrayList<Double> getEdgeWeights(){
        return this.edgeWeights;
    }
    
/*
*********************************************************************************************************    
TESTING    
*********************************************************************************************************    

*/    
    
    public void setEdgeWeightsDummy(){
        
        for(int i = 0; i < 1000; i++){
            this.edgeWeights.add((5.0));
        }
    }
    
    public void resetIndexes(){
        ArrayList<Integer> inds = new ArrayList<Integer>();
        for(int i = 0; i < 100; i++){
            inds.add(i);
        }
        this.indexes = inds;
    }
}