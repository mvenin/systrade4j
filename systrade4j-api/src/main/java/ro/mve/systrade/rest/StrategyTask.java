package ro.mve.systrade.rest;

import lombok.Data;

@Data
public class StrategyTask {
	private double budget;
	private int yearStart;
	private int yearEnd;
	private String stock;
	private String bond;
	private String buyStockRule;
	private String sellStockRule;
	private String buyBondsRule;
	private String sellBondsRule;
	private boolean balanceRule;
	private boolean showTransactions;
}
