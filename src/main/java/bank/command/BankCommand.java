package bank.command;

import bank.exception.BankException;
import bank.model.Transaction;

public interface BankCommand {
    Transaction execute() throws BankException;
    void undo() throws BankException;
}
