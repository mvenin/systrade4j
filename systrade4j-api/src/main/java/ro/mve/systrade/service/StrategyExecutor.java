package ro.mve.systrade.service;

import ro.mve.systrade.rest.StrategyTask;
import ro.mve.systrade.strategy.GemStrategy;
import ro.mve.systrade.strategy.TradeStrategy;
import ro.mve.systrade.strategy.model.SecurityDataSource;
import ro.mve.systrade.strategy.model.SecurityType;
import ro.mve.systrade.strategy.model.TradeCommandType;
import ro.mve.systrade.strategy.model.TradeStrategyReport;
import tech.tablesaw.api.Row;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static ro.mve.systrade.strategy.TradeStrategy.PRICE_COLUMN;
import static ro.mve.systrade.strategy.model.SecurityType.STOCK;

/**
 * @author veninma
 */
public class StrategyExecutor {

	public static TradeStrategyReport runTask(StrategyTask task) {
		final String stockCsvpath = String.format("/data/%s.csv",task.getStock());
		final String bondCsvPath = String.format("/data/%s.csv",task.getBond());

		final SecurityDataSource stockDs = SecurityDataSource.builder().securityType(STOCK).securitySymbol(task.getStock())
				.dataFile(stockCsvpath)
				.yearStart(task.getYearStart()).yearEnd(task.getYearEnd()).build();

		final SecurityDataSource bondDs = SecurityDataSource.builder().securityType(SecurityType.BOND).securitySymbol(task.getBond())
				.dataFile(bondCsvPath)
				.yearStart(task.getYearStart()).yearEnd(task.getYearEnd()).build();
		final String sma = task.getBuyStockRule();

		final double dd = Integer.parseInt(task.getSellStockRule()) / 100.0;
		BiConsumer<? super TradeStrategy, ? super GemStrategy.RowPair> alg = (t, p) -> {
			GemStrategy g = (GemStrategy) t;
			Row stk = p.getStock();
			Row bnd = p.getBond();
			Predicate<Row> buyStkRule = (r) -> r.getDouble(PRICE_COLUMN) > r.getDouble(sma);
			Predicate<Row> buyBndRule = (r) -> (r.getDouble(sma) - r.getDouble(PRICE_COLUMN)) / r.getDouble(sma) >= dd;
			if (buyStkRule.test(stk)) {
				g.sellBonds(bnd);
				g.buyStocks(stk);
			} else if (buyBndRule.test(stk)) {
				g.sellStocks(stk);
				g.buyBonds(bnd);
			}
		};

		GemStrategy gemStrategy = GemStrategy.builder().securityDataSource(stockDs).securityDataSource(bondDs).daysSamplingWindow(20)
				.multipleDataSetsAlgorithm(alg).build();
		gemStrategy.getCashRegister().applyCommand(LocalDate.now(), TradeCommandType.DEPOSIT, task.getBudget());
		gemStrategy.execute();

		TradeStrategyReport report = new TradeStrategyReport(gemStrategy, gemStrategy.getSecurityDataSources());
		return report;
	}
}
