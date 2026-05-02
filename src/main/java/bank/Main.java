package bank;

import bank.console.BankConsole;

import java.io.PrintStream;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, "CP1251"));
        System.setErr(new PrintStream(System.err, true, "CP1251"));
        System.out.println();
        new BankConsole().run();
    }
}
