/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    
    private List<List<Double>> edgeMap = new ArrayList<>();
    
    private Random rand = new Random();
    /*  
    *********************************************************************************************************    
    TESTING
    *********************************************************************************************************    
    */
    private ArrayList<Integer> indexes = new ArrayList();   //JW: possible to delete. Use possibleAttackOptions instead
    
    private ArrayList<Double> outputList = new ArrayList();
    
    private ArrayList<Integer> possibleAttackOptions = new ArrayList();

        
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
        this.makeEdgeMap();
        this.initOutputList();
        this.initPossibleOptionsToAttack();
        updateOutputListAfterAttacking(100); // update output list for the bias node
        
    }
    
    public Player(ArrayList<Double> newEdgeWeights){
        setupOpponentBoard();
        this.playerBoard = getPlayerBoard();
        this.victories = initializeVictories();
        
        //important difference
        
        this.edgeWeights = newEdgeWeights;
        this.makeEdgeMap();
        this.initOutputList();
        this.initPossibleOptionsToAttack();
        updateOutputListAfterAttacking(100); // update output list for the bias node
        
        
        /*  
        *********************************************************************************************************    
        TESTING
        *********************************************************************************************************    
        */       
    }
    
    public void resetPossibleOptionsToAttack(){
        ArrayList<Integer> temp = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            temp.add(i);
        }
        this.possibleAttackOptions = temp;
    }
    
    private void initPossibleOptionsToAttack(){
        ArrayList<Integer> temp = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            temp.add(i);
        }
        this.possibleAttackOptions = temp;
    }
    
    private void updateOutputListAfterAttacking(int i){
        //i = previously attacked square
        double afterAttackValue = 0.0;
        if(i == 100){   //bias node
            afterAttackValue = 1;
        }else{
            afterAttackValue = this.opponentBoard.get(i);
        }
        
        //JB: Is this right
        for(int j = 0; j < 100; j++){
            double newValue = afterAttackValue * this.edgeMap.get(i).get(j);
            this.outputList.set(j, newValue);
        }
    }
    
    private void initOutputList(){
        ArrayList<Double> temp = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            temp.add(0.0);
        }
        this.outputList = temp;
    }
    
    private void makeEdgeMap(){
        for(int i = 0; i < 101; i++){ //PLACEHOLDER INT (includes bias node)
            ArrayList<Double> temp = new ArrayList<>();
            for(int j = 0; j < 100; j++){
                temp.add(this.edgeWeights.get(i*100 + j));
            }
            this.edgeMap.add(temp);
        }
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
    
    /* 
    We get our next index to attack from our understanding of the opponent board.
    */
    public int getAttackIndex(){
        //this is a handy way to tell us when we have won--there are 0 spaces of the enemy left so we know we win.
        if(this.numberOfEnemyShipSquaresRemaining == 0){
            return -1;
        }
        
        int indexToAttack = getIndexToAttackFromNeuralNet(this.opponentBoard);
        
        return indexToAttack;
        
    }
    
    //Attack gets called from the attacker and "attacks" a target with a given index.
    //The function assumes that the index it gets from the neural net is a valid one that has not already been hit.
    
    //In this function, I am not actually editing or changing the enemy board, JUST the host's understanding of the board.
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
        
        this.updateOutputListAfterAttacking(indexToAttackEnemy);
    }
    
    //the instance that this method is called on returns the value of the board for that instance at the given index.
    //so, this method gets called on the parasite and returns the value for the parasite there.
    public int getValueAtBoardIndex(int index){
        return this.playerBoard.get(index);
    }
    
    /* THIS IS A DUMMY FUNCTION RIGHT NOW */
    /* You guys need to fill in this part */
    /* I am using these indexes from 0 to 99 to test */
    /*We need to avoid looking at spaces we have already hit*/
    private int getIndexToAttackFromNeuralNet(ArrayList<Integer> enemyBoard){
        
        //JW: Get best option from possible remaining options to attack
        //boyle: if not empty
        if(!this.possibleAttackOptions.isEmpty()){
            int indexOfBestAttack = 0;
            int bestAttack = this.possibleAttackOptions.get(indexOfBestAttack);
            double bestAttackValue = this.outputList.get(bestAttack);
            for(int i = 1; i < this.possibleAttackOptions.size(); i++){
                if(this.outputList.get(this.possibleAttackOptions.get(i)) > bestAttackValue){
                    bestAttack = this.possibleAttackOptions.get(i);
                    bestAttackValue = this.outputList.get(bestAttack);
                    indexOfBestAttack = i;
                }
            }

            this.possibleAttackOptions.remove(indexOfBestAttack);

            return bestAttack;
            
        }
        //boyle: if it is empty, 
        else{
            return -1;
        }
        
        
        //all the code in this function will be replaced with the real NN stuff. 

//        if(!this.indexes.isEmpty()){
//            int result = this.indexes.get(0);
//            this.indexes.remove(0);
//            return result;             
//        }
//        else{
//            System.out.println("indexes empty");
//            return -1;
//        }   

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
        
        for(int i = 0; i < 10100; i++){  //total number of edges (101*100) ==> ((num inputs + bias node) * num outputs)
            
            double randRange = -1 + (1 - -1) * rand.nextDouble();
            
            
            this.edgeWeights.add(randRange);
        }
    }
}