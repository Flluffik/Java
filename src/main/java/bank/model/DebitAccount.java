package bank.model;

import bank.exception.BankException;

public class DebitAccount extends Account {
    private final double annualInterestRate;
    private double accruedInterest = 0;

    public DebitAccount(Client owner, double initialBalance, double annualInterestRate, double suspiciousLimit) {
        super(owner, initialBalance, suspiciousLimit);
        this.annualInterestRate = annualInterestRate;
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
        if (balance - amount < 0) throw new BankException("Недостаточно средств на дебетовом счёте");
        balance -= amount;
    }

    @Override
    public void applyDailyInterestAndFees() {
        double dailyRate = annualInterestRate / 365.0 / 100.0;
        accruedInterest += balance * dailyRate;
    }

    @Override
    public void applyMonthlyPayouts() {
        balance += accruedInterest;
        accruedInterest = 0;
    }

    @Override
    public String getType() { return "DebitAccount"; }

    public double getAnnualInterestRate() { return annualInterestRate; }
}
