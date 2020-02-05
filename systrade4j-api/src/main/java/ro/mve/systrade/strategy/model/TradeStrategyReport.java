package ro.mve.systrade.strategy.model;

import lombok.Data;
import lombok.NonNull;
import ro.mve.systrade.strategy.TradeStrategy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

    public String print() {
		ByteArrayOutputStream os=new ByteArrayOutputStream();
    	PrintStream out = new PrintStream(os);

        tr.getTradeCommands().stream().filter(t -> printTradeLog).forEach(out::println);

        out.println();
        out.println(format("Deposited Cash =  %.2f", cashRegister.getDepositedCash()));
        out.println(format("Available Cash = %.2f", cashRegister.getAvailableCash()));
        dataSources.forEach(
                ds -> {
                    long bought = tr.getTradeCommands().stream().filter(t -> t.getCommandType() == TradeCommandType.BUY && t.getSecurityType() == ds.getSecurityType()).count();
                    long sold = tr.getTradeCommands().stream().filter(t -> t.getCommandType() == TradeCommandType.SELL && t.getSecurityType() == ds.getSecurityType()).count();
                    out.println(format("AvailableShares = %s (%s) bought %d time(s), sold %d time(s), lastSharePrice= %.2f at date = %s",
                            tr.getAvailableShares(ds.getSecuritySymbol()), ds.getSecuritySymbol(), bought, sold, ds.getLastSharePrice(), ds.getLastShareDate()))
                    ;
                }
        );

        out.println(format("Unrealized Profit = %.2f", tradeStrategy.getUnrealizedProfit()));
        out.println(format("Average Return = %.2f%%", tradeStrategy.getAverageReturn()));
        return os.toString();
    }

    public String printAverageReturn() {
        String s = format("Average Return = %.2f%%", tradeStrategy.getAverageReturn());
        System.out.println(s);
        return s;
    }
}
