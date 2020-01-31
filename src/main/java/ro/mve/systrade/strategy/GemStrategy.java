package ro.mve.systrade.strategy;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ro.mve.systrade.strategy.model.*;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuperBuilder
@Getter
public class GemStrategy extends TradeStrategy {

    @Singular("securityDataSource")
    private List<SecurityDataSource> securityDataSources;

    private TradeRuleSignal buyRule;
	private TradeRuleSignal sellRule;
	protected BiConsumer<? super TradeStrategy,? super RowPair> multipleDataSetsAlgorithm;

	@Data
	@Builder
	public static class RowPair {
		Row stock;
		Row bond;
	}

	private SecurityDataSource stockDs() {
        return securityDataSources.stream().filter(t -> t.getSecurityType() == SecurityType.STOCK).findFirst().get();
    }

    private SecurityDataSource bondDs() {
        return securityDataSources.stream().filter(t -> t.getSecurityType() == SecurityType.BOND).findFirst().get();
    }

    public SecurityDataSource getSecurityDataSource(){
    	return stockDs();
	}

	protected GemStrategy withAlgorithm(@NonNull BiConsumer<? super TradeStrategy,? super RowPair> alg) {
		this.multipleDataSetsAlgorithm = alg;
		return this;
	}

	protected BiConsumer<? super TradeStrategy,? super RowPair> algorithm() {
		return multipleDataSetsAlgorithm != null? multipleDataSetsAlgorithm : (multipleDataSetsAlgorithm = (t,p) -> {
			Row stk = p.stock;
			Row bnd = p.bond;
			boolean buyStockSignal = getBuyRule().test(stk);
			if (buyStockSignal) {
				sellBonds(bnd);
				buyStocks(stk);
			} else {
				sellStocks(stk);
				buyBonds(bnd);
			}
		});

	}

    private void buyShares(Row r, SecurityDataSource ds) {
        LocalDate date = r.getDate(DATE_COLUMN);
        this.getCashRegister().ensureAvailableCash(date);
        long sharesNo = getTradeRegister().getSharesNoAtPrice(r.getDouble(PRICE_COLUMN));
        getTradeRegister().applyCommand(TradeCommand.of(date, TradeCommandType.BUY_SHARES, sharesNo, r.getDouble(PRICE_COLUMN), ds.getSecurityType(), ds.getSecuritySymbol()));
    }

    private void sellShares(Row r, SecurityDataSource ds) {
        TradeRegister tr = this.getTradeRegister();
        long availableShares = tr.getAvailableShares(ds.getSecurityType());
        if (availableShares > 0) {
            LocalDate date = r.getDate(DATE_COLUMN);
            tr.applyCommand(TradeCommand.of(date, TradeCommandType.SELL_SHARES, availableShares, r.getDouble(PRICE_COLUMN), ds.getSecurityType(), ds.getSecuritySymbol()));
        }
    }

	public void sellStocks(Row r) {
        sellShares(r, stockDs());
    }

	public void buyStocks(Row r) {
        buyShares(r, stockDs());
    }

	public void sellBonds(Row r) {
        sellShares(r, bondDs());
    }

	public void buyBonds(Row r) {
        buyShares(r, bondDs());
    }

    public GemStrategy execute() {
        Table stockData = stockDs().getData();
        Table bondData = bondDs().getData();
        prepareDataForRules(stockData, bondData);
        if (stockData.rowCount() != bondData.rowCount()) {
            System.out.println("Data sets have different sizes, stockData.size=" + stockData.rowCount() + ", bondData.size=" + bondData.rowCount());
        }
		BiConsumer<? super TradeStrategy,? super RowPair> c = algorithm();
        int skip = getDaysSamplingWindow();
        int size = stockData.rowCount();
        int limit = size / skip + Math.min(size % skip, 1);
        Stream.iterate(0, i -> i + skip)
                .limit(limit)
                .map(x -> RowPair.builder().stock(stockData.row(x)).bond(bondData.row(x)).build())
                .collect(Collectors.toList())
                .forEach( (t)-> {c.accept(this, t);});
        return this;
    }

    protected void prepareDataForRules(Table... dataSets) {
        Arrays.stream(dataSets).forEach(data ->
				Arrays.asList(200, 120, 50).forEach(rollingWindow ->
						data.addColumns(data.numberColumn(PRICE_COLUMN).rolling(rollingWindow).mean().setName("SMA" + rollingWindow))));
    }

	public Double getUnrealizedProfit() {
		if (getTradeRegister().getAllAvailableShares() > 0) {
			return getSecurityDataSources().stream().map(t -> getTradeRegister().getAvailableShares(t.getSecurityType())
					* t.getLastSharePrice()).reduce((d1, d2) -> d1 + d2).get();
		}
		return 0D;
	}

	public double getAverageReturn() {
		double availableCash = getCashRegister().getAvailableCash();
		double depositedCash = getCashRegister().getDepositedCash();
		double unrealizedProfit = getUnrealizedProfit();
		return ( (unrealizedProfit > 0) ? ((unrealizedProfit + availableCash) - depositedCash) / depositedCash
				: (availableCash - depositedCash) / depositedCash) * 100;
	}

}