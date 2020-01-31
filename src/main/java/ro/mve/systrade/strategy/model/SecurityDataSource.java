package ro.mve.systrade.strategy.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.time.LocalDate;

import static ro.mve.systrade.strategy.TradeStrategy.PRICE_COLUMN;

@Getter
public class SecurityDataSource {
    public static final String DATE_COLUMN = "Date";
    private final String securitySymbol;
    private final SecurityType securityType;
    private final String dataFile;
    private final int yearStart;
    private final int yearEnd;
    private final Table data;

    @Builder
    public SecurityDataSource(String securitySymbol, SecurityType securityType, String dataFile, int yearStart, int yearEnd) {
        this.securitySymbol = securitySymbol;
        this.securityType = securityType;
        this.dataFile = dataFile;
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
        try {
            Table t = Table.read().csv(SecurityDataSource.class.getResource(dataFile));
            data = t.where(t.dateColumn(DATE_COLUMN).year().isBetweenInclusive(yearStart, yearEnd));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDate getLastShareDate() {
        return getData().dateColumn(DATE_COLUMN).asList().get(
                getData().rowCount() - 1);
    }

    public double getLastSharePrice() {
        return getData().numberColumn(PRICE_COLUMN).asList().get(
                getData().rowCount() - 1).doubleValue();
    }

}
