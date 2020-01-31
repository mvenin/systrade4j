package ro.mve.systrade.strategy.model;

import lombok.Data;
import lombok.NonNull;
import ro.mve.systrade.strategy.TradeStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Data
public class TradeStrategyReport {
    private final TradeStrategy tradeStrategy;
    private final TradeRegister tr;
    private final CashRegister cashRegister;
    private final String strategyName;
    private List<SecurityDataSource> dataSources;
    private boolean printTradeLog;

    public TradeStrategyReport(TradeStrategy tradeStrategy, @NonNull List<SecurityDataSource> dataSources) {
        this(tradeStrategy);
        this.dataSources = dataSources;
    }

    public TradeStrategyReport(TradeStrategy tradeStrategy) {
        this.tradeStrategy = tradeStrategy;
        this.tr = tradeStrategy.getTradeRegister();
        this.cashRegister = tradeStrategy.getCashRegister();
        this.strategyName = tradeStrategy.getStrategyDescription();
        this.dataSources = Arrays.asList(tradeStrategy.getSecurityDataSource());
    }

    public TradeStrategyReport setPrintTradeLog(boolean printTradeLog) {
        this.printTradeLog = printTradeLog;
        return this;
    }

    public TradeStrategyReport print() {
        tr.getTradeCommands().stream().filter(t -> printTradeLog).forEach(System.out::println);

        String strategyName = this.tradeStrategy.getStrategyDescription();
        System.out.println(format("\n[%s] Deposited =  %.2f", strategyName, cashRegister.getDepositedCash()));
        System.out.println(format("[%s] AvailableCash = %.2f", strategyName, cashRegister.getAvailableCash()));
        dataSources.stream().forEach(
        		ds ->  System.out.println(format("[%s] AvailableShares = %s (%s) lastSharePrice= %.2f at date = %s",
						strategyName, tr.getAvailableShares(ds.getSecurityType()), ds.getSecuritySymbol(), ds.getLastSharePrice(), ds.getLastShareDate() ))
		);
        System.out.println(format("[%s] UnRealized Profit = %.2f", strategyName, tradeStrategy.getUnrealizedProfit()));
        System.out.println(format("[%s] Average Return = %.2f%%", strategyName, tradeStrategy.getAverageReturn()));
        return this;
    }

    public String printAverageReturn() {
        String s = format("[%s] Average Return = %.2f%%", this.strategyName, tradeStrategy.getAverageReturn());
        System.out.println(s);
        return s;
    }
}
