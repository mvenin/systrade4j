package ro.mve.systrade.strategy.model;

import lombok.Data;
import lombok.NonNull;
import ro.mve.systrade.strategy.TradeStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        this.dataSources = Collections.singletonList(tradeStrategy.getSecurityDataSource());
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
        dataSources.forEach(
                ds -> {
                    long bought = tr.getTradeCommands().stream().filter(t -> t.getCommandType() == TradeCommandType.BUY && t.getSecurityType() == ds.getSecurityType()).count();
                    long sold = tr.getTradeCommands().stream().filter(t -> t.getCommandType() == TradeCommandType.SELL && t.getSecurityType() == ds.getSecurityType()).count();
                    System.out.println(format("[%s] AvailableShares = %s (%s) bought %d time(s), sold %d time(s), lastSharePrice= %.2f at date = %s",
                            strategyName, tr.getAvailableShares(ds.getSecuritySymbol()), ds.getSecuritySymbol(), bought, sold, ds.getLastSharePrice(), ds.getLastShareDate()))
                    ;
                }
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
