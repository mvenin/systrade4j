package ro.mve.systrade.rest;

import lombok.Data;

@Data
public class StrategyTask {
	double budget;
	int yearStart;
	int yearEnd;
	String stock;
	String bond;
	String buyStockRule;
	String sellStockRule;
	String buyBondsRule;
	String sellBondsRule;
	boolean balanceRule;
}
