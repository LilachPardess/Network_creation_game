import java.util.*;
public class networkNode {
	int val;
	List<networkNode> neighbors;
	double cost;
	boolean asCostChange;
	
	networkNode(int value){
		val=value;
		neighbors= new LinkedList<>();
		cost= Double.POSITIVE_INFINITY;
		asCostChange=true;
	}

}
