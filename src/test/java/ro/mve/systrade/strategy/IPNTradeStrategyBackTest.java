package ro.mve.systrade.strategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ro.mve.systrade.SysTradeApplication;
import ro.mve.systrade.strategy.model.SecurityDataSource;
import ro.mve.systrade.strategy.model.TradeRuleSignal;
import ro.mve.systrade.strategy.model.TradeStrategyReport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SysTradeApplication.class)
public class IPNTradeStrategyBackTest {

	static final String SP500 = "IPN";
	static final String CSV = "/data/IPN.csv";

	@Test
	public void testBuyAndHoldStrategy() {
		for(int year = 2019; year >= 2000; year--) {
			SecurityDataSource stockDs = SecurityDataSource.builder().securitySymbol(SP500).dataFile(CSV)
					.yearStart(year).yearEnd(2020).build();

			BuyAndSellStrategy strategy = BuyAndSellStrategy.builder().securityDataSource(stockDs).daysSamplingWindow(20)
					.build().execute();

			TradeStrategyReport report = new TradeStrategyReport(strategy);
			report.setPrintTradeLog(true).print();
		}
	}

	@Test
	public void testSma() {
		for( int sma : Arrays.asList(200, 120, 50)) {
			for (int year = 2019; year >= 2000; year--) {
				SecurityDataSource stockDs = SecurityDataSource.builder().securitySymbol(SP500).dataFile(CSV)
						.yearStart(year).yearEnd(2020).build();
				BuyAndSellStrategy strategy = BuyAndSellStrategy.builder().securityDataSource(stockDs).daysSamplingWindow(20)
						.buyRule(TradeRuleSignal.findBy("SMA"+sma)).build().execute();
				TradeStrategyReport report = new TradeStrategyReport(strategy);
			}
			System.out.println();
		}
	}
}

