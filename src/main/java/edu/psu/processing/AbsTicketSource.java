package edu.psu.processing;

public abstract class AbsTicketSource {
    protected ParsingStrategyIF strategy;
    protected AbsTicketBuilder builder;

    public AbsTicketSource(ParsingStrategyIF s) {}
    public void setStrategy(ParsingStrategyIF s) {}

    public abstract void submitTicketData(Object raw);
}
