package com.kientran;

public class Edge {
	private String startNode;
	private String endNode;
	private int weight;

	public Edge(String startNode, String endNode, int weight) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.weight = weight;
	}

	public String getStartNode() {
		return startNode;
	}

	public void setStartNode(String startNode) {
		this.startNode = startNode;
	}

	public String getEndNode() {
		return endNode;
	}

	public void setEndNode(String endNode) {
		this.endNode = endNode;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "Edge [startNode=" + startNode + ", endNode=" + endNode + ", weight=" + weight + "]";
	}
}
