/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignments;

import java.util.LinkedList;
import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.stat.StatProbe;
import umontreal.ssj.stat.Tally;
import umontreal.ssj.stat.list.ListOfStatProbes;
/**
 *
 * @author mctenthij
 */
public class Assignment2 {
    
    public static void main(String[] args) {
        int C = 5;                  //#Servers
        double lambda = 3;          //Arrival rate
        double mu = 3./2;           //Service rate
        double maxTime = 100000;    //Simulation endtime (minutes)

        int openNew = 3; // number of customers at the registry (including the one in service) before a new registry can be opened
        Assignment2 grocery;
        grocery = new Assignment2(C, lambda, mu, maxTime, openNew);
        ListOfStatProbes[] output = grocery.simulateOneRun();
        System.out.println(output[0].report());
        System.out.println(output[1].report());
    }
//Estos son los valores que hay dentro de Assignment2 (en este caso, los que contendra grocery)
    Server[] serverList;
    
    ArrivalProcess arrivalProcess;
    StopEvent stopEvent;
    ExponentialGen serviceTimeGen;

    int numServers;    
    double arrivalRate;
    double serviceRate;
    double stopTime;
    int openLimit;

    Tally serviceTimeTally;
    Tally waitTimeTally;
    ListOfStatProbes<StatProbe> stats;
    ListOfStatProbes<StatProbe> stats2;
    
    public Assignment2(int numServers, double arrivalRate, double avgServiceTime, double stopTime, int openNew) {
        //ponemos la informacion basica dentro de la clase Assignment 2 llamada grocery.
    	this.arrivalRate = arrivalRate;
        this.serviceRate = avgServiceTime;
        this.numServers = numServers;
        this.stopTime = stopTime;
        this.openLimit = openNew;
        
        //En serverList guardaremos la informacion de los servers. En stats, ...
        serverList = new Server[numServers];
        stats = new ListOfStatProbes<>("Stats for Accumulate");
        stats2 = new ListOfStatProbes<>("Stats for Tallies");
        
        
        //Inicializamos todo
        for (int i = 0; i < numServers; i++) {
            String id = "server " + i;
            //escribe server i: System.out.println(id);
            Accumulate utilization = new Accumulate(id);
            stats.add(utilization);
            Server server = new Server(utilization); //inicializamos el server desde 0.
            serverList[i] = server; //Añadimos el server + info a la lista de servers.
            if (i > 0){ //Todos los servers estan cerrados menos el primero.
                server.closeRegistry();
            }
        }
        
        //Create inter arrival time, and service time generators. (numeros aleatorios para llegadas y servicio)
        serviceTimeGen = new ExponentialGen(new MRG32k3a(), 1 / avgServiceTime);
        arrivalProcess = new ArrivalProcess(new MRG32k3a(), arrivalRate);
        stopEvent = new StopEvent();
        
        //Create Tallies
        waitTimeTally = new Tally("Waittime");
        serviceTimeTally = new Tally("Servicetime");
        //Add Tallies in ListOfStatProbes for later reporting
        stats2.add(waitTimeTally);
        stats2.add(serviceTimeTally);
    }

    public ListOfStatProbes[] simulateOneRun() {
        Sim.init(); //reinicia la simulacion limpiando la lista de eventos y reseteando el reloj a 0.
        waitTimeTally.init();
        serviceTimeTally.init();
        
        arrivalProcess.init();
        stopEvent.schedule(stopTime);
        Sim.start();
        
        ListOfStatProbes[] output = new ListOfStatProbes[2];
        output[0] = stats;
        output[1] = stats2;
        return output;
    }
    ///////////////////////////////////////////////////
    int Question2() { //Write a function that returns the shortest queue
    	int shortest = 999999999;
        int shortestIndex = 0;
        //Para cada uno de los servers (ABIERTOS) de la lista, contar quien tiene menos clientes.
        for(int i = 0; i < 5; i++) { //quitar el 5 y poner el numero de servers de alguna manera.
        	Server aux = serverList[i];
        	if(Sim.time() < 38000) System.out.println(Sim.time() + ")El server " + i + " esta abierto?: " + aux.openServer + " y tiene estos en la cola " + aux.queue.size() + " y tiene el servicio ocupado? " + aux.busyServer);
        	if(aux.openServer && aux.queue.size() < shortest) {
	       		shortest = aux.queue.size();
	       		shortestIndex = i;
	       	}
	        else if(aux.openServer && aux.queue.size() == shortest) { //Si tienen la misma cola y uno de los dos no tiene gente en el server
	        	if(serverList[shortestIndex].busyServer == true && aux.busyServer == false) {
	        		//This can only happen if the queue has 0 people and one has a customer in the server and the other doesn't.
	        		shortestIndex = i;
	        		shortest = aux.queue.size();
	        	}
	        }
        }
        if(Sim.time() < 38000) {System.out.println("");
        
        System.out.println("El numero de clientes es " + shortest + " de la cola " + shortestIndex);
        }
        return shortestIndex;
    }
    
    boolean Question3() {
        // If all registries have 3 customers waiting, open a new register if possible.
        boolean openNew = true;
        
        for(int i = 0; i < 5; i++) { //queue.size < 2 because counting the one in the server is 3
        	//if the server is not open, the if won't enter in looking the queue
        	if(serverList[i].openServer && serverList[i].queue.size() < 2) openNew = false;
        }
        
        return openNew;
    }
    
