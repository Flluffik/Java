package bank.model;

public class BankConditions {
    private double debitInterestRate;      // annual %
    private double creditLimit;
    private double creditMonthlyCommission;
    private double suspiciousClientLimit;

    public BankConditions(double debitInterestRate, double creditLimit,
                          double creditMonthlyCommission, double suspiciousClientLimit) {
        this.debitInterestRate = debitInterestRate;
        this.creditLimit = creditLimit;
        this.creditMonthlyCommission = creditMonthlyCommission;
        this.suspiciousClientLimit = suspiciousClientLimit;
    }

    public double getDebitInterestRate() { return debitInterestRate; }
    public double getCreditLimit() { return creditLimit; }
    public double getCreditMonthlyCommission() { return creditMonthlyCommission; }
    public double getSuspiciousClientLimit() { return suspiciousClientLimit; }

    public void setDebitInterestRate(double rate) { this.debitInterestRate = rate; }
    public void setCreditLimit(double limit) { this.creditLimit = limit; }
    public void setCreditMonthlyCommission(double commission) { this.creditMonthlyCommission = commission; }
    public void setSuspiciousClientLimit(double limit) { this.suspiciousClientLimit = limit; }

    @Override
    public String toString() {
        return String.format("Условия[ставкаДебет=%.2f%%, кредитЛимит=%.2f, комиссия=%.2f, лимитСомнит=%.2f]",
                debitInterestRate, creditLimit, creditMonthlyCommission, suspiciousClientLimit);
    }
}
