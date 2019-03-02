import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Queue;

public class turnQueue {
	
	public static void setQueue(graphCreationGame tree,networkGraph graph,int turnDeter,Queue<Integer> NextToPlayQueue ) throws FileNotFoundException, UnsupportedEncodingException {
		if(turnDeter==0) {
			RR(graph,NextToPlayQueue,turnDeter);
		}else if(turnDeter==1) {
			maxCost(graph,NextToPlayQueue,turnDeter);
		}else if(turnDeter==2) {
			maxDegree(graph,NextToPlayQueue,turnDeter);
		}else if(turnDeter==3) {
			
		}else if(turnDeter==4) {
			maxGainPure(tree,NextToPlayQueue,turnDeter);
		}
	}
	
	public static void RR(networkGraph graph,Queue<Integer> NextToPlayQueue,int turnDeter) {
		for(int i=0;i<graph.nodes.length;i++) {
			NextToPlayQueue.add(i);
		}
	}
	
	public static void maxCost(networkGraph graph,Queue<Integer> NextToPlayQueue,int turnDeter) {
		double[][] costAndNode=new double[graph.nodes.length][2];
		for(int i=0;i<graph.nodes.length;i++) {
			costAndNode[i][0]=networkGraph.computeNodeCost(i, graph);
			costAndNode[i][1]=i;
		}
		
		Arrays.sort(costAndNode, Comparator.comparing((double[] arr) -> arr[0]).reversed());
		for(int i=0;i<graph.nodes.length;i++) {
			NextToPlayQueue.add((int)costAndNode[i][1]);
		}
		toString(NextToPlayQueue,turnDeter);
	}
	
	public static void maxDegree(networkGraph graph,Queue<Integer> NextToPlayQueue,int turnDeter) {
		double[][] degreeAndNode=new double[graph.nodes.length][2];
		for(int i=0;i<graph.nodes.length;i++) {
			degreeAndNode[i][0]=graph.getDegree(i);
			degreeAndNode[i][1]=i;
		}
		Arrays.sort(degreeAndNode, Comparator.comparing((double[] arr) -> arr[0]).reversed());
		for(int i=0;i<graph.nodes.length;i++) {
			NextToPlayQueue.add((int)degreeAndNode[i][1]);
		}
		toString(NextToPlayQueue,turnDeter);
	}
	
	public static void minDegree(networkGraph graph,Queue<Integer> NextToPlayQueue,int turnDeter) {
		//TODO
	}
	public static void maxGainPure(graphCreationGame networkGraph,Queue<Integer> NextToPlayQueue,int turnDeter) throws FileNotFoundException, UnsupportedEncodingException {
		ArrayList<Integer> BRD=new ArrayList<>();
		ArrayList<Integer> legalNeibers=new ArrayList<>();
		graphCreationGame temp=networkGraph.deepClone();
		
		double cost=0;
		double maxGain=0;
		double gain=0;
		double prevCost=0;
		int curPlayer=0;
		boolean b=false;
		//for each player we cheack his cost gain if his turn was now
		for(curPlayer=0;curPlayer<networkGraph.numOfNodes;curPlayer++) {
			
			prevCost=temp.graph.nodes[curPlayer].cost;
			temp.removeEdgesUnderPlayerResponsibility(curPlayer);
			//findind all nodes such that ther degree is smaller then maxdeg-1
			legalNeibers.removeAll(legalNeibers);
			temp.findlegalNeighbors(legalNeibers, curPlayer);
			//findind best neibers and cost of node after change
			cost=temp.findBRD(BRD,temp.graph,curPlayer,legalNeibers);
			//comute gain
			gain=prevCost-cost;
			//if cost an the end and of loop is still infinity, we wil want to oparet diffrently
			if(cost<Double.POSITIVE_INFINITY) b=true;
			
			if(gain>maxGain) {
				maxGain=gain;
				if(NextToPlayQueue.size()>0) NextToPlayQueue.poll();
				NextToPlayQueue.add(curPlayer);
			}
			temp=networkGraph.deepClone();
		}
		if(!b) {
			for(curPlayer=0;curPlayer<networkGraph.numOfNodes;curPlayer++) {
				if(networkGraph.graph.getDegree(curPlayer)==0) {
					NextToPlayQueue.add(curPlayer);
					break;
				}
			}
		}
	}
	
	public static void toString(Queue<Integer> nodes,int turnDeter) {
		StringBuilder string = new StringBuilder();
		string.append("Queue ");
		if(turnDeter==0) {
			string.append(" RR :");
		}else if(turnDeter==1) {
			string.append(" MaxCost :");
		}
		for(int node:nodes) {
		    string.append(node);
		}
		System.out.println(string);
	}

}
