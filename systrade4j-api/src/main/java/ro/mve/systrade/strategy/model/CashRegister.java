package ro.mve.systrade.strategy.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static ro.mve.systrade.strategy.model.TradeCommandType.DEPOSIT;

@Data
public class CashRegister {
	static final Currency EUR = Currency.getInstance("EUR");
	static final Currency USD = Currency.getInstance("USD");
	static final int DEF_MIN_CASH_LEVEL = 1_000;

	private List<CashRegisterCommand> cashOperations = new ArrayList<>();

	public void applyCommand(LocalDate opDate, TradeCommandType commandType, double amount) {
		cashOperations.add(new CashRegisterCommand(opDate, commandType, amount, EUR));
	}

	public double getAvailableCash() {
		Optional<Double> amount = cashOperations.stream().map(t -> (t.getCommandType().increasesCapital() ? 1 : -1)
				* t.getAmount()).reduce(Double::sum);
		return amount.orElse(0D);
	}

	public double getDepositedCash() {
		Optional<Double> amount = cashOperations.stream().filter(t -> t.getCommandType() == DEPOSIT)
				.map(CashRegisterCommand::getAmount)
				.reduce(Double::sum);
		return amount.orElse(0D);
	}

}