package bank.notification;

public class ConsoleNotificationChannel implements NotificationChannel {
    @Override
    public void send(String recipient, String message) {
        // Silent - messages go to inbox only, displayed via showInbox()
    }
}
