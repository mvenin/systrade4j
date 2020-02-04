package ro.mve.systrade.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.mve.systrade.service.StrategyExecutor;
import ro.mve.systrade.strategy.GemStrategy;
import ro.mve.systrade.strategy.TradeStrategy;
import ro.mve.systrade.strategy.model.SecurityDataSource;
import ro.mve.systrade.strategy.model.SecurityType;
import ro.mve.systrade.strategy.model.TradeCommandType;
import ro.mve.systrade.strategy.model.TradeStrategyReport;
import tech.tablesaw.api.Row;

import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static ro.mve.systrade.strategy.TradeStrategy.PRICE_COLUMN;
import static ro.mve.systrade.strategy.model.SecurityType.STOCK;

@Log4j2
@RestController
public class StrategyRest {

	@GetMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> api() {
		return ResponseEntity.ok(LocalDate.now().toString());
	}

	@PostMapping(value = "/api/strategy-task", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<StrategyResult> runStrategy(@RequestBody StrategyTask task) {
		System.out.println(task);
		TradeStrategyReport report = StrategyExecutor.runTask(task);
		String results = report.setPrintTradeLog(false).print();
		return ResponseEntity.ok(StrategyResult.of(results));
	}

}