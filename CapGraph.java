/**
 * 
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import util.GraphLoader;

/**
 * @author Praveen Sankaranarayanan
 *
 */
public class CapGraph implements Graph {
	private int numVertices;
	private int numEdges;
	// map between each vertex and its connected edges
	private HashMap<Integer, HashSet<Integer>> adjListMap;
	
	// map between each vertex and its corresponding GraphNode
	private HashMap<Integer, GraphNode> nodeMap;
	
	public CapGraph(){
		this.numVertices = 0;
		this.numEdges = 0;
		this.adjListMap = new HashMap<Integer, HashSet<Integer>>();
		this.nodeMap = new HashMap<Integer, GraphNode>();
	}
	
	/**
	 * Add the vertex 'num' to the graph
	 */
	@Override
	public void addVertex(int num) {
		// add only if vertex not already present
		if(!nodeMap.containsKey(num)){
			GraphNode node = new GraphNode(num);
			nodeMap.put(num,node);
			adjListMap.put(num, node.getEdges());
			numVertices++;
		}
	}

	/**
	 * Add a directed edge ('from' --> 'to')
	 */
	@Override
	public void addEdge(int from, int to) {
		if(!nodeMap.containsKey(from) || !nodeMap.containsKey(to)){
			throw new IllegalArgumentException("Either 'from' or 'to' not present in the graph");
		}
		GraphNode fromNode = nodeMap.get(from); // get the 'from' GraphNode
		fromNode.addDirectedEdge(to); // add the edge
		adjListMap.get(from).add(to); // add the edge to adjListMap
		numEdges++;
	}

	/**
	 * get the Ego Network for a node
	 * A EgoNet for a node is the sub-graph that includes all of the node's neighbors
	 * and the edges between them
	 */
	@Override
	public Graph getEgonet(int center) {
		// return empty graph if vertex is not present
		if(!nodeMap.containsKey(center)){
			return new CapGraph();
		}
		Graph egoNet = new CapGraph();
		// add the center to egonet
		egoNet.addVertex(center);
		
		// add center's neighbors to egonet, create edges to neighbors
		for(Integer neighbor: adjListMap.get(center)){
			egoNet.addVertex(neighbor);
			egoNet.addEdge(center, neighbor);			
		}
		
		// iterate through neighbors, find if egoNet already has neighbor's neighbors. 
		// If yes, add corresponding edge 
		for(Integer neighbor: adjListMap.get(center)){
			HashSet<Integer> edges = adjListMap.get(neighbor);
			for(int e: edges){
				if(((CapGraph)egoNet).containsNode(e)){
					egoNet.addEdge(neighbor,e);
				}
			}
		}
		return egoNet;
	}

	/*
	 * get the strongly connected components of a directed graph
	 * Use algorithm from Coursera lecture:
	 * Step 1: Do DFS(G) keeping track of the order in which the nodes finish
	 * Step 2: Transpose G (reverse all edges)
	 * Step 3: Do DFS(G-Transpose) exploring in the reverse order of finish from step 1
	 */
	
	@Override
	public List<Graph> getSCCs() {
		Stack<Integer> vertices = new Stack<Integer>();
		Stack<Integer> finished = new Stack<Integer>(); // track the nodes that have finished visiting all neighbors
		HashSet<Integer> visited = new HashSet<Integer>();
		
		// create a stack of all the vertices in the graph
		for(Integer v: nodeMap.keySet()){
			vertices.push(v);
		}
		// Step 1
		DFS(vertices,finished,visited);
		
		// Step 2
		CapGraph transpose = getTranspose();
		
		// Step 3
		vertices = finished;
		finished = new Stack<Integer>();
		visited = new HashSet<Integer>();
		List<List<Integer>> sccList = transpose.DFS(vertices, finished, visited);
//		System.out.println(sccList);
		List<Graph> SCCGraphList = new ArrayList<Graph>();
		for(List<Integer> scc: sccList){
			Graph sccGraph = buildSCCFromList(scc);
			SCCGraphList.add(sccGraph);
		}
		return SCCGraphList;
	}
	
