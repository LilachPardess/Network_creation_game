import java.nio.file.Path;
import java.util.*;
public class networkGraph {
	networkNode[] nodes;
	double[][] shortestPath;
	double alpha;
	int[][] graphMatix;
	int[][] responsibilityOnEdge;
	double[] congestion;
	final static double infinity=Double.POSITIVE_INFINITY;

	/**
	 * class constrector
	 * @param numOfNodes : pre-selected variable
	 * @param a: pre-selected variable
	 */
	networkGraph (int numOfNodes, double a){	
		nodes=new networkNode[numOfNodes];
		for(int i=0;i<numOfNodes;i++) nodes[i]=new networkNode(i);
		alpha=a; 
		graphMatix=new int[numOfNodes][numOfNodes];
		responsibilityOnEdge=new int[numOfNodes][numOfNodes];	
		shortestPath=new double[numOfNodes][numOfNodes];
		congestion=new double[numOfNodes];
		for(int i=0;i<numOfNodes;i++) {
			for(int j=0;j<numOfNodes;j++) {
				shortestPath[i][j]= (i==j) ? 0 : infinity;
			}
		}
	}
	
	/**
	 * build new edge between node1 and node2
	 * @param node1
	 * @param node2
	 */
	public void buildEdge(int node1,int node2) {
		if(node1<0 || node1>nodes.length-1) {
			System.out.println("Trying to add an edge but node "+node1+" out of bound");
			return;
		}
		if(node2<0 || node2>nodes.length-1) {
			System.out.println("Trying to add an edge but node "+node2+" out of bound");
			return;
		}
		if(node1==node2) return; // don't build edge. same node.
		if(graphMatix[node1][node2]==1) return; // don't build edge. edge already exist.
		nodes[node1].neighbors.add(nodes[node2]);
		nodes[node2].neighbors.add(nodes[node1]);
		graphMatix[node1][node2]=1;
		graphMatix[node2][node1]=1;
		for(int i=0;i<shortestPath.length;i++) {
			for(int j=0;j<shortestPath.length;j++) {
				if(shortestPath[i][node1]+shortestPath[node2][j]+1< shortestPath[i][j]) {
					shortestPath[i][j]=shortestPath[i][node1]+shortestPath[node2][j]+1;
					shortestPath[j][i]=shortestPath[i][node1]+shortestPath[node2][j]+1;
				}
			}
		}
		updateNodesCost();
	}
	
	/**
	 * build multipy edges between node1 and all nodes in nieghbors list.
	 * @param node1
	 * @param neibers
	 */
	public void buildMultipyEdge(int node1,ArrayList<Integer> neighbors) {
		if(node1<0 || node1>nodes.length-1) {
			System.out.println("Trying to add an edge but node "+node1+" out of bound");
			return;
		}
		for(int node2:neighbors){
			if(node2<0 || node2>nodes.length-1) {
				System.out.println("node "+node2+" out of bound");
				return;
			}
			if(node1==node2 || graphMatix[node1][node2]==1) continue;
			nodes[node1].neighbors.add(nodes[node2]);
			nodes[node2].neighbors.add(nodes[node1]);
			graphMatix[node1][node2]=1;
			graphMatix[node2][node1]=1;
			for(int i=0;i<shortestPath.length;i++) {
				for(int j=0;j<shortestPath.length;j++) {
					if(shortestPath[i][node1]+shortestPath[node2][j]+1< shortestPath[i][j]) {
						shortestPath[i][j]=shortestPath[i][node1]+shortestPath[node2][j]+1;
						shortestPath[j][i]=shortestPath[i][node1]+shortestPath[node2][j]+1;
					}
				}
			}
		}
		updateNodesCost();
	}
	/**
	 * remove edge betweem node1 and node2
	 * @param node1
	 * @param node2
	 */
	public void removeEdge(int node1,int node2) {
		if(node1<0 || node1>nodes.length-1) {
			System.out.println("trying to remove  an edge from "+node1+" but node out of bound");
			return;
		}
		if(node2<0 || node2>nodes.length-1) {
			System.out.println("trying to remove  an edge from "+node2+ " but node out of bound");
			return;
		}
		if(graphMatix[node1][node2]==0) {
			System.out.println("trying to remove an edge"+node1+","+node2+" but edge doesnt exist");
			return;
		}
		graphMatix[node1][node2]=0;
		graphMatix[node2][node1]=0;
		nodes[node1].neighbors.remove(nodes[node2]);
		nodes[node2].neighbors.remove(nodes[node1]);
		getAndUpdateShorestPathMatrix();
		updateNodesCost();
	}
	
