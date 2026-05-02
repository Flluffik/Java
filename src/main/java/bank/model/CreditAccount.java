package bank.model;

import bank.exception.BankException;

public class CreditAccount extends Account {
    private final double creditLimit;
    private final double monthlyCommission;

    public CreditAccount(Client owner, double creditLimit, double monthlyCommission, double suspiciousLimit) {
        super(owner, 0, suspiciousLimit);
        this.creditLimit = creditLimit;
        this.monthlyCommission = monthlyCommission;
    }

    @Override
    public void deposit(double amount) throws BankException {
        if (amount <= 0) throw new BankException("Сумма пополнения должна быть положительной");
        balance += amount;
    }

    @Override
    public void withdraw(double amount) throws BankException {
        if (amount <= 0) throw new BankException("Сумма снятия должна быть положительной");
        checkSuspiciousLimit(amount);
        if (balance - amount < -creditLimit) {
            throw new BankException("Кредитный лимит превышен. Доступно: " + (balance + creditLimit));
        }
        balance -= amount;
    }

    @Override
    public void applyDailyInterestAndFees() {
        // No daily interest for credit accounts
    }

    @Override
    public void applyMonthlyPayouts() {
        if (balance < 0) {
            balance -= monthlyCommission;
        }
    }

    @Override
    public String getType() { return "CreditAccount"; }

    public double getCreditLimit() { return creditLimit; }
    public double getMonthlyCommission() { return monthlyCommission; }
}
