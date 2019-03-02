# Network_creation_game
**Network_creation_game simultion**
The network creation game was  first introduced in Fabrikant et al, 2003.
This simultion built as part of research project on this game. The purpose of the research is to 
determine which heuristics minimize the cost function and contribute to the reduction of congestion problems,
maintaining optimal complexity and solution quality. 

Game introduction
The game begins with a graph without edges. Player i represented by node i in the graph. 
Each player can choose his best strategy in his turn, a strategy means to choose nieghbors. 
Each player plays in his turn, he can delete existing edges outgoing from his node and add new outgoing edges. 
A player can act in any way as long he resists the limitations of the game (Maximum degree, Maximum edges).
Each player chooses the best strategy considering only is own good. The BRD halts when no player has a beneficial move. see convergence
of game in [1]. The aim of each palyer is to minimize his own cost function. The cost function includes shortest path, 
the price alpha of edgess, and the number of out-going edes.

Implemented Algorithms
Network Creation 
•	Start with graph of nodes with no edges.
•	Until no node wants to change his set of neighbors:
o	For each node:
	Find all possible set of neighbors- all groups of nodes that the player can build an edge to them under game limitation.  
	Find the group of nodes that building an edge to them minimizes the cost function. 
	Build the chosen edges.
•	Return the graph

Find all possible set of neighbors 
•	The possible neighbors are
o	Those whose degree does not exceed the Maximum degree. 
o	Those who don’t have out-going edge to current player.  
•	A possible set of neighbors:
o	Belong to the set of all subsets of possible neighbors (not include player himself).
o	Is one whose size, in addition to the size of existing player's neighbors does not exceed the Maximum degree.
o	Is one whose size, in addition to the number of the other edges, does not exceed Maximum edges. 
•	Return a set of all possible set neighbors 

Deviation rule\turn determtion
Return the next deviating player according to the implemented deviator rule. 
The Deviation rule is pre-selection, and the deviation function dynamically determine the next order according to
which the players perform.

[1]Previous work has shown that for Trees convergence guarantee. However, even one non-tree edge suffices to destroy the convergence
guarantee. Thus, for non-tree graph Nash equilibrium does not necessarily can be achieved.
