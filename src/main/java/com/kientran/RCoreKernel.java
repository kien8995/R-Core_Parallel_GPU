package com.kientran;

import com.aparapi.Kernel;

public class RCoreKernel extends Kernel {

	private int currentVertex;
	private int[] rCore;
	private int[] adjListV;
	private int[] reachability;

	private int[] reachableSource;
	private int[][] reachableList;

	private int inputCase;

	private boolean temp[];

	public RCoreKernel(int[] rCore, int[] adjListV, int[] reachability, int[] reachableSource, int[][] reachableList,
			int inputCase) {
		this.rCore = rCore;
		this.adjListV = adjListV;
		this.reachability = reachability;
		this.reachableSource = reachableSource;
		this.reachableList = reachableList;
		this.inputCase = inputCase;

		temp = new boolean[reachability.length];

		this.currentVertex = -1;
	}

	public RCoreKernel(int currentVertex, int[] reachability, int[] reachableSource, int[][] reachableList,
			int inputCase) {
		this.currentVertex = currentVertex;
		this.reachability = reachability;
		this.reachableSource = reachableSource;
		this.reachableList = reachableList;
		this.inputCase = inputCase;

		temp = new boolean[reachability.length];

		this.rCore = new int[] { -1 };
		this.adjListV = new int[] { -1 };
	}

	@Override
	public void run() {
		int id = getGlobalId();
		if (inputCase == 1) {

			if (rCore[adjListV[id]] == -1) {
				reachability[adjListV[id]] = reachability[adjListV[id]] - 1;

				for (int source = 0; source < reachableSource.length; source++) {
					if (reachableSource[source] != -1 && containReachValue(reachableSource[source], adjListV[id])) {
						reachability[reachableSource[source]] = reachability[reachableSource[source]] - 1;
					}
				}

				temp[adjListV[id]] = true;
			}

		} else if (inputCase == 2) {
			int source = reachableSource[id];
			if (source != -1 && containReachValue(source, currentVertex)) {
				reachability[source] = reachability[source] - 1;

				temp[source] = true;
			}
		}
	}

	private boolean containReachValue(int row, int value) {
		for (int c = 0; c < reachableList[row].length; c++) {
			if (reachableList[row][c] == value) {
				return true;
			}
		}
		return false;
	}

	public int[] getResult() {
		int count = 0;
		for (int i = 0; i < temp.length; i++) {
			if (temp[i]) {
				count++;
			}
		}
		int[] ret = new int[count];
		count = 0;
		for (int i = 0; i < temp.length; i++) {
			if (temp[i]) {
				ret[count] = i;
				count++;
			}
		}
		return ret;
	}

	public int getCurrentVertex() {
		return currentVertex;
	}

	public void setCurrentVertex(int currentVertex) {
		this.currentVertex = currentVertex;
	}

	public int[] getrCore() {
		return rCore;
	}

	public void setrCore(int[] rCore) {
		this.rCore = rCore;
	}

	public int[] getAdjListV() {
		return adjListV;
	}

	public void setAdjListV(int[] adjListV) {
		this.adjListV = adjListV;
	}

	public int[] getReachability() {
		return reachability;
	}

	public void setReachability(int[] reachability) {
		this.reachability = reachability;
	}

	public int[] getReachableSource() {
		return reachableSource;
	}

	public void setReachableSource(int[] reachableSource) {
		this.reachableSource = reachableSource;
	}

	public int[][] getReachableList() {
		return reachableList;
	}

	public void setReachableList(int[][] reachableList) {
		this.reachableList = reachableList;
	}

	public int getInputCase() {
		return inputCase;
	}

	public void setInputCase(int inputCase) {
		this.inputCase = inputCase;
	}
}
