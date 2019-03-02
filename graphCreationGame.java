import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.jgrapht.alg.util.UnionFind;
public class graphCreationGame {
	int maxDeg;
	int maxEdgeNumber;
	int numOfEdges;
	int turnDetermintion;
	Queue<Integer> nextToPlay=new LinkedList<>();
	boolean[] equilibrium;
	boolean toDraw;
	streamGraph SupportsGraphDisplay;
	networkGraph graph; 
	int numOfNodes;
	int numOfItertion;
	boolean firstItertionCond;
	final double infinity=Double.POSITIVE_INFINITY;
	
	/**
	 * class constractor
	 * @param maxDeg
	 * @param maxEdgeNumber
	 * @param turnDetermintion
	 * @param graph
	 * @param toDraw
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	graphCreationGame(int maxDeg, int maxEdgeNumber,int turnDetermintion,networkGraph graph, boolean toDraw )
			throws FileNotFoundException, UnsupportedEncodingException{
	
		this.numOfEdges=0;
		this.maxEdgeNumber=maxEdgeNumber;
		this.graph=graph;
		this.maxDeg=maxDeg;
		this.numOfNodes=graph.nodes.length;
		this.turnDetermintion=turnDetermintion;
		this.SupportsGraphDisplay=new streamGraph(numOfNodes);
		this.toDraw=toDraw;
		if(toDraw) SupportsGraphDisplay.draw();
		numOfItertion=0;
		firstItertionCond=false;
	}
	/**
	 * Build graph.
	 * For all players(nodes), that next to play in current itertion, 
	 * (acording to turn detrmition Queue), call buildGraphAccordingToTurnDetermination.
	 * Continue until Equilibrium, e.g. no player want to change his strategy.
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws InterruptedException 
	 */
	public void buildGraph() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException{
		boolean totalEquilibrium=false;
		int maxDegTemp=maxDeg;
		if(firstItertionCond) {
			maxDeg=2;
		}
		//while equilibrium not echived keep playing
		while(!totalEquilibrium){
			totalEquilibrium=true;
			//set Queue for chosen nodes turn
			turnQueue.setQueue(this, graph, turnDetermintion, nextToPlay);
			if(nextToPlay.isEmpty()) {
				totalEquilibrium=true;
			}else {
				buildGraphAccordingToTurnDetermination();					
				//checkind who changed
				for(int i=0;i<numOfNodes;i++) {
					if(graph.nodes[i].asCostChange==true) totalEquilibrium=false;
			 	}
			}
			nextToPlay.removeAll(nextToPlay);
			numOfItertion++;
			//force stop game
			//if numOfItertion is 5 we conclude that game will not converge  
			if(numOfItertion==10 && turnDetermintion!=10) {
				numOfItertion=Integer.MAX_VALUE;
				return;
			}
			//case doesnt converge
			if(numOfItertion==numOfNodes*20 && turnDetermintion==4) {
				numOfItertion=Integer.MAX_VALUE;
				return;
			}
			maxDeg=maxDegTemp;
			System.out.println("num of iteretion"+numOfItertion);
		}
		//mathematical calculations for calculating the number of iterations
		if(turnDetermintion==4) {
			numOfItertion/=numOfNodes;
			numOfItertion++;
		}else {
			numOfItertion--;
		}
	}
	/**
	 * Each player play in his turn. 
	 * Each player (node) want to chose the best strategy consider only is own interests.
	 * A stagety is a group of nodes that the player can be nieghbor with.
	 * A player can build new edges and remove existing edges that 
	 * under his responsibility(only edges that he build in previes itiraiton).
	 * The best stagety of a player is the one that minimize the player's cost. 
	 * 
	 * Algorithm:
	 * 	For each player:
	 * 		find posible nieghbors.	
	 * 		fing all possible combination of possible neighbors 
	 * 		chose the the best combintion.
	 * 		update graph.
	 * @throws InterruptedException 
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public void buildGraphAccordingToTurnDetermination() throws InterruptedException, FileNotFoundException, UnsupportedEncodingException {	
		ArrayList<Integer> BRD=new ArrayList<>();
		ArrayList<Integer> legalNeighbors=new ArrayList<>();
		ArrayList<Integer> prevNeighbors=new ArrayList<>(); 
		double prevCost=0;
		double minCost=0;
		//for each node curPlayer , remove egdes in resposibility, find BRD build edges.
		for(int curPlayer: nextToPlay){
			prevCost=graph.nodes[curPlayer].cost;
			System.out.println("curPlayer: "+ curPlayer);
			System.out.println("curPlayer cost: "+ graph.nodes[curPlayer].cost);
			simulator.printGraph(graph);
			System.out.println("curPlayer is playing ");
			TimeUnit.SECONDS.sleep(0);
			findInResponsibilityNeibers(curPlayer,prevNeighbors); 
			removeEdgesUnderPlayerResponsibility(curPlayer);
			//update responsibility of removenEdges.
			graph.updateResponsobility(curPlayer,graph.getEdgesUnderRespo(curPlayer),true);
			findlegalNeighbors(legalNeighbors,curPlayer); 
			//finding best legal neibers, and put them in BRD list
			minCost=findBRD(BRD,graph,curPlayer,legalNeighbors);
			graph.nodes[curPlayer].asCostChange=!equalsLists(prevNeighbors,BRD);
			//if cost has not change the BRD is the prev nieghbors
			if(minCost==prevCost && minCost!=infinity) {
				graph.nodes[curPlayer].asCostChange=false;
				BRD.removeAll(BRD);
				BRD.addAll(prevNeighbors);
			}
			//building all chosen edges
			addEdgesToPlayer(curPlayer,BRD); //TODO ubdate hwo responsible on this edges
			graph.updateResponsobility(curPlayer,BRD,false);
			System.out.println("curPlayer build edges to: ");
			for(int nieghbor:BRD) System.out.println(nieghbor+", ");
			System.out.println("curPlayer: "+ curPlayer);
			System.out.println("curPlayer cost: "+ graph.nodes[curPlayer].cost);
			simulator.printGraph(graph);
			System.out.println("curPlayer is done playing ");
			//init all list for next node
			BRD.removeAll(BRD);
			legalNeighbors.removeAll(legalNeighbors);
			prevNeighbors.removeAll(prevNeighbors);	
		}
	}
	/**
	 * Find best response for curPlayer e.g who are the nieghbors that minimize his cost.
	 * Limitation on player response: 
	 * neighbor can be only the ones that appers in legalNeighbors list.
	 * player can not remove edges the in responsibility of another player(other player buils it).
	 * Algorithm:
	 * 		remove all edges that under player risponsibility.
	 * 		find set of all combination of possible nieghbors.
	 * 		find the nieghbors that minimize the cost.
	 * 		return min cost. 
	 * 	
	 * @param BRD - list of chosen neighbors
	 * @param graph
	 * @param curPlayer
	 * @param legalNeighbors
	 * @return mincost
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public double findBRD(ArrayList<Integer> BRD,networkGraph graph,int curPlayer,ArrayList<Integer> legalNeighbors) throws FileNotFoundException, UnsupportedEncodingException{
	
		Set<ArrayList<Integer>> allSubSet=new HashSet<>();
		//node curplayer cant controll edges that are not in node's responsibility
		// curplayer shuld take them for granted but still count them as part of his maxDeg
		int numOfEdgesNotInResponsibilty=graph.getNumOfEdgesNotInResponsibilty(curPlayer);
		int maxNumOfNieghbors=Math.min(maxEdgeNumber-numOfEdges, maxDeg-numOfEdgesNotInResponsibilty);
		//find all possible combination of neighbors
		for(int i=1;i<=maxNumOfNieghbors;i++) {
			if(i<=legalNeighbors.size()) {
			findCombination(legalNeighbors, legalNeighbors.size(), i,allSubSet);
			}
		}
		allSubSet.add(new ArrayList<>());
		//Find BRD among  allSubSet group
		//creat copy of graph, build the currentPossibleNeibers on the copy graph and compute cost. 
		graphCreationGame copyOfCurNetwork=this.deepClone();
		double minCost=infinity;
		double cost=0;
		ArrayList<Integer> currentPossibleNeighbors=new ArrayList<>();
		Iterator<ArrayList<Integer>> iterator = allSubSet.iterator();
		int maxNumOfConected=0;
		int numOfConected=0;
		while(iterator.hasNext()) {
			currentPossibleNeighbors=iterator.next();
			copyOfCurNetwork.addEdgesToPlayer(curPlayer,currentPossibleNeighbors);
			copyOfCurNetwork.graph.updateResponsobility(curPlayer, currentPossibleNeighbors, false);
	   		cost=networkGraph.computeNodeCost(curPlayer,copyOfCurNetwork.graph);
	    	//aim to find the group that minimize cost on curPlayer node.
	   		if(cost<minCost) {
	    		minCost=cost;
	   			BRD.removeAll(BRD);
	   			BRD.addAll(currentPossibleNeighbors);
	   		}

	   		//case min Cost is still ifinitiy 
	   		//TODO currentPossibleNeibers.size() need to be carfull
	    	if(minCost==infinity&& currentPossibleNeighbors.size()==maxNumOfNieghbors && BRD.isEmpty()) {
	    		numOfConected=0;
	    		//find all group of conected nodes is order to find witch nodes 
	   			//will reduce num of unConcted nodes.
	   			Set<Integer> setOfNodes=new HashSet<>();
	   			for(int i=0;i<numOfNodes;i++) setOfNodes.add(i);
	   			UnionFind groupOfNodes=new UnionFind(setOfNodes);
	   			for(int i=0;i< numOfNodes;i++) {
	   				for(int j=0;j< numOfNodes;j++) {
		   				if(graph.shortestPath[i][j]<infinity)groupOfNodes.union(i,j);
		   			}
	   			}
	   			//if cost is infinity the node will want to reduce num of unConcted nodes.
	   			boolean isNodesInDiffrantGroups=true;
	    		for(int possibleNeiber:currentPossibleNeighbors) {
	    			isNodesInDiffrantGroups&= (groupOfNodes.find(curPlayer)!=groupOfNodes.find(possibleNeiber));
	    			groupOfNodes.union(curPlayer,possibleNeiber);
	    		}
	    		//if all nodes in diffrent group then it can be the BDR
	   			if(isNodesInDiffrantGroups) {
	   				maxNumOfConected=numOfConected;
    				BRD.removeAll(BRD);
    				BRD.addAll(currentPossibleNeighbors);
	    		}
	    	}
	    	copyOfCurNetwork.removeEdgesUnderPlayerResponsibility(curPlayer);
	    	copyOfCurNetwork.graph.updateResponsobility(curPlayer, currentPossibleNeighbors, true);
	    }
		return minCost;
	}
	/**
	 * remove onlt the edges under player responsibility
	 * @param curPlayer
	 */
	public void removeEdgesUnderPlayerResponsibility( int curPlayer){

		ArrayList<Integer> neighborsInRisponsibility=new ArrayList<>();
		for(networkNode neighbor: graph.nodes[curPlayer].neighbors) {
			if(graph.responsibilityOnEdge[curPlayer][neighbor.val]==1) {
				neighborsInRisponsibility.add(neighbor.val);
				numOfEdges--;
				if(toDraw) SupportsGraphDisplay.removeEdge(curPlayer, neighbor.val);
			}
			
		}
		graph.removeMultipyEdge(curPlayer,neighborsInRisponsibility);
	}
	/**
	 * Build chosen neibors
	 * @param curPlayer
	 * @param BRD
	 */
	public void addEdgesToPlayer( int curPlayer, ArrayList<Integer> BRD){
			graph.buildMultipyEdge(curPlayer, BRD);
			for(int chosenNeiber: BRD){
				if(toDraw)SupportsGraphDisplay.buildEdge(curPlayer, chosenNeiber);
				numOfEdges++;
			}		
	}
	/**
	 * Put in nieghbors list all the neighbors that under player responsibily. 
	 * @param curPlayer
	 * @param neibers
	 */
	public void findInResponsibilityNeibers( int curPlayer,ArrayList<Integer> neibers) {
		for(int i=0;i<numOfNodes;i++) {
			if(graph.graphMatix[curPlayer][i]==1 && graph.responsibilityOnEdge[i][curPlayer]!=1) 
				neibers.add(i);
		}
	}
	
