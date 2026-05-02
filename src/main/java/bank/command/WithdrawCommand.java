package bank.command;

import bank.exception.BankException;
import bank.model.Account;
import bank.model.Transaction;

public class WithdrawCommand implements BankCommand {
    private final Account account;
    private final double amount;
    private Transaction transaction;

    public WithdrawCommand(Account account, double amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public Transaction execute() throws BankException {
        account.withdraw(amount);
        transaction = new Transaction(Transaction.Type.WITHDRAW, account, amount);
        return transaction;
    }

    @Override
    public void undo() throws BankException {
        if (transaction == null || transaction.isCancelled()) {
            throw new BankException("Транзакцию невозможно отменить");
        }
        account.deposit(amount);
        transaction.setCancelled(true);
    }
}
