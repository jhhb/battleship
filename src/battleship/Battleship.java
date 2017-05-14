/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author jamesboyle
 * 
 * 
 *********************************************************************************************************
 * THINGS TO LOOK AT 
 * 
 * 1. Add the Neural net functionality in the Player class
 *      A. This will involve making changes to the attack function, the `AttackFromNeuralNet function, and the various 
 *         objects and functions used by those methods.
 *      B. It will be important to think of how to prevent already hit board positions from being selected by the neural net again. I think
 *         that this could be done by just using a temporary set of edge weights for making the decisions, while preserving the real edge weights in
 *         the edgeWeights variable. You would set the edgeWeight to zero for the decision-making edgeWeights for a given position (0 through 99) after
 *         you hit at a position
 *      C. In order to test the GA without any of the neural net index selection capabilities, I am using "indexes" to select indexes.
 *         All I am doing is pushing numbers 0 through 99 into that array, and then goign in order, 0 through 99, attackign each index.
 *      D. I am using setEdgeWeightsDummy to set initial edgeWeights so that I can test the GA code. I think you should replace my setEdgeWeightsDummy function
 *         calls with a real setEdgeWeights function. It should only be used at the beginning of the program when we dont have initial weights.
 *         Later on, after the first iteration, when we do have initial weights, we use the second constructor for the Players that takes the evolved
 *         edge weights as a parameter. 
 * 2. Check my Boltzmann calculation. I'm not using any kind of scale but I think that might be fine for what we are doing.
 * 3. Please also check my fitness calculation. It seemed pretty straight-forward to me but I'm a retard with math and math symbols
 * 4. In crossoverWrapper, do we want all those -1s? those were from oru original GA code.
 * 
 * TODOs:
 *  -verify that players are actually "winning" games by checking their boards to make sure that this.opponentBoard shows 1s covering the enemy's ships
 * 
 * 
 *         //Important questions:
            //What range of initial edge weights do we pick?
            //How do we evolve when we have edge weights that are numbers in a range and not just 0 or 1?
                
 * 
 *********************************************************************************************************

 */
public class Battleship {

    /**
     * @param args the command line arguments
     */
    public static Random rand = new Random();
    public static double crossoverProbability = 0.00;
    public static double mutationProbability = 0.00;
    
    public static int iter = 0;
    
    public static void main(String[] args) {
        
        //We want to have each member of the parasite population play against each member of the host population.
        //So, if we have 10 parasites and 10 hosts, we want them each to play against each other the same number of times.
        //we want all this playing to happen before we perform any evolution so that we can accurately calculate the fitness
        //
        
        wrapper();
       
    }
    
    public static void wrapper(){
        
        //initialize hosts and parasites
        int NUMBER_OF_TEAM_MEMBERS = 6;
        int iterationCounter = 0;
        
        ArrayList<Player> hosts = new ArrayList<Player>();
        ArrayList<Player> parasites = new ArrayList<Player>();
        
        /* Initializes hosts and parasites */
        for(int i = 0; i < NUMBER_OF_TEAM_MEMBERS; i++){
            hosts.add(new Player());
            parasites.add(new Player());
        }
       
        /* We give the hosts 10 chances to be hosts and 10 chances to be parasites, and we do the same for hte parasites */
        while(iterationCounter < 100){
            
            ArrayList<Player> temp = new ArrayList<Player>();
            
            /* This function "plays" all the battleship games for the host and parasite populations. each host plays against each parasite
                For 10 hosts and 10 parasites, you get 100 games.
            
                This function only adds information to the Player objects in hosts and parasites and doesn't need to return anything. We then access this info
                when we calculate the evolution, new edge weights.
            */
            
            
            playGamesForPopulations(hosts, parasites);
            System.out.println("finished round " + iterationCounter + " of games");
            ArrayList<ArrayList<Double>> newEdgeWeights = new ArrayList<ArrayList<Double>>();
            
            /* We evolve the host population to produce new edge weights. This includes mutation */
            newEdgeWeights = evolvePopulations(hosts, parasites);

            /* We create NEW hosts using the new edge weights */
            hosts = createNewPlayersWithNewEdgeWeights(hosts, newEdgeWeights);
            
            /*We create NEW parasites but with the same edge weights--we need to keep the old parasite edge weights because
            They still need a chance to be hosts*/
            parasites = createNewParasitesWithOldEdgeWeights(parasites);
            
            /*We flip the parasites and hosts*/
            temp = parasites;
            parasites = hosts;
            hosts = temp;
            
            iterationCounter+=1;
            iter+=1;
        }
        
        //See how we did after 20 iterations 
        
        
    }
    
    public static ArrayList<Player> createNewPlayersWithNewEdgeWeights(ArrayList<Player> hosts, ArrayList<ArrayList<Double>> newEdgeWeights){
        
        for(int i = 0; i < hosts.size(); i++){
            hosts.set(i, new Player(newEdgeWeights.get(i)));
        }
        return hosts;
    }
    
    public static ArrayList<Player> createNewParasitesWithOldEdgeWeights(ArrayList<Player> parasites){
        for(int i = 0; i < parasites.size(); i++){
            parasites.set(i, new Player(parasites.get(i).getEdgeWeights()));
        }
        return parasites;
    }
    
    /*Each host and parasite play a game with each other.
      After the game is over, we "reset indexes" which just makes it so the same host and same parasite can play games against other parasites and hosts
      In the same iteration. This is just done for testing purposes.
    */
    public static void playGamesForPopulations(ArrayList<Player> hosts, ArrayList<Player> parasites){
                
        for(int i = 0; i < hosts.size(); i++){
            for(int j = 0; j < parasites.size(); j++){
                    playGame(hosts.get(i), parasites.get(j), j);
                    System.out.println("finished game " + i + "," + j);
                    //HERE I AM USING "Dummy indexes" that allow me to actually run the code. I have to reset the indexes after each iteration
                    //BEcause I pick the next index to attack by popping off an index in an array, and eventually if you dont reset the indexes
                    //You will get an out of bounds error after you hit all 100 of them.
                   

            }
        }
        //at the end of this, all hosts have played against all parasites.
    }
    
    /* This function first determines who goes first, then it has each player take turns getting an index and attacking
    their enemy
    */
    public static void playGame(Player host, Player parasite, int parasiteIndex){

        boolean noWinner = true;
        
        String winner = "none";        
        //host attacks first
        if(rand.nextDouble() <= 0.5){
            while(noWinner){
                
                int hostAttackIndex = host.getAttackIndex();
                System.out.println(hostAttackIndex);
                //return -1 when all enemy ships are sunk;
                if(hostAttackIndex == -1){
                    noWinner = false;
                    
                    //if the parasite loses, we increment its loss counter. this is needed for fitness calculation
                    parasite.incrementLossCounterAsParasite();
                    winner  = "host";
                    //since the host wins, we add the parasite's index to the victories array inside the host. 
                    //this is done so that we can easily iterate the parasites the host defeated when calculating fitness
                    
                    host.updateVictories(parasiteIndex);
                    
                    break;
                }
                else{
                    //if we didnt win, we have the host attack the parasite
                    //we need the else statement to avoid an out of bounds
                    host.attack(parasite, hostAttackIndex);
                }
              
                //Everything else in this function is a similar but flipped case from the above.
                int parasiteAttackIndex = parasite.getAttackIndex();
                
                if(parasiteAttackIndex == -1){
                    winner = "par";
                    noWinner = false;
                    break;
                }
                else{
                parasite.attack(host, parasiteAttackIndex);
                }
            }
        }
        //parasite attacks first 
        else{
            while(noWinner){
                
                int parasiteAttackIndex = parasite.getAttackIndex();
                
                if(parasiteAttackIndex == -1){
                    winner = "par";
                    noWinner = false;
                    break;
                }
                else{
                    parasite.attack(host, parasiteAttackIndex);
                }
                
                int hostAttackIndex = host.getAttackIndex();
                
                if(hostAttackIndex == -1){
                    winner = "host";
                    noWinner = false;
                    parasite.incrementLossCounterAsParasite();
                    host.updateVictories(parasiteIndex);
                }
                else{
                    host.attack(parasite, hostAttackIndex);
                }
            }
        }
        
        if(iter == 0){
            System.out.println("WINNER: " + winner);
            System.out.println("parasite board");
            for(int i = 0; i < 10; i++){
                for(int z = 0; z < 10; z++){
                    System.out.print(parasite.getBoard().getSerializedBoard().get(i * 10 + z) + " ");
                }
                System.out.print("\n");
            }
            System.out.println();
            System.out.println("host opponent board");
            
            for(int i = 0; i < 10; i++){
                for(int z = 0; z < 10; z++){
                    if(host.getOpponentBoard().get(i*10 + z) == -1){
                        System.out.print(2 + " " );
                    }
                    else{
                      System.out.print(host.getOpponentBoard().get(i * 10 + z) + " ");

                    }
                }
                System.out.print("\n");
            }
            
        }
        
        //added these here too - boyle
        host.setupOpponentBoard();
        parasite.setupOpponentBoard();
     
    }
    
    //I am assuming that we only evolve the hosts, which is what I think we should be doing based on paper
    public static ArrayList<ArrayList<Double>> evolvePopulations(ArrayList<Player> hosts, ArrayList<Player> parasites){
        
        /*Calculates the fitness for all the hosts */
        ArrayList<Double> fitnessArray = calculateFitnessForHosts(hosts, parasites);
        
        /* Selects a breeding pool of hosts using boltzmann */
        ArrayList<Player> breedingPoolOfHosts = boltzmannSelection(hosts, fitnessArray);
        
        //NEED TO CHANGE THESE
       
        
        //crossoverWrapper works with the breeding pool of PLAYERS and spits out a List of List of doubles to represent the edge weights for all the players
        ArrayList<ArrayList<Double>> crossedOverHosts = crossoverWrapper(breedingPoolOfHosts, crossoverProbability);
        
        //mutated Host Edge Weights contains all the edge weights for our mutated hosts
        //We use an array of array of edge weights rather than the player so we dont have to worry about state in the player objects

        ArrayList<ArrayList<Double>> mutatedHostEdgeWeights = new ArrayList<ArrayList<Double>>();
        
        for(int i = 0; i < crossedOverHosts.size(); i++){
            mutatedHostEdgeWeights.add(gaMutate(crossedOverHosts.get(i), mutationProbability));
        }
        
        return mutatedHostEdgeWeights;
        
    }
    
        
    //Returns mutated edge weights
    //What we are doing is just incrementing or decrementing by 5% randomly.
    //Maybe we can experiemnt with a smarter way of doing this
    public static ArrayList<Double> gaMutate(ArrayList<Double> edgeWeightsForPlayer, double mutationProbability){
                
        ArrayList<Double> edgeWeights = new ArrayList();

        for(int i = 0; i < edgeWeightsForPlayer.size(); i++){
            
            double randomProbability = (double)rand.nextInt()/(double)Integer.MAX_VALUE;
            
            if(randomProbability <= mutationProbability){
                
                double edgeWeight = edgeWeightsForPlayer.get(i);

                double increaseOrDecreaseProbability = (double)rand.nextInt() / (double)Integer.MAX_VALUE;
                
		if(increaseOrDecreaseProbability <= 0.5){
                    edgeWeights.add(edgeWeight * 1.05);                    
		}
		else{
                    edgeWeights.add(edgeWeight * 0.95);
                }
            }
            else{
                edgeWeights.add(edgeWeightsForPlayer.get(i));
            }
	}      
        return edgeWeights;
    }
          
    public static ArrayList<ArrayList<Double>> crossoverWrapper(ArrayList<Player> breedingPool, double crossoverProbability){
        
        ArrayList<ArrayList<Double>> newCandidates = new ArrayList<ArrayList<Double>>();

        int i = 0; //offspring counter and breeding pool iterator

        ArrayList<Double> child1 = new ArrayList<Double>();
        ArrayList<Double> child2 = new ArrayList<Double>(); 
                
        //Want to pass in the arrayList of edge weights into the crossover function
        // -1? or nah?
        while(i < breedingPool.size() - 1){

            double randomProbability = (double)rand.nextInt()/(double)Integer.MAX_VALUE;
            if(randomProbability <= crossoverProbability){
                    
//size -1??
                    int crossPoint = rand.nextInt(breedingPool.size() );
                    
                    child1 = onePointCrossover(breedingPool.get(i).getEdgeWeights(), breedingPool.get(i + 1).getEdgeWeights(), crossPoint);
                    child2 = onePointCrossover(breedingPool.get(i+1).getEdgeWeights(), breedingPool.get(i).getEdgeWeights(), crossPoint);
            }
            else {
                    child1 = breedingPool.get(i).getEdgeWeights(); //[i];
                    child2 = breedingPool.get(i+1).getEdgeWeights();
            }
            newCandidates.add(child1);
            newCandidates.add(child2);

            i += 2;

            if (newCandidates.size() >= breedingPool.size()) {
                    if (breedingPool.size() % 2 == 0) {
                            break;
                    } 
                    else {
                        newCandidates.add(breedingPool.get(i).getEdgeWeights());
                        i += 1;
                    }
            }
        }
        return newCandidates;
    }
    
    public static ArrayList<Double> onePointCrossover(ArrayList<Double> p1EdgeWeights, ArrayList<Double> p2EdgeWeights, int crossoverIndex){
              
        ArrayList<Double> crossedOverEdgeWeights = new ArrayList<Double>();
  	for (int i = 0; i < p1EdgeWeights.size(); i++) {
            
  		if (i < crossoverIndex) {
  			crossedOverEdgeWeights.add(p1EdgeWeights.get(i));
  		} else {
  			crossedOverEdgeWeights.add(p2EdgeWeights.get(i));
  		}
  	}
  	return crossedOverEdgeWeights;
    }
        
    //QUESTION:   
    //What is the theoretical maximum for something's fitness? E.g. what is the maximum for an iteration of the sharing fitness calculation for
    //a given number of hosts and parasites?
    //You will notice that I am printing out the numerator / denom and that number, ideally, should be scaled between 0 and 1.
    //I want to know the theoretical maximum for that value so that we can properly scale the probabilities of higher-performing players
    public static ArrayList<Player> boltzmannSelection(ArrayList<Player> hostsToSelect, ArrayList<Double> fitnessArray){

        //I removed parts of the calculation in the for() loop that were in the original. This is because I'm not sure how / what to make the fitness
        //a proportion of if at all
        
        double denom = 0;
        double scale = 2.0;
        
        ArrayList<Double> efitness = new ArrayList();
        for(int i = 0; i< hostsToSelect.size(); i++){
          
            double fitness = fitnessArray.get(i); // * 10;
            //fitness *= scale;   
            double eToPower = Math.exp(fitness);
            denom+= eToPower;
            efitness.add(eToPower);
        }
        ArrayList<Player> breedingPopulation = new ArrayList();
        
        int counter = 0;

      //randomly select candidates.size() number of individuals for the vector 
        while(counter < hostsToSelect.size()) {
            int randomIndex = rand.nextInt(hostsToSelect.size());
            double randomProbability =  (double)rand.nextInt()/ (double)Integer.MAX_VALUE;

            double numerator = efitness.get(randomIndex);
            if(randomProbability <= numerator/denom){
              breedingPopulation.add(hostsToSelect.get(randomIndex));
              counter+=1;
            }
        }
        System.out.println();

        return breedingPopulation;      
    }
    
    /*
    This function takes all of the hosts, and for each host, it creates a "running Sum" for the fitness.

    Then, for a given host, we iterate the length of its "victories" array which contains the index of the parasites it defeated.
    
    Iterating the victories array for a given host, we have the index for each parasite as stored in the parasites array.
    With this, we can then get the number of losses that parasite suffered versus hosts, and add it to our running sum.
    
    Finally we add the running sum to the vector to get our fitnesses. 
    
    The fitness array is ordered such that the 0th host's fitness comes first
    */
    public static ArrayList<Double> calculateFitnessForHosts(ArrayList<Player> hosts, ArrayList<Player> parasites){
        
        ArrayList<Double> fitnessArray = new ArrayList<Double>();
        
        for(int i = 0; i < hosts.size(); i++){
            double runningSumForFitness = 0.0;
            for(int j = 0; j < hosts.get(i).getVictories().size(); j++){
                
                //loss counter is cast to a double
                runningSumForFitness += 1.00 / parasites.get(hosts.get(i).getVictories().get(j)).getLossCounterAsParasite();
                
            }
            fitnessArray.add(runningSumForFitness);
            
        }
        return fitnessArray;
    }
}