package ro.mve.systrade.strategy.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TradeCommand {
	public static final double DEF_TAX_PERCENTAGE = 0.005;

	private final LocalDate tradeDate;
	private final TradeCommandType commandType;
	private final long sharesNo;
	private final double sharePrice;
	private double tradeTaxPrice;
	private double tradeAmount;
	private SecurityType securityType;
	private String securitySymbol;

	public static TradeCommand of(LocalDate opDate, TradeCommandType cmdType, long sharesNo, Double sharePrice,
			SecurityType securityType, String securitySymbol) {
		return new TradeCommand(opDate, cmdType, sharesNo, sharePrice, securityType, securitySymbol);
	}

	public TradeCommand(LocalDate tradeDate, TradeCommandType cmd, long sharesNo, double sharePrice, SecurityType securityType, String securitySymbol) {
		this(tradeDate, cmd, sharesNo, sharePrice, tax(sharesNo, sharePrice), (sharesNo * sharePrice + tax(sharesNo, sharePrice)), securityType, securitySymbol);
	}

	public static double tax(long sharesNo, double sharePrice){
		return sharesNo * sharePrice * DEF_TAX_PERCENTAGE;
	}

    public boolean requiresCapital() {
		return commandType.decreasesCapital();
	}

	@Override public String toString() {
		return String.format("TradeCommand: " +
				 tradeDate +
				", " + commandType +
				" " + securityType +
				", " + securitySymbol +
				", sharesNo=" + sharesNo +
				", sharePrice=" + sharePrice +
				", amount= %.2f, tax= %.2f", tradeAmount, tradeTaxPrice);
	}
}