    void Question4() { //Write a function that closes the other queues when they can.
       int busy_servers = 0;
       int server_busy = -1;
       for(int i = 0; i < 5; i++) {
    	   //Si de los servers que estan abiertos, hay mas de una cola con clientes, no cerramos nada.
    	   //if(serverList[i].openServer && serverList[i].queue.size() > 0) { Esto seria si solo miraramos la cola
    	   if(serverList[i].openServer && serverList[i].busyServer) { //Pero con  esto solo con mirar si el servicio esta ocupado ya vale
    		   busy_servers++;
    		   if(busy_servers == 1) server_busy = i;
    	   }
       }
       
       if(busy_servers == 1) {
    	   for(int i = 0; i < 5; i++) {
    		   //Si no es el server con cola, cerramos el server.
    		   if(server_busy != i) serverList[i].closeRegistry();
    	   }
       }        
    }

    void openServer() { //Me lo he inventado para abrir un server.
    	boolean founded = false;
    	for(int i = 0; i < 5 && founded == false; i++) {
    		if(serverList[i].openServer == false) {
    			serverList[i].openRegistry();
    			founded = true;
    		}
    	}
    }
    
    
    void handleArrival() { //Ha llegado un nuevo cliente. Lo tenemos que poner en la cola correspondiente.
        Customer cust = new Customer();
        
        if (Question3()) openServer();
        
        int short_queue = Question2();
        cust.chooseRegistry(short_queue);
        if(serverList[short_queue].busyServer == false) { //Si el server esta libre
        	//serverList[short_queue].busyServer = true; innecesario debido a lo de dentro de start service
        	serverList[short_queue].startService(cust); //Empezamos el servicio
        }
        else {
        	serverList[short_queue].queue.addLast(cust); //Al server con la lista
        }

    }

    void serviceCompleted(Server server, Customer cust) {
    	//El servicio de un cliente ha acabado. Tenemos que quitarlo y añadir el siguiente cliente de la cola (si hay)
    	//Creo que tambien aqui habriamos de mirar si cerrar todos los servers o no
        
    	cust.completed(); //El cliente ha completado el servicio.
    	waitTimeTally.add(cust.waitTime);
        serviceTimeTally.add(cust.serviceTime);
    	if(server.queue.size() == 0) { //Si no hay nadie más en la fila, miramos todas a ver si se pueden cerrar.
        	Question4(); 
        }
        else { //si aun quedan clientes en la cola, ponemos al primero en servicio.
        	server.startService(server.queue.removeFirst()); //quitamos al cliente de la cola y este empieza el servicio.
        }
    }

    class ArrivalProcess extends Event {
        ExponentialGen arrivalTimeGen;
        double arrivalRate;

        public ArrivalProcess(RandomStream rng, double arrivalRate) {
            this.arrivalRate = arrivalRate;
            arrivalTimeGen = new ExponentialGen(rng, arrivalRate);
        }
        @Override
        public void actions() {
            double nextArrival = arrivalTimeGen.nextDouble();
            schedule(nextArrival);//Schedule this event after
            //nextArrival time units
            handleArrival();
        }
        
        public void init() {
            double nextArrival = arrivalTimeGen.nextDouble();
            schedule(nextArrival);//Schedule this event after
            //nextArrival time units
        }
    }

    class Customer {

        private double arrivalTime;
        private double startTime;
        private double completionTime;
        private double waitTime;
        private double serviceTime;
        private int chosenRegistry;

        public Customer() {
            //Record arrival time when creating a new customer
            arrivalTime = Sim.time();
            startTime = Double.NaN;
            completionTime = Double.NaN;
            waitTime = Double.NaN;
            serviceTime = serviceTimeGen.nextDouble();
        }
        
        // Call this method when the customer chooses a 
        // registry where he/she will be served
        public void chooseRegistry(int choice) {
            chosenRegistry = choice;
        }

        //Call this method when the service for this
        //customer started
        public void serviceStarted() {
            startTime = Sim.time();
            waitTime = startTime - arrivalTime;
        }

        //Call this method when the service for this
        //customer completed
        public void completed() {
            completionTime = Sim.time();
            serviceTime = completionTime - startTime;
        }
    }

    //This Event object represents a server
    class Server extends Event {
        static final double BUSY = 1.0;
        static final double IDLE = 0.0;
        Accumulate utilization; //Record utilization
        Customer currentCust; //Current customer in service
        LinkedList<Customer> queue; //Queue of the server
        boolean openServer;
        boolean busyServer;

        public Server(Accumulate utilization) {
            this.utilization = utilization;
            utilization.init(IDLE);
            currentCust = null;
            queue = new LinkedList<>();
            busyServer = false;
        }

        @Override
        public void actions() {
            utilization.update(IDLE);
            busyServer = false;
            serviceCompleted(this, currentCust);
        }

        public void startService(Customer cust) {
            utilization.update(BUSY);
            busyServer = true;
            currentCust = cust;
            cust.serviceStarted();
            
            schedule(cust.serviceTime);//Schedule this event
            //after serviceTime time units
        }
        
        public void closeRegistry() {
            this.openServer = false;
        }
        
        public void openRegistry() {
            this.openServer = true;
        }
    }

    //Stop simulation by using this event
    class StopEvent extends Event {

        @Override
        public void actions() {
            Sim.stop();
        }
    }
}
