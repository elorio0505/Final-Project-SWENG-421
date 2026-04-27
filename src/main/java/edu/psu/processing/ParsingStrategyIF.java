package edu.psu.processing;

public interface ParsingStrategyIF {
    public void parseRaw(Object data, AbsTicketBuilder builder);
}
