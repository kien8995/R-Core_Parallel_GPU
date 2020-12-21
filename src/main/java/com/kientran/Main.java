package com.kientran;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.aparapi.Range;

public class Main {

	static {
		System.setProperty("com.aparapi.executionMode", "GPU");
		System.setProperty("com.aparapi.dumpProfilesOnExit", "true");
		System.setProperty("com.aparapi.enableExecutionModeReporting", "false");
		System.setProperty("com.aparapi.enableShowGeneratedOpenCL", "true");
	}

	// path to input/output file
	private static final String INPUT = "SequenceAssociation.txt";
	private static final String OUTPUT = "output.txt";

	// list to store edges
	private List<Edge> edgeList;
	// map to store r-core
	private int[] rCore;
	// map to store adjacency list
	private Map<Integer, ArrayList<Integer>> adjList;
	// map to store reach level
	private int[] reachLevel;
	// vertex queue
	private PriorityQueue<Vertex> vertexQueue;

	//
	private int[] reachableSource;
	private int[][] reachableList;
	//

	// sets vertex
	private Set<String> setV;

	// convert vertex string to intId
	private Map<String, Integer> vStringToInt;

	// convert vertex intId to string
	private String[] vStringArray;

	private int numberOfVertexs;
	private int numberOfEdges;

	// temp
	private Set<Integer> visited;
	private int reachListIndex = 0;
	private int[] reachListColumnIndex;

	public static void main(String[] args) throws Exception {
		int mb = 1024 * 1024;
		// Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();
		System.out.println("##### Heap utilization statistics [MB] #####");

		Main main = new Main();
		main.init();
		main.readFile();
		main.loadData();
		long start = System.currentTimeMillis();
		main.compute();
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		main.writeTextFile();

		// Print used memory
		System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
	}

	// initialize
	private void init() {
		edgeList = new ArrayList<>();
		adjList = new HashMap<>();
		vertexQueue = new PriorityQueue<>();
		setV = new HashSet<>();
		visited = new HashSet<>();
		vStringToInt = new HashMap<>();
	}

