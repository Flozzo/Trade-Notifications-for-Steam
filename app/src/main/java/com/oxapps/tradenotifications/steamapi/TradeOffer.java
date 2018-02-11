package com.oxapps.tradenotifications.steamapi;

/**
 * Created by Flynn on 25/03/2017.
 */

public class TradeOffer {
    public final static int STATE_ACTIVE = 2;

    private long timeCreated;
    private int state;

    public TradeOffer(long timeCreated, int state) {
        this.timeCreated = timeCreated;
        this.state = state;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public int getState() {
        return state;
    }
}
