package ro.mve.systrade.strategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ro.mve.systrade.SysTradeApplication;
import ro.mve.systrade.strategy.model.SecurityDataSource;
import ro.mve.systrade.strategy.model.SecurityType;
import ro.mve.systrade.strategy.model.TradeRuleSignal;
import ro.mve.systrade.strategy.model.TradeStrategyReport;
import tech.tablesaw.api.Row;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static ro.mve.systrade.strategy.model.SecurityType.STOCK;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SysTradeApplication.class)
public class GEMradeStrategyBackTest {
	static final String SP500 = "IVV";
	static final String CSV = "/data/IVV.csv";
	static final String BOND = "AGG";
	static final String BOND_CSV = "/data/AGG.csv";

	@Test
	public void testSma() {
		for (int sma : Arrays.asList(200, 120, 50)) {
			for (int year = 2019; year >= 2019; year--) {
				SecurityDataSource stockDs = SecurityDataSource.builder().securityType(STOCK).securitySymbol(SP500).dataFile(CSV)
						.yearStart(year).yearEnd(2020).build();

				SecurityDataSource bondDs = SecurityDataSource.builder().securityType(SecurityType.BOND).securitySymbol(BOND).dataFile(BOND_CSV)
						.yearStart(year).yearEnd(2020).build();

				GemStrategy gemStrategy = GemStrategy.builder().securityDataSource(stockDs).securityDataSource(bondDs).daysSamplingWindow(20)
						.buyRule(TradeRuleSignal.findBy("SMA"+sma)).build().execute();
				TradeStrategyReport report = new TradeStrategyReport(gemStrategy, gemStrategy.getSecurityDataSources());
				report.setPrintTradeLog(true).print();
			}
			System.out.println();
		}
	}

	@Test
	public void testBuyStrategy() {
		for (int year = 2019; year >= 2019; year--) {
			SecurityDataSource stockDs = SecurityDataSource.builder().securityType(STOCK).securitySymbol(SP500).dataFile(CSV)
					.yearStart(year).yearEnd(2020).build();

			SecurityDataSource bondDs = SecurityDataSource.builder().securityType(SecurityType.BOND).securitySymbol(BOND).dataFile(BOND_CSV)
					.yearStart(year).yearEnd(2020).build();

			BiConsumer<? super TradeStrategy,? super GemStrategy.RowPair> alg = (t, p) -> {
				GemStrategy g = (GemStrategy)t;
				Row stk = p.stock;
				Row bnd = p.bond;
				boolean buyStockSignal = TradeRuleSignal.findBy("SMA120").test(stk);
				if (buyStockSignal) {
					//g.sellBonds(bnd);
					g.buyStocks(stk);
				} else {
					//g.sellStocks(stk);
					g.buyBonds(bnd);
				}
			};

			GemStrategy gemStrategy = GemStrategy.builder().securityDataSource(stockDs).securityDataSource(bondDs).daysSamplingWindow(20)
					.multipleDataSetsAlgorithm(alg).build().execute();

			TradeStrategyReport report = new TradeStrategyReport(gemStrategy, gemStrategy.getSecurityDataSources());
			report.setPrintTradeLog(true).print();
		}
	}
}