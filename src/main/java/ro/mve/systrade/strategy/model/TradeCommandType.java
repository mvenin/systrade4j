package ro.mve.systrade.strategy.model;

public enum TradeCommandType {
	DEPOSIT, REDRAW, BUY, SELL;

	public boolean increasesCapital() {
		return this == DEPOSIT || this == SELL;
	}

	public boolean decreasesCapital() {
		return this == REDRAW || this == BUY;
	}
}