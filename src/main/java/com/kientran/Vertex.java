package com.kientran;

public class Vertex implements Comparable<Vertex> {
	private String vertex;
	private int degree;

	public Vertex(String vertex, int degree) {
		this.vertex = vertex;
		this.degree = degree;
	}

	public void setVertex(String vertex) {
		this.vertex = vertex;
	}

	public String getVertex() {
		return vertex;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public int getDegree() {
		return degree;
	}

	@Override
	public int compareTo(Vertex obj) {

		Vertex vertex = obj;

		if (this.degree == vertex.getDegree()) {
			return 0;
		} else if (this.degree > vertex.getDegree()) {
			return 1;
		} else {
			return -1;
		}
	}
}