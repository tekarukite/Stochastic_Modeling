package assignments;
import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.charts.*;
import java.util.Arrays;
import java.awt.Color;

import umontreal.ssj.charts.EmpiricalChart;
import umontreal.ssj.probdist.EmpiricalDist;
import umontreal.ssj.stat.Tally;


public class Assignment1 {
    double seed = 5001;
    double m = 5003;
    double a = 24;
    double c = 23;
    
    LCG prng;
    EmpiricalDist durationDist;
    
    public Assignment1() {
        // This is your test function. During grading we will execute the function that are called here directly.

    	//Here, we are going to calculate the empirical distribution of the games and store it in the EmpiricalDist durationDist.
    	
    	durationDist = Question2(".\\game_lengths.csv");
    	
    	//Question3: simulate 5000 games:
    	Tally simulations = new Tally();
    	simulations = Question3();
    	
    	System.out.println(simulations.report());

    }

    public EmpiricalDist getDurationDist(String csvFile){
        // Create the empirical distribution using the EmpiricalDist object and return it

    	BufferedReader br = null; //keep information in buffers.
        String line = ""; //Each of the lines of the csv file.
        
        LinkedList queue = new LinkedList();  //we store in the queue the game lengths
        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) { //while there are lines to read:

            	int aux = Integer.parseInt(line); //Transform the string line into a number.
            	queue.add(aux);
            	
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //We have stored the data in the queue. Now we will transform the queue into a sorted double[]

        Collections.sort(queue); //We sort the data
        int n = queue.size();
        double[] data = new double[n]; 

        for(int i = 0; i < n; i++) {
        	data[i] = (int) queue.peek(); 
        	queue.removeFirst();
        }
              
        durationDist = new EmpiricalDist(data);         
        
        //This is not necessary, but I like to have it:
        /*
        double[] cdf = new double[durationDist.getN()];
        for(int i = 0; i < durationDist.getN(); i++) {
        	cdf[i] = durationDist.cdf(durationDist.getObs(i));
        }
        */
        
        //Here we plot the Empirical CDF of the lengths of the games.
    	plotDurationCDF();
    	
        return durationDist;
    }

    public void plotDurationCDF(){
    	double[] data = new double[durationDist.getN()];
    	for(int i = 0; i < durationDist.getN(); i++) data[i] = durationDist.getObs(i);
    	
    	EmpiricalChart chart = new EmpiricalChart("Empirical Cumulative Distribution Function", "lengths of the games", "probability", data);
    	
    	chart.toLatexFile("EmpiricalChartTest.tex", 12, 8); 
    	//This allows us to put it in latex and make the picture (however, in my case, it exceeded memory, //
    	//so instead of this, I've used as a picture of the plot, the next command line)
    	
    	chart.view(10000, 10000);
    }
    
    public Tally simulateMatches() {
        // Simulate the matches in this function and return the Tally with the durations
        Tally durations = new Tally();
        
        for(int i = 0; i < 5000; i++) { 
        	double R[] = Question1(seed + 19 * i, 18, true); //The last one will decide who will win
        	int counter = 0; //Here, we add the length of the match.
        	int win_game = 0;
        	int loose_game = 0;
        	
        	int j = 0;
        	while(j < 18 && win_game < 5 && loose_game < 5) {
        		//In each couple of values of R, the first one we will consider it as the time and the second one as the one deciding if he/she wins.
        		counter = counter + (int) this.durationDist.inverseF(R[j]);
        		j++;
        		if (R[j] < 0.5) loose_game++;
        		else win_game++;
        		j++;
        	}
        	durations.add(counter);
        }
        

        return durations;
    }    

    /* DO NOT CHANGE THESE FUNCTIONS BELOW, THESE FUNCTIONS ARE USED DURING GRADING */
    public double[] Question1(double givenSeed, int numOutputs, boolean normalize) {
        prng = new LCG(givenSeed,a,c,m);
        double[] result = new double[numOutputs];
        for (int i=0;i<numOutputs;i++) {
            result[i] = prng.generateNext(normalize);
        }
        return result;
    }
    
    public EmpiricalDist Question2(String csvFile) {
        durationDist = getDurationDist(csvFile);
        return durationDist;
    }
    
    public Tally Question3() {
        Tally durations = simulateMatches();
        return durations;
    }

    /*  ONLY CHANGE generateNext in the LCG class */
    public class LCG {
        public double seed;
        public final double m;
        public final double a;
        public final double c;
        
        public double lastOutput;
        
        public LCG(double seed, double a,double c,double m){
            this.seed = seed;
            this.m = m;
            this.a = a;
            this.c = c;
            
            this.lastOutput = seed;
        }
        
        public double generateNext(boolean normalize){
            // implement the pseudo-code algorithm here. You're code should be able to return both normalized and regular numbers based on the value of normalize.
        	
        	int z = (int)(this.a * this.lastOutput + this.c);
        	z = z % (int) this.m;
        	this.lastOutput = z;
        	
        	if (normalize) {
        		double aux = (z + 1)/(this.m + 1);
        		return aux;
        	}
        	
        	return lastOutput;

        }
        
        public void setSeed(double newSeed) {
            this.seed = newSeed;
        }
    }
    
    public static void main(String[] args){
        new Assignment1();
    }

}
