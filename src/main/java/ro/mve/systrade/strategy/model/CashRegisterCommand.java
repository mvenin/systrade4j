package ro.mve.systrade.strategy.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Currency;

@Data
@AllArgsConstructor
public class CashRegisterCommand {
	private LocalDate opDate;
	private TradeCommandType commandType;
	private Double amount;
	private Currency currency;

}