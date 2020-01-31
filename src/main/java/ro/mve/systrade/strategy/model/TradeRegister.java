package ro.mve.systrade.strategy.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ro.mve.systrade.strategy.model.TradeCommand.DEF_TAX_PERCENTAGE;

@Data
public class TradeRegister {
	private List<TradeCommand> tradeCommands = new ArrayList<>();
	private final CashRegister cashRegister;

	public void applyCommand(TradeCommand trade) {
		if(trade.requiresCapital() && cashRegister.getAvailableCash() < trade.getTradeAmount() ){
			throw new RuntimeException("Not enough funds for trade "+ trade+", availableCash="+this.cashRegister.getAvailableCash());
		}
		this.cashRegister.applyCommand(trade.getTradeDate() ,trade.getCommandType(), trade.getTradeAmount());
		tradeCommands.add(trade);
	}

	public long getAvailableShares(String symbol/*SecurityType securityType*/) {
		Optional<Long> sharesNo = tradeCommands.stream().filter(t-> symbol == null || t.getSecuritySymbol().equalsIgnoreCase(symbol) )
				.map(t -> (t.requiresCapital() ? 1 : -1) * t.getSharesNo()).reduce(Long::sum);
		return sharesNo.orElse(0L);
	}

	public long getSharesNoAtPrice(double sharePrice){
		double cash = ((cash = this.cashRegister.getAvailableCash() )<0)? 0 : cash;
		return  (long)( cash / (sharePrice * (1 + DEF_TAX_PERCENTAGE)));
	}

	public long getAllAvailableShares() {
		return getAvailableShares(null);
	}
}