	/**
	 * Do DFS on all vertices in the graph, return a list of SCCs
	 * @param vertices
	 * @param finished
	 * @param visited
	 * @return A list of SCCs in the graph
	 * Note: The list of SCCs is valid only in the second call to this function in step 3 of the algorithm
	 */
	private List<List<Integer>> DFS(Stack<Integer> vertices,Stack<Integer> finished,HashSet<Integer> visited){
		List<List<Integer>> sccList = new ArrayList<List<Integer>>();
		while(!vertices.empty()){
			int v = vertices.pop();
			if(!visited.contains(v)){
				List<Integer> scc = new ArrayList<Integer>();// contains the SCC rooted at this vertex
				DFSVisit(v,finished,visited,scc);
				sccList.add(scc);
			}
		}
		return sccList;
	}

	/**
	 * Do DFS starting at the given vertex v
	 * @param v
	 * @param finished
	 * @param visited
	 */
	private void DFSVisit(int v, Stack<Integer> finished,HashSet<Integer> visited,List<Integer> scc){
		visited.add(v);
		scc.add(v);
		GraphNode node = nodeMap.get(v);
		for(int neighbor: node.getEdges()){
			if(!visited.contains(neighbor)){
				DFSVisit(neighbor, finished, visited, scc);
			}
		}
		finished.push(v);
	}
	
	/**
	 * get the transpose of a graph i.e. all edges reversed
	 * @return transpose of graph
	 */
	private CapGraph getTranspose(){
		CapGraph transpose = new CapGraph();
		for(int v: nodeMap.keySet()){
			transpose.addVertex(v);
			for(int neighbor: adjListMap.get(v)){
				transpose.addVertex(neighbor);
				transpose.addEdge(neighbor, v);
			}
		}
		return transpose;
	}
	
	/**
	 * Takes a list of nodes in scc, and builds a sub-graph
	 * @param scc - the list containing the SCC
	 * @return a sub-graph built from scc
	 */
	private Graph buildSCCFromList(List<Integer> scc){
		Graph sccGraph = new CapGraph();
		for(int v: scc){
			sccGraph.addVertex(v);
			for(int neighbor: adjListMap.get(v)){
				if(scc.contains(neighbor)){
					sccGraph.addVertex(neighbor);
					sccGraph.addEdge(v, neighbor);
				}
			}
		}
		return sccGraph;
	}
	
	/**
	 * return a map between each vertex and its edges
	 */
	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {	
		return new HashMap<Integer, HashSet<Integer>>(adjListMap);
	}

	public int getNumVertices(){
		return numVertices;
	}
	public int getNumEdges(){
		return numEdges;
	}
	
	public void printGraph(){
		HashMap<Integer, HashSet<Integer>> graphMap = exportGraph();
		for(Integer i: graphMap.keySet()){
			System.out.println(i + "-> " + graphMap.get(i));
		}
	}
	
	public boolean containsNode(int vertex){
		return this.nodeMap.containsKey(vertex);
	}
	
	public static void main(String[] args){
		CapGraph myGraph = new CapGraph();
		GraphLoader.loadGraph(myGraph, "data/small_graph.txt");
		myGraph.printGraph();
		System.out.println("Vertices: " + myGraph.getNumVertices());
		System.out.println("Edges: "+ myGraph.getNumEdges());
		Graph egoNet = myGraph.getEgonet(25);
		((CapGraph) egoNet).printGraph();
		System.out.println("******");
		egoNet = myGraph.getEgonet(44);
		((CapGraph) egoNet).printGraph();
		System.out.println("Transpose:");
		CapGraph transpose = myGraph.getTranspose();
		transpose.printGraph();
		System.out.println("Vertices: " + transpose.getNumVertices());
		System.out.println("Edges: "+ transpose.getNumEdges());
		List<Graph> SCCs = myGraph.getSCCs();
		for(Graph scc: SCCs){
			((CapGraph)scc).printGraph();
			System.out.println("****");
		}
	}
}

/**
 * 
 * @author praveens
 * A class to represent a node in the graph
 */
class GraphNode{
	private int id;
	private HashSet<Integer> edges;
	
	public GraphNode(int id){
		this.id = id;
		this.edges = new HashSet<Integer>();
	}
	
	public void addDirectedEdge(int vertex){
		// HashSet will not add duplicate edges
		this.edges.add(vertex);
	}
	
	public HashSet<Integer> getEdges(){
		return new HashSet<Integer>(edges);
	}
	
	public int getID(){
		return this.id;
	}
}

