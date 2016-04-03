# EgoNets And Strongly Connected Components (SCCs)
Extracted Ego Networks and SCCs from directed graph based on social network data

## Data
* The data is based on real Facebook data, collected by the [SNAP](https://snap.stanford.edu/data/egonets-Facebook.html) project
* The input was fed from a text file. The file consists of lines with 2 integers each, corresponding 
to an edge from "from" vertex to a "to" vertex.

## Ego Network
* An ego network for a user includes all their friends and the relationship between them. 
* Technically, an ego network for a node is a subgraph with all of its neighbors and the edges between them

## Strongly Connected Component
* An SCC is a maximal subgraph in which for every pair of nodes ```u``` and ```v```, there is a path in both directions 
between ```u``` and ```v```

## Description of classes
* ```Graph``` - interface for the graph methods
* ```CapGraph``` - implements Graph interface. Includes the methods to extract Ego Networks and SCC
* ```GraphLoader``` - used to load a graph from a text file
