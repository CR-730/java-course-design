package edu.gpnu.bigdata.dto;

public record FunnelStats(
        long viewUsers,
        long cartUsers,
        long orderUsers,
        long payUsers,
        double viewToCartRate,
        double cartToOrderRate,
        double orderToPayRate
) {
}

