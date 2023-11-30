package com.brinvex.util.ibkr.impl;

import com.brinvex.util.ibkr.api.model.raw.CashTransaction;
import com.brinvex.util.ibkr.api.model.raw.CorporateAction;
import com.brinvex.util.ibkr.api.model.raw.Trade;
import com.brinvex.util.ibkr.api.model.raw.TradeConfirm;

/**
 * Natural ordering of all returned IDs is consistent with natural ordering of CashTransaction+Trade+TradeConfirm.
 */
public class TranIdGenerator {

    public static String getId(CashTransaction cashTran) {
        return "%s/%s/%s/CashTran".formatted(
                cashTran.getDateTime(), cashTran.getTransactionID(), cashTran.getActionID()
        );
    }

    public static String getId(Trade trade) {
        return "%s/%s/%s/Trade".formatted(
                trade.getDateTime(), trade.getTradeID(), trade.getIbOrderID()
        );
    }

    public static String getId(TradeConfirm tradeConfirm) {
        return "%s/%s/%s/TradeConfirm".formatted(
                tradeConfirm.getDateTime(), tradeConfirm.getTradeID(), tradeConfirm.getOrderID()
        );
    }

    public static String getId(CorporateAction corpAction) {
        return "%s/%s/%s/Trade".formatted(
                corpAction.getDateTime(), corpAction.getTransactionId(), corpAction.getActionID()
        );
    }

}
