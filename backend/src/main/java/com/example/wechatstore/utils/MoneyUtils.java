package com.example.wechatstore.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static int yuanToCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .intValueExact();
    }
}
