package com.binary_studio.academy_coin;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AcademyCoin {

	private AcademyCoin() {
	}

	public static int maxProfit(Stream<Integer> prices) {

		int profit = 0;

		Integer[] pricesArray = prices.collect(Collectors.toList()).toArray(Integer[]::new);

		int j = 0;
		for (int i = 1; i < pricesArray.length; i++) {
			if (pricesArray[i - 1] > pricesArray[i]) {
				j = i;
			}
			if (pricesArray[i - 1] <= pricesArray[i]
					&& (i + 1 == pricesArray.length || pricesArray[i] > pricesArray[i + 1])) {
				profit += pricesArray[i] - pricesArray[j];
			}
		}

		return profit;
	}

}
