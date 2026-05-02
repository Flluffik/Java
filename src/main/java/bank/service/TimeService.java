package bank.service;

import bank.model.Bank;

import java.time.LocalDate;
import java.util.List;

public class TimeService {
    private LocalDate currentDate;
    private int dayOfMonth = 1;

    public TimeService(LocalDate startDate) {
        this.currentDate = startDate;
        this.dayOfMonth = startDate.getDayOfMonth();
    }

    public void advanceDays(int days, List<Bank> banks) {
        for (int i = 0; i < days; i++) {
            advanceOneDay(banks);
        }
    }

    private void advanceOneDay(List<Bank> banks) {
        for (Bank bank : banks) {
            bank.applyDailyProcessing();
        }

        currentDate = currentDate.plusDays(1);
        dayOfMonth++;

        // Month boundary: apply monthly payouts
        if (currentDate.getDayOfMonth() == 1) {
            dayOfMonth = 1;
            for (Bank bank : banks) {
                bank.applyMonthlyProcessing();
            }
        }
    }

    public LocalDate getCurrentDate() { return currentDate; }
}
