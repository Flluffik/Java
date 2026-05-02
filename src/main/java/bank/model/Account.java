package bank.model;

import bank.exception.BankException;

import java.util.UUID;

public abstract class Account {
    protected final String id;
    protected final Client owner;
    protected double balance;
    protected double suspiciousLimit;

    public Account(Client owner, double initialBalance, double suspiciousLimit) {
        this.id = UUID.randomUUID().toString();
        this.owner = owner;
        this.balance = initialBalance;
        this.suspiciousLimit = suspiciousLimit;
    }

    public abstract void deposit(double amount) throws BankException;
    public abstract void withdraw(double amount) throws BankException;
    public abstract void applyDailyInterestAndFees();
    public abstract void applyMonthlyPayouts();
    public abstract String getType();

    protected void checkSuspiciousLimit(double amount) throws BankException {
        if (owner.isSuspicious() && amount > suspiciousLimit) {
            throw new BankException("Сомнительный клиент не может снимать/переводить более " + suspiciousLimit);
        }
    }

    public String getId() { return id; }
    public Client getOwner() { return owner; }
    public double getBalance() { return balance; }

    @Override
    public String toString() {
        return String.format("%s[id=%s, владелец=%s %s, баланс=%.2f]",
                getType(), id.substring(0, 8), owner.getFirstName(), owner.getLastName(), balance);
    }
}