	/**
	 * Put in legallNeibers list all the nodes that 
	 * curplayer can build edge them under game limition: maxDeg, responibility.
	 * @param legallNeibers
	 * @param curPlayer
	 */
	public void findlegalNeighbors(ArrayList<Integer> legallNeibers, int curPlayer){
		for(networkNode node: graph.nodes) {
			if(node.val!=curPlayer && node.neighbors.size()<maxDeg
			   && graph.responsibilityOnEdge[node.val][curPlayer] !=1) 
				
				legallNeibers.add(node.val);
		}
	}
	/**
	 * Make a deep copy of graphCreationGame.
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public graphCreationGame deepClone() throws FileNotFoundException, UnsupportedEncodingException {
		graphCreationGame clone=new graphCreationGame(maxDeg,maxEdgeNumber,turnDetermintion,graph,false);
		clone.numOfNodes=this.numOfNodes;
		clone.graph=graph.deepClone();
		clone.numOfEdges=this.numOfEdges;
    	return clone;
	}
	/**
	 * find all combinations of size r in arr[] of size n. This function  
     * mainly uses combinationUtil() 
	 * @param arr
	 * @param n
	 * @param r
	 * @param allSubSet
	 */
	static void findCombination(ArrayList<Integer> arr, int n, int r,Set<ArrayList<Integer>> allSubSet)  { 
		 // A temporary array to store all combination 
	     // one by one 
	     int data[] = new int[r]; 
	  
	     // Print all combination using temprary 
	     // array 'data[]' 
	     combinationUtil(arr, n, r, 0, data, 0, allSubSet); 
	 }
	/**
	 *  
	 * @param arr[]  ---> Input Array 
	 * @param data[] ---> Temporary array to store current combination 
	    start & end ---> Staring and Ending indexes in arr[] 
	 * @param index ---> Current index in data[] 
	    r ---> Size of a combination to be printed 
	 * @param n
	 * @param r
	 * @param i
	 * @param allSubSet
	 */
	public static void combinationUtil(ArrayList<Integer> arr, int n, int r, 
            int index, int data[], int i, Set<ArrayList<Integer>> allSubSet) {
		// Current combination is ready to be printed,  
        // print it 
        if (index == r) {
        	ArrayList<Integer> subSet=new ArrayList<>();
            for (int j = 0; j < r; j++) 
                subSet.add(data[j]); 
            allSubSet.add(subSet);
            return; 
        } 
  
        // When no more elements are there to put in data[] 
        if (i >= n) 
            return; 
  
        // current is included, put next at next 
        // location 
        data[index] = arr.get(i); 
        combinationUtil(arr, n, r, index + 1,  
                               data, i + 1,allSubSet); 
  
        // current is excluded, replace it with 
        // next (Note that i+1 is passed, but 
        // index is not changed) 
        combinationUtil(arr, n, r, index, data, i + 1,allSubSet); 
	}
	/**
     * Comper element of BRD list and previus neibers list
     * @param BRD
     * @param prevNeiber
     * @return true if both list contaiens same elements.
     */
	public boolean equalsLists(ArrayList<Integer> BRD,ArrayList<Integer> prevNeiber) {
		if(BRD.size()!=prevNeiber.size()) return false;
    	HashSet<Integer> BRDSet= new HashSet<>();
    	for(int elem:BRD) BRDSet.add(elem);
    	for(int elem:prevNeiber) {
    		if(!BRD.contains(elem)) return false;
    	}
    	return true;	
	}
	
	 /**
     *creat csv file according to builed graph 
     * @throws IOException 
     */
    public void creatCSVFile(FileWriter writer) throws IOException {
    	writer.append('\n');

    	writer.append("ID,");
    	writer.append(numOfNodes+",");
    	writer.append(graph.alpha+",");
    	writer.append(maxDeg+",");
    	writer.append(maxEdgeNumber+",");
    	writer.append(turnDetermintion+",");
    	writer.append(graph.computeSocialCost(graph)+",");
    	writer.append(graph.ComputeGrapheCogestion(graph)+",");
    	writer.append(numOfItertion+",");
    	writer.append(maxEdgeNumber-numOfNodes+",");
    	writer.append(this.firstItertionCond+",");
    	int numOfEgdes=0;
    	for(int i=0;i<numOfNodes;i++) {
    		for(int j=i+1;j<numOfNodes;j++) {
    			numOfEgdes+=graph.graphMatix[i][j];
    		}
    	}
    	
    	writer.append(numOfEgdes+",");
    	
    	for(int i=0;i<numOfNodes;i++) {
    		writer.append(graph.congestion[i]+",");
    	}
    	writer.flush();
    	writer.close();
    }
}
