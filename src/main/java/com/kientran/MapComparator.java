package com.kientran;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapComparator {
	// sort map by value (descending order)
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, (Map.Entry<K, V> o1, Map.Entry<K, V> o2) -> -(o1.getValue()).compareTo(o2.getValue()));

		Map<K, V> result = new LinkedHashMap<>();
		list.forEach((entry) -> {
			result.put(entry.getKey(), entry.getValue());
		});
		return result;
	}
}
