package bank.model;

import java.util.UUID;

public class Transaction {
    public enum Type { DEPOSIT, WITHDRAW, TRANSFER }

    private final String id;
    private final Type type;
    private final Account fromAccount;
    private final Account toAccount;
    private final double amount;
    private boolean cancelled = false;

    // Deposit or Withdraw
    public Transaction(Type type, Account account, double amount) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.fromAccount = account;
        this.toAccount = null;
        this.amount = amount;
    }

    // Transfer
    public Transaction(Account from, Account to, double amount) {
        this.id = UUID.randomUUID().toString();
        this.type = Type.TRANSFER;
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
    }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public String getId() { return id; }
    public Type getType() { return type; }
    public Account getFromAccount() { return fromAccount; }
    public Account getToAccount() { return toAccount; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        if (type == Type.TRANSFER) {
            return String.format("Транзакция[%s, ПЕРЕВОД %.2f со счёта %s на счёт %s, отменена=%b]",
                    id.substring(0, 8), amount, fromAccount.getId().substring(0, 8),
                    toAccount.getId().substring(0, 8), cancelled);
        }
        return String.format("Транзакция[%s, %s %.2f на счёте %s, отменена=%b]",
                id.substring(0, 8), type, amount, fromAccount.getId().substring(0, 8), cancelled);
    }
}
