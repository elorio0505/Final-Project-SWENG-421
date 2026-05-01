package edu.psu.processing;

public abstract class AbsTicketSource {
    protected ParsingStrategyIF strategy;
    protected AbsTicketBuilder builder;

    public AbsTicketSource(ParsingStrategyIF s) {
        this.strategy = s;
    }

    public AbsTicketSource(ParsingStrategyIF s, AbsTicketBuilder builder) {
        this.strategy = s;
        this.builder = builder;
    }

    public void setStrategy(ParsingStrategyIF s) {
        this.strategy = s;
    }

    public void setBuilder(AbsTicketBuilder builder) {
        this.builder = builder;
    }

    public void submitTicketData(Object raw) {
        if (strategy == null) {
            throw new IllegalStateException("No parsing strategy configured.");
        }
        if (builder == null) {
            throw new IllegalStateException("No ticket builder configured.");
        }

        strategy.parseRaw(raw, builder);
    }
}
