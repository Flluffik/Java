package bank.model;

import bank.exception.BankException;

import java.time.LocalDate;
import java.util.*;

public class Bank {
    private final String id;
    private final String name;
    private BankConditions conditions;
    private final List<Client> clients = new ArrayList<>();
    private final List<Account> accounts = new ArrayList<>();

    public Bank(String name, BankConditions conditions) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.conditions = conditions;
    }

    public void registerClient(Client client) {
        if (!clients.contains(client)) clients.add(client);
    }

    public DebitAccount createDebitAccount(Client client, double initialBalance) throws BankException {
        ensureClientRegistered(client);
        DebitAccount acc = new DebitAccount(client, initialBalance,
                conditions.getDebitInterestRate(), conditions.getSuspiciousClientLimit());
        accounts.add(acc);
        return acc;
    }

    public DepositAccount createDepositAccount(Client client, double initialBalance,
                                               LocalDate openDate, int termDays) throws BankException {
        ensureClientRegistered(client);
        DepositAccount acc = new DepositAccount(client, initialBalance,
                conditions.getSuspiciousClientLimit(), openDate, termDays);
        accounts.add(acc);
        return acc;
    }

    public CreditAccount createCreditAccount(Client client) throws BankException {
        ensureClientRegistered(client);
        CreditAccount acc = new CreditAccount(client,
                conditions.getCreditLimit(), conditions.getCreditMonthlyCommission(),
                conditions.getSuspiciousClientLimit());
        accounts.add(acc);
        return acc;
    }

    private void ensureClientRegistered(Client client) throws BankException {
        if (!clients.contains(client)) {
            throw new BankException("Клиент не зарегистрирован в банке: " + name);
        }
    }

    public Account findAccount(String accountId) throws BankException {
        return accounts.stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new BankException("Account not found: " + accountId));
    }

    public void applyDailyProcessing() {
        for (Account acc : accounts) {
            acc.applyDailyInterestAndFees();
        }
    }

    public void applyMonthlyProcessing() {
        for (Account acc : accounts) {
            acc.applyMonthlyPayouts();
        }
    }

    public void updateConditions(BankConditions newConditions) {
        BankConditions old = this.conditions;
        this.conditions = newConditions;
        StringBuilder msg = new StringBuilder();
        msg.append("Банк «").append(name).append("» изменил условия обслуживания.\n");
        if (old.getDebitInterestRate() != newConditions.getDebitInterestRate())
            msg.append("  Ставка по дебету: ").append(String.format("%.2f%%", old.getDebitInterestRate()))
               .append(" -> ").append(String.format("%.2f%%", newConditions.getDebitInterestRate())).append("\n");
        if (old.getCreditLimit() != newConditions.getCreditLimit())
            msg.append("  Кредитный лимит: ").append(String.format("%.0f", old.getCreditLimit()))
               .append(" -> ").append(String.format("%.0f руб.", newConditions.getCreditLimit())).append("\n");
        if (old.getCreditMonthlyCommission() != newConditions.getCreditMonthlyCommission())
            msg.append("  Комиссия по кредиту: ").append(String.format("%.0f", old.getCreditMonthlyCommission()))
               .append(" -> ").append(String.format("%.0f руб./мес.", newConditions.getCreditMonthlyCommission())).append("\n");
        if (old.getSuspiciousClientLimit() != newConditions.getSuspiciousClientLimit())
            msg.append("  Лимит для сомнительных клиентов: ").append(String.format("%.0f", old.getSuspiciousClientLimit()))
               .append(" -> ").append(String.format("%.0f руб.", newConditions.getSuspiciousClientLimit())).append("\n");
        String message = msg.toString().trim();
        for (Client c : clients) {
            c.notify(message);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BankConditions getConditions() { return conditions; }
    public List<Client> getClients() { return Collections.unmodifiableList(clients); }
    public List<Account> getAccounts() { return Collections.unmodifiableList(accounts); }

    @Override
    public String toString() {
        return String.format("Банк[%s, клиентов=%d, счетов=%d]", name, clients.size(), accounts.size());
    }
}
