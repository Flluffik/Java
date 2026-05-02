package bank.console;

import bank.exception.BankException;
import bank.model.*;
import bank.notification.ConsoleNotificationChannel;
import bank.service.CentralBank;

import java.util.*;
import java.nio.charset.Charset;

public class BankConsole {
    private final CentralBank centralBank = CentralBank.getInstance();
    private final Scanner scanner = new Scanner(System.in, Charset.forName("CP1251"));

    private final Map<String, Client> clientRegistry  = new LinkedHashMap<>();
    private final Map<String, Account> accountRegistry = new LinkedHashMap<>();
    private final Map<String, Bank> accountBankMap     = new LinkedHashMap<>();

    private String readLine() {
        System.out.flush();
        return scanner.nextLine().trim();
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(readLine().replace(",", ".")); }
            catch (NumberFormatException e) { System.out.println("Введите число."); }
        }
    }

    // ── ЗАПУСК ──────────────────────────────────────────────────
    public void run() {
        System.out.println("Дата: " + centralBank.getCurrentDate());
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = readLine();
            switch (choice) {
                case "1" -> createBank();
                case "2" -> selectBankMenu();
                case "3" -> createClient();
                case "4" -> selectClientMenu();
                case "5" -> advanceTime();
                case "6" -> showStatus();
                case "0" -> running = false;
                default  -> System.out.println("Неизвестная команда.");
            }
        }
        System.out.println("До свидания!");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("ГЛАВНОЕ МЕНЮ");
        System.out.println("  1. Создать банк");
        System.out.println("  2. Выбрать банк");
        System.out.println("  3. Создать клиента");
        System.out.println("  4. Выбрать клиента");
        System.out.println("  5. Перемотать время");
        System.out.println("  6. Статус системы");
        System.out.println("  0. Выход");
        System.out.print("Выбор: ");
    }

    // ── СОЗДАНИЕ БАНКА ──────────────────────────────────────────
    private void createBank() {
        System.out.println("\nНовый банк");
        System.out.print("Название: ");
        String name = readLine();
        if (name.isEmpty()) { System.out.println("Название не может быть пустым."); return; }

        double rate      = readDouble("Процентная ставка по дебету (годовая %, напр. 3.65): ");
        double limit     = readDouble("Кредитный лимит: ");
        double comm      = readDouble("Ежемесячная комиссия по кредиту: ");
        double suspLimit = readDouble("Лимит операций для сомнительных клиентов: ");

        Bank bank = centralBank.registerBank(name, new BankConditions(rate, limit, comm, suspLimit));
        System.out.println("Банк \"" + bank.getName() + "\" создан.");
    }

    // ── МЕНЮ БАНКА ──────────────────────────────────────────────
    private void selectBankMenu() {
        if (centralBank.getBanks().isEmpty()) { System.out.println("\nБанков нет."); return; }
        Bank bank = selectBank();
        if (bank == null) return;
        bankMenu(bank);
    }

    private void bankMenu(Bank bank) {
        boolean inMenu = true;
        while (inMenu) {
            BankConditions c = bank.getConditions();
            System.out.println();
            System.out.println("БАНК: " + bank.getName());
            System.out.printf("  Ставка по дебету:        %.2f%%%n",       c.getDebitInterestRate());
            System.out.printf("  Кредитный лимит:         %s руб.%n",      fmt(c.getCreditLimit()));
            System.out.printf("  Комиссия по кредиту:     %s руб./мес.%n", fmt(c.getCreditMonthlyCommission()));
            System.out.printf("  Лимит для сомнительных:  %s руб.%n",      fmt(c.getSuspiciousClientLimit()));
            System.out.println();
            System.out.println("  1. Изменить условия");
            System.out.println("  2. История транзакций банка");
            System.out.println("  3. Отменить транзакцию банка");
            System.out.println("  0. Назад");
            System.out.print("Выбор: ");

            switch (readLine()) {
                case "1" -> editBankConditions(bank);
                case "2" -> showBankTransactions(bank);
                case "3" -> cancelBankTransaction(bank);
                case "0" -> inMenu = false;
                default  -> System.out.println("Неизвестная команда.");
            }
        }
    }

    private void editBankConditions(Bank bank) {
        BankConditions cur = bank.getConditions();
        System.out.println("\nИзменение условий банка \"" + bank.getName() + "\"");
        System.out.println("Оставьте поле пустым чтобы не менять значение.");

        System.out.printf("Ставка по дебету (сейчас %.2f%%): ", cur.getDebitInterestRate());
        String s = readLine();
        double rate = s.isEmpty() ? cur.getDebitInterestRate() : Double.parseDouble(s.replace(",", "."));

        System.out.printf("Кредитный лимит (сейчас %s): ", fmt(cur.getCreditLimit()));
        s = readLine();
        double limit = s.isEmpty() ? cur.getCreditLimit() : Double.parseDouble(s.replace(",", "."));

        System.out.printf("Комиссия по кредиту (сейчас %s): ", fmt(cur.getCreditMonthlyCommission()));
        s = readLine();
        double comm = s.isEmpty() ? cur.getCreditMonthlyCommission() : Double.parseDouble(s.replace(",", "."));

        System.out.printf("Лимит для сомнительных (сейчас %s): ", fmt(cur.getSuspiciousClientLimit()));
        s = readLine();
        double suspLimit = s.isEmpty() ? cur.getSuspiciousClientLimit() : Double.parseDouble(s.replace(",", "."));

        bank.updateConditions(new BankConditions(rate, limit, comm, suspLimit));
        System.out.println("Условия обновлены. Подписанные клиенты получили уведомление.");
    }

    private void showBankTransactions(Bank bank) {
        var txs = centralBank.getTransactionsForBank(bank);
        System.out.println("\nТранзакции банка \"" + bank.getName() + "\":");
        if (txs.isEmpty()) { System.out.println("  Транзакций нет."); return; }
        for (Transaction tx : txs) System.out.println("  " + formatTransaction(tx));
    }

    private void cancelBankTransaction(Bank bank) {
        var txs = centralBank.getTransactionsForBank(bank);
        if (txs.isEmpty()) { System.out.println("\nТранзакций нет."); return; }
        System.out.println("\nТранзакции банка \"" + bank.getName() + "\":");
        txs.forEach(tx -> System.out.println("  " + formatTransaction(tx)));
        System.out.print("\nНомер операции для отмены (0 - назад): ");
        String txId = readLine();
        if (txId.equals("0")) return;
        String fullId = txs.stream()
                .filter(t -> t.getId().startsWith(txId))
                .map(Transaction::getId)
                .findFirst().orElse(txId);
        try {
            centralBank.cancelTransaction(fullId, bank);
            System.out.println("Операция отменена.");
        } catch (BankException e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    // ── СОЗДАНИЕ КЛИЕНТА ────────────────────────────────────────
    private void createClient() {
        System.out.println("\nНовый клиент");
        System.out.print("Имя: ");
        String firstName = readLine();
        System.out.print("Фамилия: ");
        String lastName = readLine();
        if (firstName.isEmpty() || lastName.isEmpty()) {
            System.out.println("Имя и фамилия обязательны.");
            return;
        }
        System.out.print("Адрес (Enter - пропустить): ");
        String address = readLine();
        System.out.print("Номер паспорта (Enter - пропустить): ");
        String passport = readLine();
        System.out.print("Телефон (Enter - пропустить): ");
        String phone = readLine();

        Client.Builder builder = new Client.Builder(firstName, lastName);
        if (!address.isEmpty())  builder.address(address);
        if (!passport.isEmpty()) builder.passport(passport);
        if (!phone.isEmpty())    builder.phone(phone);

        Client client = builder.build();
        String key = makeClientKey(firstName, lastName);
        clientRegistry.put(key, client);

        System.out.println("Клиент создан. Ключ для выбора: " + key);
        if (client.isSuspicious())
            System.out.println("Внимание: клиент считается сомнительным (не указан адрес или паспорт).");
    }

    // ── ВЫБОР КЛИЕНТА ───────────────────────────────────────────
    private void selectClientMenu() {
        if (clientRegistry.isEmpty()) { System.out.println("\nКлиентов нет."); return; }
        System.out.println("\nСписок клиентов:");
        clientRegistry.forEach((key, c) ->
            System.out.printf("  %-20s  %s %s  (%s)%n", key, c.getFirstName(), c.getLastName(),
                    c.isSuspicious() ? "сомнительный" : "надёжный"));
        System.out.print("\nКлюч клиента (0 - назад): ");
        String key = readLine().toLowerCase();
        if (key.equals("0")) return;
        Client client = clientRegistry.get(key);
        if (client == null) { System.out.println("Клиент не найден."); return; }
        clientMenu(client);
    }

    // ── МЕНЮ КЛИЕНТА ────────────────────────────────────────────
    private void clientMenu(Client client) {
        boolean inClient = true;
        while (inClient) {
            String status = client.isSuspicious() ? "(сомнительный)" : "(надёжный)";
            String notifLabel = client.isSubscribed() ? "  9. Отписаться от уведомлений"
                                                      : "  9. Подписаться на уведомления";
            int unread = client.getInbox().size();
            String inboxLabel = unread > 0 ? "  8. Уведомления  [" + unread + " новых]" : "  8. Уведомления";

            System.out.println();
            System.out.println("КЛИЕНТ: " + client.getFirstName() + " " + client.getLastName() + "  " + status);
            System.out.println("  1. Открыть счёт");
            System.out.println("  2. Пополнить счёт");
            System.out.println("  3. Снять деньги");
            System.out.println("  4. Перевод на другой счёт");
            System.out.println("  5. История транзакций");
            System.out.println("  6. Мои счета");
            System.out.println("  7. Обновить данные");
            System.out.println(inboxLabel);
            System.out.println(notifLabel);
            System.out.println("  0. Назад");
            System.out.print("Выбор: ");

            switch (readLine()) {
                case "1" -> openAccount(client);
                case "2" -> depositMoney(client);
                case "3" -> withdrawMoney(client);
                case "4" -> transferMoney(client);
                case "5" -> showClientTransactions(client);
                case "6" -> showClientAccounts(client);
                case "7" -> updateClientData(client);
                case "8" -> showInbox(client);
                case "9" -> toggleNotifications(client);
                case "0" -> inClient = false;
                default  -> System.out.println("Неизвестная команда.");
            }
        }
    }

    // ── СЧЕТА ───────────────────────────────────────────────────
    private void openAccount(Client client) {
        if (centralBank.getBanks().isEmpty()) { System.out.println("\nСначала создайте банк."); return; }
        Bank bank = selectBank();
        if (bank == null) return;

        bank.registerClient(client);

        System.out.println("\nТип счёта:");
        System.out.println("  1. Дебетовый");
        System.out.println("  2. Депозит");
        System.out.println("  3. Кредитный");
        System.out.print("Выбор: ");

        try {
            Account account = switch (readLine()) {
                case "1" -> {
                    double bal = readDouble("Начальный баланс: ");
                    yield bank.createDebitAccount(client, bal);
                }
                case "2" -> {
                    double bal  = readDouble("Начальный баланс: ");
                    int    term = (int) readDouble("Срок в днях: ");
                    yield bank.createDepositAccount(client, bal, centralBank.getCurrentDate(), term);
                }
                case "3" -> bank.createCreditAccount(client);
                default  -> { System.out.println("Неверный тип."); yield null; }
            };
            if (account != null) {
                String shortId = account.getId().substring(0, 8);
                accountRegistry.put(shortId, account);
                accountBankMap.put(shortId, bank);
                System.out.println("Счёт открыт. ID: " + shortId + "  Баланс: " + fmt(account.getBalance()) + " руб.");
            }
        } catch (BankException e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    private void depositMoney(Client client) {
        Account account = selectClientAccount(client);
        if (account == null) return;
        double amount = readDouble("Сумма пополнения: ");
        try {
            Bank bank = accountBankMap.get(account.getId().substring(0, 8));
            var tx = centralBank.deposit(account, amount, bank);
            System.out.println("Пополнено на " + fmt(amount) + " руб.  Номер операции: " + tx.getId().substring(0, 8));
        } catch (BankException e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    private void withdrawMoney(Client client) {
        Account account = selectClientAccount(client);
        if (account == null) return;
        double amount = readDouble("Сумма снятия: ");
        try {
            Bank bank = accountBankMap.get(account.getId().substring(0, 8));
            var tx = centralBank.withdraw(account, amount, bank);
            System.out.println("Снято " + fmt(amount) + " руб.  Номер операции: " + tx.getId().substring(0, 8));
        } catch (BankException e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    private void transferMoney(Client client) {
        System.out.println("\nВыберите счёт для отправки:");
        Account from = selectClientAccount(client);
        if (from == null) return;
        System.out.println("\nВыберите счёт получателя:");
        Account to = selectAnyAccount();
        if (to == null) return;
        double amount = readDouble("Сумма перевода: ");
        try {
            Bank bank = accountBankMap.get(from.getId().substring(0, 8));
            var tx = centralBank.transfer(from, to, amount, bank);
            System.out.println("Перевод " + fmt(amount) + " руб. выполнен.  Номер операции: " + tx.getId().substring(0, 8));
        } catch (BankException e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    // ── ТРАНЗАКЦИИ КЛИЕНТА ──────────────────────────────────────
    private void showClientTransactions(Client client) {
        var history = centralBank.getTransactionHistory();
        System.out.println("\nИстория операций: " + client.getFirstName() + " " + client.getLastName());
        boolean found = false;
        for (Transaction tx : history) {
            boolean belongs = (tx.getFromAccount() != null && tx.getFromAccount().getOwner() == client)
                    || (tx.getToAccount() != null && tx.getToAccount().getOwner() == client);
            if (belongs) { System.out.println("  " + formatTransaction(tx)); found = true; }
        }
        if (!found) System.out.println("  Операций нет.");
    }

    private String formatTransaction(Transaction tx) {
        String status = tx.isCancelled() ? "  [отменена]" : "";
        String ownerFrom = tx.getFromAccount() != null
                ? tx.getFromAccount().getOwner().getFirstName() + " " + tx.getFromAccount().getOwner().getLastName() : "";
        String ownerTo = tx.getToAccount() != null
                ? tx.getToAccount().getOwner().getFirstName() + " " + tx.getToAccount().getOwner().getLastName() : "";
        String shortId = tx.getId().substring(0, 8);
        return switch (tx.getType()) {
            case DEPOSIT  -> shortId + "  Пополнение  " + fmt(tx.getAmount()) + " руб.  (" + ownerFrom + ")" + status;
            case WITHDRAW -> shortId + "  Снятие      " + fmt(tx.getAmount()) + " руб.  (" + ownerFrom + ")" + status;
            case TRANSFER -> shortId + "  Перевод     " + fmt(tx.getAmount()) + " руб.  от " + ownerFrom + " к " + ownerTo + status;
        };
    }

    // ── УВЕДОМЛЕНИЯ ─────────────────────────────────────────────
    private void toggleNotifications(Client client) {
        if (client.isSubscribed()) {
            client.unsubscribe();
            System.out.println("Вы отписались от уведомлений.");
        } else {
            client.subscribe(new ConsoleNotificationChannel());
            System.out.println("Вы подписались на уведомления.");
        }
    }

    private void showInbox(Client client) {
        List<String> inbox = client.getInbox();
        System.out.println("\nУведомления: " + client.getFirstName() + " " + client.getLastName());
        if (inbox.isEmpty()) {
            System.out.println("  Новых уведомлений нет.");
        } else {
            for (int i = 0; i < inbox.size(); i++)
                System.out.println("  " + (i + 1) + ". " + inbox.get(i));
            System.out.print("\nОчистить уведомления? (1 - да, Enter - нет): ");
            if (readLine().equals("1")) { client.clearInbox(); System.out.println("Уведомления очищены."); }
        }
    }

    // ── ДАННЫЕ КЛИЕНТА ──────────────────────────────────────────
    private void showClientAccounts(Client client) {
        System.out.println("\nСчета: " + client.getFirstName() + " " + client.getLastName());
        boolean found = false;
        for (Map.Entry<String, Account> entry : accountRegistry.entrySet()) {
            Account acc = entry.getValue();
            if (acc.getOwner() == client) {
                String bankName = accountBankMap.containsKey(entry.getKey())
                        ? accountBankMap.get(entry.getKey()).getName() : "неизвестно";
                System.out.printf("  [%s]  %-20s  Банк: %-15s  %s руб.%n",
                        entry.getKey(), accountTypeName(acc.getType()), bankName, fmt(acc.getBalance()));
                found = true;
            }
        }
        if (!found) System.out.println("  Счетов нет.");
    }

    private void updateClientData(Client client) {
        System.out.println("\nОбновить данные клиента");
        System.out.println("  Имя:     " + client.getFirstName() + " " + client.getLastName());
        System.out.println("  Адрес:   " + (client.getAddress()  != null ? client.getAddress()  : "не указан"));
        System.out.println("  Паспорт: " + (client.getPassport() != null ? client.getPassport() : "не указан"));
        System.out.println("  Телефон: " + (client.getPhone()    != null ? client.getPhone()    : "не указан"));
        System.out.print("\nНовый адрес (Enter - оставить): ");
        String address = readLine();
        if (!address.isEmpty()) client.setAddress(address);
        System.out.print("Новый паспорт (Enter - оставить): ");
        String passport = readLine();
        if (!passport.isEmpty()) client.setPassport(passport);
        System.out.println("Данные сохранены.");
        System.out.println(client.isSuspicious()
                ? "Клиент по-прежнему сомнительный (нужны и адрес, и паспорт)."
                : "Ограничения сняты - клиент больше не считается сомнительным.");
    }

    // ── ВРЕМЯ ───────────────────────────────────────────────────
    private void advanceTime() {
        System.out.println("\nПеремотка времени:");
        System.out.println("  1. 1 день");
        System.out.println("  2. 30 дней");
        System.out.println("  3. 1 год (365 дней)");
        System.out.println("  4. Произвольное количество дней");
        System.out.println("  0. Назад");
        System.out.print("Выбор: ");
        int days = switch (readLine()) {
            case "1" -> 1;
            case "2" -> 30;
            case "3" -> 365;
            case "4" -> (int) readDouble("Количество дней: ");
            default  -> 0;
        };
        if (days > 0) centralBank.advanceDays(days);
    }

    // ── СТАТУС ──────────────────────────────────────────────────
    private void showStatus() {
        System.out.println("\nСТАТУС СИСТЕМЫ");
        System.out.println("Дата: " + centralBank.getCurrentDate());

        var banks = centralBank.getBanks();
        if (banks.isEmpty()) {
            System.out.println("\nБанков нет.");
        } else {
            for (Bank bank : banks) {
                BankConditions c = bank.getConditions();
                System.out.println("\nБанк: " + bank.getName());
                System.out.printf("  Ставка по дебету:        %.2f%% годовых%n",      c.getDebitInterestRate());
                System.out.printf("  Кредитный лимит:         %s руб.%n",             fmt(c.getCreditLimit()));
                System.out.printf("  Комиссия по кредиту:     %s руб./мес.%n",        fmt(c.getCreditMonthlyCommission()));
                System.out.printf("  Лимит для сомнительных:  %s руб.%n",             fmt(c.getSuspiciousClientLimit()));
                System.out.printf("  Клиентов: %d%n",                                 bank.getClients().size());
                var accounts = bank.getAccounts();
                if (accounts.isEmpty()) {
                    System.out.println("  Счетов нет.");
                } else {
                    System.out.println("  Счета:");
                    for (Account acc : accounts)
                        System.out.printf("    [%s]  %-20s  %s %s  %s руб.%n",
                                acc.getId().substring(0, 8), accountTypeName(acc.getType()),
                                acc.getOwner().getFirstName(), acc.getOwner().getLastName(), fmt(acc.getBalance()));
                }
            }
        }

        System.out.println("\nКлиенты:");
        if (clientRegistry.isEmpty()) {
            System.out.println("  Клиентов нет.");
        } else {
            clientRegistry.forEach((key, cl) ->
                System.out.printf("  %-20s  %s %s  (%s)%n", key, cl.getFirstName(), cl.getLastName(),
                        cl.isSuspicious() ? "сомнительный" : "надёжный"));
        }
    }

    // ── ВСПОМОГАТЕЛЬНЫЕ ─────────────────────────────────────────
    private String accountTypeName(String type) {
        return switch (type) {
            case "DebitAccount"   -> "Дебетовый счёт";
            case "DepositAccount" -> "Депозит";
            case "CreditAccount"  -> "Кредитный счёт";
            default               -> type;
        };
    }

    private String fmt(double amount) { return String.format("%,.2f", amount); }

    private Bank selectBank() {
        System.out.println("\nДоступные банки:");
        centralBank.getBanks().forEach(b -> System.out.println("  " + b.getName()));
        System.out.print("Название банка (0 - назад): ");
        String name = readLine();
        if (name.equals("0")) return null;
        try { return centralBank.findBankByName(name); }
        catch (BankException e) { System.out.println("Банк не найден."); return null; }
    }

    private Account selectClientAccount(Client client) {
        List<Map.Entry<String, Account>> entries = accountRegistry.entrySet().stream()
                .filter(e -> e.getValue().getOwner() == client).toList();
        if (entries.isEmpty()) { System.out.println("\nУ клиента нет счетов."); return null; }
        System.out.println("\nСчета клиента:");
        entries.forEach(e -> {
            String bankName = accountBankMap.containsKey(e.getKey()) ? accountBankMap.get(e.getKey()).getName() : "";
            System.out.printf("  [%s]  %-20s  Банк: %-15s  %s руб.%n",
                    e.getKey(), accountTypeName(e.getValue().getType()), bankName, fmt(e.getValue().getBalance()));
        });
        System.out.print("ID счёта (0 - назад): ");
        String id = readLine();
        if (id.equals("0")) return null;
        Account a = accountRegistry.get(id);
        if (a == null || a.getOwner() != client) { System.out.println("Счёт не найден."); return null; }
        return a;
    }

    private Account selectAnyAccount() {
        if (accountRegistry.isEmpty()) { System.out.println("Счетов в системе нет."); return null; }
        System.out.println("\nВсе счета:");
        accountRegistry.forEach((k, v) -> {
            String bankName = accountBankMap.containsKey(k) ? accountBankMap.get(k).getName() : "";
            System.out.printf("  [%s]  %-20s  Банк: %-15s  %s %s  %s руб.%n",
                    k, accountTypeName(v.getType()), bankName,
                    v.getOwner().getFirstName(), v.getOwner().getLastName(), fmt(v.getBalance()));
        });
        System.out.print("ID счёта (0 - назад): ");
        String id = readLine();
        if (id.equals("0")) return null;
        Account a = accountRegistry.get(id);
        if (a == null) { System.out.println("Счёт не найден."); return null; }
        return a;
    }

    private String makeClientKey(String firstName, String lastName) {
        String base = (firstName + lastName).toLowerCase().replaceAll("\\s+", "");
        if (!clientRegistry.containsKey(base)) return base;
        int i = 2;
        while (clientRegistry.containsKey(base + i)) i++;
        return base + i;
    }
}
