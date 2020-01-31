package ro.mve.systrade.strategy.model;

public enum TradeCommandType {
	DEPOSIT, REDRAW, BUY_SHARES, SELL_SHARES;

	public boolean increasesCapital() {
		return this == DEPOSIT || this == SELL_SHARES;
	}

	public boolean decreasesCapital() {
		return this == REDRAW || this == BUY_SHARES;
	}
}