	// read input.txt and convert edge list to adjacency list
	public void readFile() {

		Path path = Paths.get(INPUT);

		try (Stream<String> lines = Files.lines(path)) {
			Spliterator<String> lineSpliterator = lines.spliterator();
			Spliterator<Edge> edgeSpliterator = new EdgeSpliterator(lineSpliterator);

			Stream<Edge> edgeStream = StreamSupport.stream(edgeSpliterator, false);
			edgeStream.forEach(edge -> edgeList.add(edge));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// load data
	private void loadData() {
		for (Edge edge : edgeList) {
			setV.add(edge.getStartNode());
			setV.add(edge.getEndNode());
		}

		numberOfEdges = edgeList.size();
		numberOfVertexs = setV.size();

		rCore = new int[numberOfVertexs];
		Arrays.fill(rCore, -1);

		reachListColumnIndex = new int[numberOfVertexs];

		reachLevel = new int[numberOfVertexs];

		reachableSource = new int[numberOfVertexs];
		Arrays.fill(reachableSource, -1);
		reachableList = new int[numberOfVertexs][numberOfVertexs];

		encryptVertex();

		for (Edge edge : edgeList) {
			pushMapV(adjList, edge.getStartNode(), edge.getEndNode());
		}

		for (String vertex : setV) {
			visited.clear();
			int n = countChildNode(vStringToInt.get(vertex), vStringToInt.get(vertex));
			reachLevel[vStringToInt.get(vertex)] = n;

			vertexQueue.add(new Vertex(vertex, n));
		}
	}

	// write result to output.txt
	public void writeTextFile() throws Exception {

		Path path = Paths.get(OUTPUT);
		List<String> lines = new ArrayList<>();

		Map<String, Integer> rCoreResult = new HashMap<>();
		for (int i = 0; i < rCore.length; i++) {
			rCoreResult.put(vStringArray[i], rCore[i]);
		}

		// sort map by value
		Map<String, Integer> sortedMap = MapComparator.sortByValue(rCoreResult);
		lines.add("Node\tRCore");
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			lines.add(String.format("%s\t%d", entry.getKey(), entry.getValue()));
		}
		Files.write(path, lines);
	}

	// push value to map
	private void pushMapV(Map<Integer, ArrayList<Integer>> adjList, String start, String end) {
		if (!adjList.containsKey(vStringToInt.get(start))) {
			adjList.put(vStringToInt.get(start), new ArrayList<>());
		}
		adjList.get(vStringToInt.get(start)).add(vStringToInt.get(end));
	}

	private void addReachableVertex(int[] reachableSource, int[][] reachableList, int start, int end) {
		int containIndex = isContainSource(reachableSource, start);
		if (containIndex == -1) {
			reachableSource[reachListIndex] = start;

			reachableList[reachableSource[reachListIndex]][reachListColumnIndex[reachListIndex]] = end;
			reachListColumnIndex[reachListIndex]++;
			reachListIndex++;
		} else {
			reachableList[reachableSource[containIndex]][reachListColumnIndex[containIndex]] = end;
			reachListColumnIndex[containIndex]++;
		}
	}

	private int isContainSource(int[] reachableSource, int start) {
		for (int ro = 0; ro < reachableSource.length; ro++) {
			if (reachableSource[ro] == start) {
				return ro;
			}
		}
		return -1;
	}

	private boolean containReachValue(int row, int value) {
		for (int c = 0; c < reachableList[row].length; c++) {
			if (reachableList[row][c] == value) {
				return true;
			}
		}
		return false;
	}

	private int countChildNode(int node, int source) {
		int count = 0;
		visited.add(node);
		if (adjList.get(node) != null) {
			for (Integer vertex : adjList.get(node)) {
				if (!visited.contains(vertex)) {
					if (adjList.get(vertex) != null && adjList.get(vertex).size() > 0) {
						count = count + countChildNode(vertex, source);
					}
					count = count + 1;
					visited.add(vertex);
					addReachableVertex(reachableSource, reachableList, source, vertex);
				}
			}
		}
		return count;
	}

	// public int countChildNodeSe(String node) {
	// Stack<String> s = new Stack<>();
	//
	// int count = 0;
	//
	// s.push(node);
	//
	// while (!s.isEmpty()) {
	// String current = s.pop();
	//
	// if (visited.contains(current)) {
	// continue;
	// }
	//
	// visited.add(current);
	//
	// if (adjList.get(current) != null) {
	// for (String vertex : adjList.get(current)) {
	// s.push(vertex);
	// if (!visited.contains(vertex)) {
	// count = count + 1;
	// pushMapS(reachableList, node, vertex);
	// }
	// }
	// }
	// }
	//
	// return count;
	// }

	// compute
	public void compute() {
		int r = 0;
		// BFS traverse
		while (!vertexQueue.isEmpty()) {
			Vertex current = vertexQueue.poll();
			String currentVertex = current.getVertex();
			if (reachLevel[vStringToInt.get(currentVertex)] < current.getDegree()) {
				continue;
			}

			r = Math.max(r, reachLevel[vStringToInt.get(currentVertex)]);

			rCore[vStringToInt.get(currentVertex)] = r;
			// System.out.println(currentVertex + ": " + r);

			// sequentially
			// if (adjList.get(vStringToInt.get(currentVertex)) != null
			// && reachLevel[vStringToInt.get(currentVertex)] > 0) {
			//
			// for (Integer vertex :
			// adjList.get(vStringToInt.get(currentVertex))) {
			// if (rCore[vertex] == -1) {
			// reachLevel[vertex] = reachLevel[vertex] - 1;
			//
			// for (int source : reachableSource) {
			// if (source != -1 && containReachValue(source, vertex)) {
			// reachLevel[source] = reachLevel[source] - 1;
			// }
			// }
			//
			// vertexQueue.add(new Vertex(vStringArray[vertex],
			// reachLevel[vertex]));
			// }
			// }
			// } else if (reachLevel[vStringToInt.get(currentVertex)] == 0) {
			// for (int source : reachableSource) {
			// if (source != -1 && containReachValue(source,
			// vStringToInt.get(currentVertex))) {
			// reachLevel[source] = reachLevel[source] - 1;
			//
			// vertexQueue.add(new Vertex(vStringArray[source],
			// reachLevel[source]));
			// }
			// }
			// }

			// GPU
			final Range range;
			final RCoreKernel rCoreKernel;
			int[] result;
			if (adjList.get(vStringToInt.get(currentVertex)) != null
					&& reachLevel[vStringToInt.get(currentVertex)] > 0) {

				int adjListV[] = convertIntegers(adjList.get(vStringToInt.get(currentVertex)));
				range = Range.create(adjListV.length);
				rCoreKernel = new RCoreKernel(rCore, adjListV, reachLevel, reachableSource, reachableList, 1);

				rCoreKernel.execute(range);
				reachLevel = rCoreKernel.getReachability();

				result = rCoreKernel.getResult();

				rCoreKernel.dispose();

				Arrays.stream(result).forEach(x -> {
					vertexQueue.add(new Vertex(vStringArray[x], reachLevel[x]));
				});

			} else if (reachLevel[vStringToInt.get(currentVertex)] == 0) {

				range = Range.create(reachableSource.length);
				rCoreKernel = new RCoreKernel(vStringToInt.get(currentVertex), reachLevel, reachableSource,
						reachableList, 2);

				rCoreKernel.execute(range);
				reachLevel = rCoreKernel.getReachability();

				result = rCoreKernel.getResult();

				rCoreKernel.dispose();

				Arrays.stream(result).forEach(x -> {
					vertexQueue.add(new Vertex(vStringArray[x], reachLevel[x]));
				});
			}

		}

		System.out.println("R-Core: " + r);
	}

	private int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}

	private void encryptVertex() {
		int id = 0;
		int length = setV.size();
		vStringArray = new String[length];
		for (String s : setV) {
			vStringToInt.put(s, id);
			vStringArray[id] = s;
			id++;
		}
	}

	public void writeXLSFile(Map<String, Integer> result) throws IOException {

		// name of excel file
		String excelFileName = "result.xls";

		// name of sheet
		String sheetName = "Sheet1";

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName);
		HSSFRow row;
		HSSFCell cell;

		// header
		row = sheet.createRow(0);
		cell = row.createCell(0);
		cell.setCellValue("Node");
		cell = row.createCell(1);
		cell.setCellValue("Rank");

		int index = 1;
		for (Map.Entry<String, Integer> entry : result.entrySet()) {
			row = sheet.createRow(index++);

			cell = row.createCell(0);
			cell.setCellValue(String.format("%s", entry.getKey()));

			cell = row.createCell(1);
			cell.setCellValue(String.format("%d", entry.getValue()));
		}

		FileOutputStream fileOut = new FileOutputStream(excelFileName);

		// write this workbook to an Outputstream.
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
	}
}
