package bank.command;

import bank.exception.BankException;
import bank.model.Account;
import bank.model.Transaction;

public class TransferCommand implements BankCommand {
    private final Account from;
    private final Account to;
    private final double amount;
    private Transaction transaction;

    public TransferCommand(Account from, Account to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public Transaction execute() throws BankException {
        from.withdraw(amount);
        try {
            to.deposit(amount);
        } catch (BankException e) {
            // Rollback withdrawal if deposit fails
            from.deposit(amount);
            throw e;
        }
        transaction = new Transaction(from, to, amount);
        return transaction;
    }

    @Override
    public void undo() throws BankException {
        if (transaction == null || transaction.isCancelled()) {
            throw new BankException("Транзакцию невозможно отменить");
        }
        to.withdraw(amount);
        from.deposit(amount);
        transaction.setCancelled(true);
    }
}