	/**
	 * remove multipy edges between node1 and all nodes in nieghbors list.
	 * @param node1
	 * @param neibers
	 */
	public void removeMultipyEdge(int node1,ArrayList<Integer> neighbors) {
		for(int node2: neighbors) {
			if(node1<0 || node1>nodes.length-1) {
				System.out.println("trying to remove but node "+ node1+" out of bound");
				return;
			}
			if(node2<0 || node2>nodes.length-1) {
				System.out.println("trying to remove but node "+node2+" out of bound");
				return;
			}
			if(graphMatix[node1][node2]==0) continue;
			
			graphMatix[node1][node2]=0;
			graphMatix[node2][node1]=0;
			nodes[node1].neighbors.remove(nodes[node2]);
			nodes[node2].neighbors.remove(nodes[node1]);
		}
		getAndUpdateShorestPathMatrix();
		updateNodesCost();
	}
	/**
	 * 
	 * @param node
	 * @return the degree of a node
	 */
	public int getDegree(int node) {
		int degree=0;
		for(int i=0;i<nodes.length;i++) {
			degree+=graphMatix[node][i];
		}
		return degree;
	}
	
	public int getNumOfEdgesNotInResponsibilty(int curPlayer) {
		int NumOfEdgesNotInResponsibilty=0;
		for(int i=0; i<nodes.length; i++) {
			NumOfEdgesNotInResponsibilty+=responsibilityOnEdge[i][curPlayer];
		}
		return NumOfEdgesNotInResponsibilty;
	}
	
	/**
	 * compute the cost of node according to the cost formula
	 * cost is the sum of all shortest path from node to all other nodes
	 * plus the nuber of edges that in node responsibility multipy with alpha
	 * @param node
	 * @param graph
	 * @return
	 */
	public static double computeNodeCost(int node,networkGraph graph) {
		double cost=0;
		int numOfEdge=0;
		for(int j=0;j<graph.nodes.length;j++) {
			cost+=graph.shortestPath[node][j];
			numOfEdge+=graph.graphMatix[j][node];
			numOfEdge-=(graph.responsibilityOnEdge[j][node]==1)?1:0;
		}
		cost+= graph.alpha*numOfEdge;
		return cost;
	}
	/**
	 * update cost fild of all nodes in graph
	 */
	public void updateNodesCost() {
		for(int i=0;i<nodes.length;i++) nodes[i].cost=computeNodeCost(i,this);	
	}
	
	/**
	 * Social cost is the sum of all node's cost.
	 * @param graph
	 * @return
	 */
	public static double computeSocialCost(networkGraph graph) {
		double cost=0;
		for(networkNode node: graph.nodes) cost+=computeNodeCost(node.val,graph);
		return cost;
	}
	
