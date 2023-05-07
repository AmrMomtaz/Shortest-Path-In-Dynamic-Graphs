package com;

public final class Operation {
    private final int A;
    private final int B;
    private final OperationType operationType;

    public Operation(int a, int b, OperationType operationType) {
        this.A = a;
        this.B = b;
        this.operationType = operationType;
    }

    public int getA() {
        return this.A;
    }

    public int getB() {
        return this.B;
    }

    public OperationType getOperationType() {
        return this.operationType;
    }

    public String toString() {
        return "client.Operation{A=" + this.A + ", B=" + this.B + ", operationType=" + this.operationType + "}";
    }

    public static enum OperationType { QUERY, ADD, DELETE }
}
