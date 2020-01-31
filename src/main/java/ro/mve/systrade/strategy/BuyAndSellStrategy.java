package ro.mve.systrade.strategy;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ro.mve.systrade.strategy.model.*;
import tech.tablesaw.api.Row;

import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static ro.mve.systrade.strategy.model.TradeCommandType.BUY_SHARES;
import static ro.mve.systrade.strategy.model.TradeCommandType.SELL_SHARES;

@SuperBuilder
@Data
public class BuyAndSellStrategy extends TradeStrategy {
	private TradeRuleSignal buyRule;
	private TradeRuleSignal sellRule;
	private BiConsumer<? super TradeStrategy,? super Row> alg;

	public String getStrategyDescription(){
		SecurityDataSource ds = getSecurityDataSource();
		String interval = " (" + ds.getSecuritySymbol() + ") (" + ds.getYearStart() + "-" + ds.getYearEnd() + ")";
		if (buyRule != null) {
			String smaCol = buyRule.name();
			return smaCol + interval;
		} else {
			return getClass().getSimpleName() + interval;
		}
	}

	protected BiConsumer<? super TradeStrategy,? super Row> algorithm() {
		SecurityType securityType = getSecurityDataSource().getSecurityType();
		String securitySymbol = getSecurityDataSource().getSecuritySymbol();

		TradeRuleSignal buyRule = getBuyRule();
		TradeRuleSignal sellRule = getSellRule();
		CashRegister cashRegister = this.getCashRegister();
		TradeRegister tradeRegister = this.getTradeRegister();

		return alg != null ? alg : (alg = (t, r) -> {
			LocalDate opDate = r.getDate(DATE_COLUMN);
			Double sharePrice = r.getDouble(PRICE_COLUMN);
			boolean buySignal = buyRule != null && buyRule.test(r);
			boolean sellSignal = sellRule != null && sellRule.test(r);
			if (buySignal) {
				cashRegister.ensureAvailableCash(opDate);
				long sharesNo = tradeRegister.getSharesNoAtPrice(sharePrice);
				tradeRegister.applyCommand(TradeCommand.of(opDate, BUY_SHARES, sharesNo, sharePrice,
						securityType, securitySymbol));
			} else if (sellSignal) {
				long sharesNo = tradeRegister.getAvailableShares(securityType);
				if (sharesNo > 0) {
					tradeRegister.applyCommand(TradeCommand.of(opDate, SELL_SHARES, sharesNo, sharePrice, securityType, securitySymbol));
				}
			}
		});
	}

}
