package com.binary_studio.dependency_detector;

import java.util.*;
import java.util.stream.Collectors;

public final class DependencyDetector {

	private static Map<String, LinkedList<String>> adj;

	private DependencyDetector() {
	}

	public static boolean canBuild(DependencyList libraries) {
		createAdj(libraries.libraries, libraries.dependencies);

		return !isCyclic(libraries.libraries);
	}

	private static void createAdj(List<String> libraries, List<String[]> dependencies) {
		adj = new HashMap<>();
		for (String lib : libraries) {
			adj.put(lib, computeChildren(lib, dependencies));
		}
	}

	private static LinkedList<String> computeChildren(String lib, List<String[]> dependencies) {
		return dependencies.stream().filter(dependency -> lib.equals(dependency[0]))
				.map(dependency -> dependency[1])
				.collect(Collectors.toCollection(LinkedList::new));
	}

	private static boolean isCyclic(List<String> libraries) {

		List<String> visited = new ArrayList<>(libraries.size());
		List<String> recursionStack = new ArrayList<>(libraries.size());

		for (String lib: libraries) {
			if (isCyclicUtil(lib, visited, recursionStack)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isCyclicUtil(String lib, List<String> visited, List<String> recursionStack) {
		if (recursionStack.contains(lib)) {
			return true;
		}

		if (visited.contains(lib)) {
			return false;
		}

		recursionStack.add(lib);
		visited.add(lib);

		List<String> children = adj.get(lib);

		for (String childLib: children) {
			if (isCyclicUtil(childLib, visited, recursionStack)) {
				return true;
			}
		}

		recursionStack.remove(lib);

		return false;
	}


}
