package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public long sum(TransactionType transactionType, long amount) {
        return transactionType == TransactionType.USE ? point-amount : point + amount;
    }
}
