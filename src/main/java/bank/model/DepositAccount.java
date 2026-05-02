package bank.model;

import bank.exception.BankException;

import java.time.LocalDate;

public class DepositAccount extends Account {
    private final LocalDate openDate;
    private final LocalDate endDate;
    private double accruedInterest = 0;

    public DepositAccount(Client owner, double initialBalance, double suspiciousLimit,
                          LocalDate openDate, int termDays) {
        super(owner, initialBalance, suspiciousLimit);
        this.openDate = openDate;
        this.endDate = openDate.plusDays(termDays);
    }

    private double getInterestRate() {
        if (balance < 50_000) return 3.0;
        if (balance <= 100_000) return 3.5;
        return 4.0;
    }

    @Override
    public void deposit(double amount) throws BankException {
        if (amount <= 0) throw new BankException("Сумма пополнения должна быть положительной");
        balance += amount;
    }

    @Override
    public void withdraw(double amount) throws BankException {
        throw new BankException("Нельзя снять деньги с депозита до окончания срока (" + endDate + ")");
    }

    public void withdrawAfterTerm(double amount, LocalDate currentDate) throws BankException {
        if (currentDate.isBefore(endDate)) {
            throw new BankException("Нельзя снять деньги с депозита до окончания срока (" + endDate + ")");
        }
        checkSuspiciousLimit(amount);
        if (balance - amount < 0) throw new BankException("Недостаточно средств");
        balance -= amount;
    }

    @Override
    public void applyDailyInterestAndFees() {
        double dailyRate = getInterestRate() / 365.0 / 100.0;
        accruedInterest += balance * dailyRate;
    }

    @Override
    public void applyMonthlyPayouts() {
        balance += accruedInterest;
        accruedInterest = 0;
    }

    @Override
    public String getType() { return "DepositAccount"; }

    public LocalDate getEndDate() { return endDate; }
    public boolean isTermExpired(LocalDate currentDate) { return !currentDate.isBefore(endDate); }
}
