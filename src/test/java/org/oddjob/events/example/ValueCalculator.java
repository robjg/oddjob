package org.oddjob.events.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ValueCalculator implements Runnable {

    private static final Logger logger =
            LoggerFactory.getLogger(ValueCalculator.class);

    private volatile String name;

    private volatile List<Trade> trades;

    private volatile List<Price> prices;

    private volatile double value;

    @Override
    public void run() {
        final List<Trade> trades = Optional.ofNullable(this.trades)
                .orElseThrow(() -> new IllegalArgumentException("No Trades"));

        final List<Price> prices = Optional.ofNullable(this.prices)
                .orElseThrow(() -> new IllegalArgumentException("No Prices"));

        Map<String, Double> priceLookup = new HashMap<>();

        prices.forEach(p -> priceLookup.put(p.getProduct(), p.getPrice()));

        value = trades.stream()
                .mapToDouble(t -> priceLookup.get( t.getProduct()) * t.getQuantity())
                .sum();

        logger.info("Value of {} trades is {}",
                    trades.size(),
                    value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public void setTrades(List<Trade> trades) {
        this.trades = trades;
    }

    public List<Price> getPrices() {
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }

    public double getValue() {
        return value;
    }

    public String toString() {
        return Optional.ofNullable(name).orElseGet(() -> getClass().getSimpleName());
    }
}
