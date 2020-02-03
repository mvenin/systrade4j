package ro.mve.systrade.strategy;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;
import ro.mve.systrade.strategy.model.*;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ro.mve.systrade.strategy.model.TradeCommandType.BUY;

@SuperBuilder
@Data
@Log4j2
public class TradeStrategy {
    public static final String DATE_COLUMN = "Date";
    public static final String PRICE_COLUMN = "Close";

    private final String strategyName = getClass().getSimpleName();
    private final CashRegister cashRegister = new CashRegister();
    private final TradeRegister tradeRegister = new TradeRegister(cashRegister);

    private BiConsumer<? super TradeStrategy,? super Row> algorithm;
    private SecurityDataSource securityDataSource;
    private int daysSamplingWindow;

    public String getStrategyDescription(){
        SecurityDataSource ds = getSecurityDataSource();
        String interval = " (" + ds.getSecuritySymbol() + ") (" + ds.getYearStart() + "-" + ds.getYearEnd() + ")";
        return getClass().getSimpleName() + interval;
    }

	protected <T extends Row> BiConsumer<? super TradeStrategy,? super T> algorithm() {
		SecurityType securityType = getSecurityDataSource().getSecurityType();
		String securitySymbol = getSecurityDataSource().getSecuritySymbol();

		return algorithm != null? algorithm : (algorithm = (t, r) -> {
			LocalDate opDate = r.getDate(DATE_COLUMN);
			double sharePrice = r.getDouble(PRICE_COLUMN);
			long sharesNo = getTradeRegister().getSharesNoAtPrice(sharePrice);
			getTradeRegister().applyCommand(new TradeCommand(opDate, BUY, sharesNo, sharePrice, securityType, securitySymbol));
		});

	}

    public <T extends TradeStrategy> T execute() {
        Table data = getSecurityDataSource().getData();
        prepareDataForRules(data);
		BiConsumer<? super TradeStrategy,? super Row> c = algorithm();
        int skip = getDaysSamplingWindow();
        int size = data.rowCount();
        int limit = size / skip + Math.min(size % skip, 1);
        Stream.iterate(0, i -> i + skip)
                .limit(limit)
                .map(data::row)
                .collect(Collectors.toList())
				.forEach( (t)-> c.accept(this, t));
        return (T) this;
    }

    protected void prepareDataForRules(Table data){
        for (int rollingWindow : Arrays.asList(200, 120, 50)) {
            data.addColumns(data.numberColumn(PRICE_COLUMN).rolling(rollingWindow).mean().setName("SMA" + rollingWindow));
        }
    }

    public double getAverageReturn() {
        long availableShares = tradeRegister.getAvailableShares(getSecurityDataSource().getSecuritySymbol());
        double unrealizedProfit = (availableShares > 0) ? availableShares * getSecurityDataSource().getLastSharePrice() : 0D;
        double avgRet;
        if (unrealizedProfit > 0) {
            avgRet = ((unrealizedProfit + cashRegister.getAvailableCash()) - cashRegister.getDepositedCash()) / cashRegister.getDepositedCash();
        } else {
            avgRet = (cashRegister.getAvailableCash() - cashRegister.getDepositedCash()) / cashRegister.getDepositedCash();
        }
        return avgRet * 100;
    }

	public Double getUnrealizedProfit() {
		if (tradeRegister.getAllAvailableShares() > 0) {
			return tradeRegister.getAvailableShares(getSecurityDataSource().getSecuritySymbol())
					* getSecurityDataSource().getLastSharePrice();
		}
		return 0D;
	}
}
