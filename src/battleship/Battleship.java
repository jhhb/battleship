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
 */
public class Battleship {

    /**
     * @param args the command line arguments
     */
    public static Random rand = new Random();
    
    public static void main(String[] args) {
        
        
        //Important questions:
            //What range of edge weights do we pick?
            //How do we evolve when we have edge weights that are numbers in a range and not just 0 or 1?
                
        //We want to have each member of the parasite population play against each member of the host population.
        //So, if we have 10 parasites and 10 hosts, we want them each to play against each other the same number of times.
        //we want all this playing to happen before we perform any evolution so that we can accurately calculate the fitness
        //
        
        wrapper();
       
        
        
        // TODO code application logic here
    }
    
    public static void wrapper(){
        
        //initialize hosts and parasites
        int NUMBER_OF_TEAM_MEMBERS = 10;
        int iterationCounter = 0;
        
        ArrayList<Player> hosts = new ArrayList<Player>();
        ArrayList<Player> parasites = new ArrayList<Player>();
        
        for(int i = 0; i < NUMBER_OF_TEAM_MEMBERS; i++){
            hosts.add(new Player());
            parasites.add(new Player());
        }
       
        
        while(iterationCounter < 20){
            ArrayList<Player> temp = new ArrayList<Player>();
            playGamesForPopulations(hosts, parasites);
            ArrayList<ArrayList<Double>> newEdgeWeights = new ArrayList<ArrayList<Double>>();
            newEdgeWeights = evolvePopulations(hosts, parasites);
            //this function should go throw and just replace the hosts Array of Array of Doubles with a new one where
            //all the edge weights are replaced
            hosts = createNewPlayersWithNewEdgeWeights(hosts, newEdgeWeights);
            
            //reuses old edge weights but clears the parasites -- new board, new everything
            parasites = createNewParasitesWithOldEdgeWeights(parasites);
            
            temp = parasites;
            parasites = hosts;
            hosts = temp;
            
            iterationCounter+=1;
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
    
    //this should have each host play against each parasite.
    public static void playGamesForPopulations(ArrayList<Player> hosts, ArrayList<Player> parasites){
        
        int[][] multi = new int[hosts.size()][parasites.size()];
        //2d array of hosts x parasites initialized to 0
        
        for(int i = 0; i < hosts.size(); i++){
            for(int j = 0; j < parasites.size(); j++){
                    playGame(hosts.get(i), parasites.get(j), j);
                    
                    //HERE I AM USING "Dummy indexes" that allow me to actually run the code. 
                    hosts.get(i).resetIndexes();
                    parasites.get(j).resetIndexes();

            }
        }
        //at the end of this, all hosts have played against all parasites.
    }
    
    public static void playGame(Player host, Player parasite, int parasiteIndex){
        //we can determine who goes first through p1 and p2, outside of this function
        boolean noWinner = true;
        
        //host attacks first
        if(rand.nextDouble() <= 0.5){
            while(noWinner){
                
                int hostAttackIndex = host.getAttackIndex();
                //could be off by one here?
                //return -1 when all enemy ships are sunk;
                if(hostAttackIndex == -1){
                    noWinner = false;
                    parasite.incrementLossCounterAsParasite();
                    host.updateVictories(parasiteIndex);
                    break;
                }
                else{
                host.attack(parasite, hostAttackIndex);
                }
              
                
                int parasiteAttackIndex = parasite.getAttackIndex();
                
                if(parasiteAttackIndex == -1){
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
                    noWinner = false;
                    break;
                }
                else{
                                    parasite.attack(host, parasiteAttackIndex);
                }
                
                int hostAttackIndex = host.getAttackIndex();
                
                if(hostAttackIndex == -1){
                    noWinner = false;
                    parasite.incrementLossCounterAsParasite();
                    host.updateVictories(parasiteIndex);
                }
                else{
                    host.attack(parasite, hostAttackIndex);
                }
            }
        }   
    }
    
    //I am assuming that we only evolve the hosts, which is what I think we should be doing based on paper
    public static ArrayList<ArrayList<Double>> evolvePopulations(ArrayList<Player> hosts, ArrayList<Player> parasites){
        
        //double? 
        ArrayList<Double> fitnessArray = calculateFitnessForHosts(hosts, parasites);
        ArrayList<Player> breedingPoolOfHosts = boltzmannSelection(hosts, fitnessArray);
        double crossoverProbability = 0.00;
        double mutationProbability = 0.00;
        
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
        while(i < breedingPool.size() - 1){

            double randomProbability = (double)rand.nextInt()/(double)Integer.MAX_VALUE;
            if(randomProbability <= crossoverProbability){
                    int crossPoint = rand.nextInt(breedingPool.size() -1 );
                    
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
                    } else {
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
        double scale = 100.0;
        ArrayList<Double> efitness = new ArrayList();
        for(int i = 0; i< hostsToSelect.size(); i++){
        

        
            double fitness = fitnessArray.get(i); // * 10;
           // fitness *= scale;
   
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
            
            System.out.println(numerator / denom);
            
            if(randomProbability <= numerator/denom){
              breedingPopulation.add(hostsToSelect.get(randomIndex));
              counter+=1;
            }
        }

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