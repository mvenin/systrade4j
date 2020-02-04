package ro.mve.systrade.strategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ro.mve.systrade.SysTradeApplication;
import ro.mve.systrade.strategy.model.*;
import tech.tablesaw.api.Row;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static ro.mve.systrade.strategy.TradeStrategy.PRICE_COLUMN;
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
		for (int year = 2004; year >= 2004; year--) {
			SecurityDataSource stockDs = SecurityDataSource.builder().securityType(STOCK).securitySymbol(SP500).dataFile(CSV)
					.yearStart(year).yearEnd(2020).build();

			SecurityDataSource bondDs = SecurityDataSource.builder().securityType(SecurityType.BOND).securitySymbol(BOND).dataFile(BOND_CSV)
					.yearStart(year).yearEnd(2020).build();

			BiConsumer<? super TradeStrategy,? super GemStrategy.RowPair> alg = (t, p) -> {
				GemStrategy g = (GemStrategy)t;
				Row stk = p.stock;
				Row bnd = p.bond;
				Predicate<Row> buyStkRule =  (r) -> r.getDouble(PRICE_COLUMN) > r.getDouble("SMA120");
				Predicate<Row> buyBndRule =  (r)->(r.getDouble("SMA120") - r.getDouble(PRICE_COLUMN) )/r.getDouble("SMA120")
						<= 0.2;
				if ( buyStkRule.test(stk)) {
					g.sellBonds(bnd);
					g.buyStocks(stk);
				} else if(buyBndRule.test(stk)){
					g.sellStocks(stk);
					g.buyBonds(bnd);
				}
			};

			GemStrategy gemStrategy = GemStrategy.builder().securityDataSource(stockDs).securityDataSource(bondDs).daysSamplingWindow(20)
					.multipleDataSetsAlgorithm(alg).build();
			gemStrategy.getCashRegister().applyCommand(LocalDate.now(), TradeCommandType.DEPOSIT, 10_000);
			gemStrategy.execute();

			TradeStrategyReport report = new TradeStrategyReport(gemStrategy, gemStrategy.getSecurityDataSources());
			report.setPrintTradeLog(true).print();
			System.out.println("----------------------");
		}
	}
}