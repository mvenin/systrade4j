package ro.mve.systrade.strategy.model;

import tech.tablesaw.api.Row;

import java.util.Arrays;
import java.util.function.Predicate;

import static ro.mve.systrade.strategy.TradeStrategy.PRICE_COLUMN;

public enum TradeRuleSignal {
		NEVER((r) -> false),
		SMA200((r) -> r.getDouble(PRICE_COLUMN) > r.getDouble("SMA200")),
		SMA120((r) -> r.getDouble(PRICE_COLUMN) > r.getDouble("SMA120")),
		SMA50((r) -> r.getDouble(PRICE_COLUMN) > r.getDouble("SMA50"))
		;
		private final Predicate<Row> rule;

		TradeRuleSignal(Predicate<Row> rule) {
			this.rule = rule;
		}

		public boolean test(Row r){
			return rule.test(r);
		}

		public Predicate<Row> getRule() {
			return rule;
		}

		public static TradeRuleSignal findBy(String sma) {
			return Arrays.stream(TradeRuleSignal.values()).filter(t->t.name().equalsIgnoreCase(sma)).findFirst().orElse(null);
		}
	}