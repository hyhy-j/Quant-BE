package com.example.quantserver.investment.enums;

public enum ProfileType {
    AGGRESSIVE, NEUTRAL, STABLE;

    public static ProfileType classify(int riskTolerance) {
        return switch (riskTolerance) {
            case 1, 2 -> STABLE;
            case 3 -> NEUTRAL;
            default -> AGGRESSIVE;
        };
    }
}