package ro.mve.systrade.rest;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class StrategyResult {
	String results;

	public static StrategyResult of(String results) {
		return new StrategyResult(results);
	}
}