	/**
	 * Compue graph congestion
	 * congestion on a node difined as the number of all shortest path 
	 * in the graph that use this node.
	 * Graph congestion define by the proprtion of the max congestion 
	 * of a node in the graph and the min congestion of node in the graph.
	 * @param graph
	 * @return graph congestion
	 */
	public static double ComputeGrapheCogestion(networkGraph graph) {
		Graph computeCongestionGraph=new Graph(graph);
		Function.modified_BFS(computeCongestionGraph);
		double maxCong=0;
		double minCong=infinity;
		double curCongestion;
		for(int i=0;i<graph.nodes.length;i++) {
			curCongestion=computeCongestionGraph.Vertices.get(i).load+graph.nodes.length;
			graph.congestion[i]=curCongestion;
			if(curCongestion>maxCong) {
				maxCong=curCongestion;
			}
			if(curCongestion<minCong) {
				minCong=curCongestion;
			}
		}
		return maxCong/minCong;
	}
	/**
	 * Compute shortest path between all pairs of nodes using BFS 
	 * and update shortest path matrix.
	 * @return the shotrest path matix.
	 */
	public double[][] getAndUpdateShorestPathMatrix(){

		for(int i=0;i<nodes.length;i++) {
			for(int j=0;j<nodes.length;j++) {
				shortestPath[i][j]= (i!=j) ? infinity : 0;
			}
		}
		
		// Run BFS for each node in graph as he his the root, and compute shortest path
		//for all other nodes.
		for(int root=0;root<nodes.length;root++) {
			Queue<networkNode> bfsQueue=new LinkedList<>();
			Queue<networkNode> bfsQueueChild=new LinkedList<>();
			boolean[] marked=new boolean[nodes.length];
			Arrays.fill(marked, false);
			marked[root]=true;
			networkNode cur=nodes[root];
			int bfsLevel=1;
			if(cur==null) continue;
			bfsQueue.add(cur);
			
			while(!bfsQueue.isEmpty()){
				while(!bfsQueue.isEmpty()) {
					cur=bfsQueue.remove();
					for(networkNode child: cur.neighbors) {
						if(marked[child.val]==false) {
							marked[child.val]=true;
							bfsQueueChild.add(child);
							shortestPath[root][child.val]=bfsLevel;
						}
					}
				}
				bfsQueue.addAll(bfsQueueChild);
				bfsQueueChild.removeAll(bfsQueueChild);
				bfsLevel++;
			}
		}
		return shortestPath;
	}
	/**
	 * return a list with all edges than node curPlayer has responsibility on them.
	 * @param curPlayer
	 * @return
	 */
	public ArrayList<Integer> getEdgesUnderRespo(int curPlayer){
		ArrayList<Integer> edgesUnderRespo=new ArrayList<>();
		for(int i=0;i<nodes.length;i++) {
			if(this.responsibilityOnEdge[curPlayer][i]==1) edgesUnderRespo.add(i);
		}
		return edgesUnderRespo;
	}
	/**
	 * update responsibilityOnEdge matrix
	 * if remove is true then this edges not in curPlayer node's responsibility any more
	 * else this edges is under curPlayer node's responsibility 
	 * @param curPlayer
	 * @param edgesToUpdate
	 * @param remove
	 */
	public void updateResponsobility(int curPlayer, ArrayList<Integer> edgesToUpdate, boolean remove) {
		if(!remove) {
			for(int newNeighbor:edgesToUpdate) {
				responsibilityOnEdge[curPlayer][newNeighbor]=1;
			}
		}else {
			for(int newNeighbor:edgesToUpdate) {
				responsibilityOnEdge[curPlayer][newNeighbor]=0;
			}
		}
		
	}
	/**
	 * Create a deep copy  of the network graph.
	 * @return the a copy of graph.
	 */
	public networkGraph deepClone() {
		networkGraph graphClone=new networkGraph(nodes.length, alpha); 
		for(int i=0;i<graphMatix.length;i++) {
			graphClone.shortestPath[i]=Arrays.copyOf(this.shortestPath[i],graphMatix.length);	
			graphClone.graphMatix[i]=Arrays.copyOf(this.graphMatix[i], graphMatix.length);
			graphClone.responsibilityOnEdge[i]=Arrays.copyOf(this.responsibilityOnEdge[i], graphMatix.length);
			graphClone.nodes[i].cost=computeNodeCost(i,this);
			for(int j=0;j<graphMatix.length;j++) {
				if(graphMatix[i][j]==1) graphClone.nodes[i].neighbors.add(graphClone.nodes[j]);
			}	
		}
		return graphClone;	
	}
	
}

