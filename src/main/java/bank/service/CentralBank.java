package bank.service;

import bank.command.*;
import bank.exception.BankException;
import bank.model.*;

import java.time.LocalDate;
import java.util.*;

public class CentralBank {
    private static CentralBank instance;

    private final List<Bank> banks = new ArrayList<>();
    private final List<Transaction> transactionHistory = new ArrayList<>();
    private final Map<String, BankCommand> commandMap = new HashMap<>();
    // Track which bank each transaction belongs to
    private final Map<String, Bank> transactionBankMap = new HashMap<>();
    private final TimeService timeService;

    private CentralBank() {
        this.timeService = new TimeService(LocalDate.now());
    }

    public static CentralBank getInstance() {
        if (instance == null) instance = new CentralBank();
        return instance;
    }

    public Bank registerBank(String name, BankConditions conditions) {
        Bank bank = new Bank(name, conditions);
        banks.add(bank);
        return bank;
    }

    public Transaction deposit(Account account, double amount, Bank bank) throws BankException {
        DepositCommand cmd = new DepositCommand(account, amount);
        Transaction tx = cmd.execute();
        transactionHistory.add(tx);
        commandMap.put(tx.getId(), cmd);
        if (bank != null) transactionBankMap.put(tx.getId(), bank);
        return tx;
    }

    public Transaction withdraw(Account account, double amount, Bank bank) throws BankException {
        WithdrawCommand cmd = new WithdrawCommand(account, amount);
        Transaction tx = cmd.execute();
        transactionHistory.add(tx);
        commandMap.put(tx.getId(), cmd);
        if (bank != null) transactionBankMap.put(tx.getId(), bank);
        return tx;
    }

    public Transaction transfer(Account from, Account to, double amount, Bank bank) throws BankException {
        TransferCommand cmd = new TransferCommand(from, to, amount);
        Transaction tx = cmd.execute();
        transactionHistory.add(tx);
        commandMap.put(tx.getId(), cmd);
        if (bank != null) transactionBankMap.put(tx.getId(), bank);
        return tx;
    }

    public void cancelTransaction(String transactionId, Bank requestingBank) throws BankException {
        BankCommand cmd = commandMap.get(transactionId);
        if (cmd == null) throw new BankException("Транзакция не найдена: " + transactionId);

        Transaction tx = transactionHistory.stream()
                .filter(t -> t.getId().equals(transactionId))
                .findFirst()
                .orElseThrow(() -> new BankException("Транзакция не найдена"));

        if (tx.isCancelled()) throw new BankException("Транзакция уже отменена");

        // Check bank ownership if a bank is specified
        if (requestingBank != null) {
            Bank txBank = transactionBankMap.get(transactionId);
            if (txBank != null && !txBank.equals(requestingBank)) {
                throw new BankException("Банк \"" + requestingBank.getName() +
                        "\" не может отменить транзакцию другого банка");
            }
        }

        cmd.undo();
    }

    public List<Transaction> getTransactionsForBank(Bank bank) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : transactionHistory) {
            if (bank.equals(transactionBankMap.get(tx.getId()))) result.add(tx);
        }
        return result;
    }

    public void advanceDays(int days) {
        timeService.advanceDays(days, banks);
        System.out.println("Текущая дата: " + timeService.getCurrentDate());
    }

    public List<Bank> getBanks() { return Collections.unmodifiableList(banks); }
    public List<Transaction> getTransactionHistory() { return Collections.unmodifiableList(transactionHistory); }
    public LocalDate getCurrentDate() { return timeService.getCurrentDate(); }

    public Bank findBankByName(String name) throws BankException {
        return banks.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new BankException("Банк не найден: " + name));
    }
}
