package bank.model;

import bank.notification.NotificationChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Client {
    private final String id;
    private final String firstName;
    private final String lastName;
    private String address;
    private String passport;
    private String phone;
    private boolean subscribedToNotifications = false;
    private final List<NotificationChannel> notificationChannels = new ArrayList<>();
    private final List<String> inbox = new ArrayList<>();

    private Client(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.address = builder.address;
        this.passport = builder.passport;
        this.phone = builder.phone;
    }

    public boolean isSuspicious() {
        return address == null || address.isBlank() || passport == null || passport.isBlank();
    }

    public void setAddress(String address) { this.address = address; }
    public void setPassport(String passport) { this.passport = passport; }

    public void subscribe(NotificationChannel channel) {
        notificationChannels.clear();
        notificationChannels.add(channel);
        subscribedToNotifications = true;
    }

    public void unsubscribe() {
        notificationChannels.clear();
        subscribedToNotifications = false;
    }

    public boolean isSubscribed() { return subscribedToNotifications; }

    public void notify(String message) {
        if (!notificationChannels.isEmpty()) {
            inbox.add(message);
            for (NotificationChannel ch : notificationChannels) {
                ch.send(firstName + " " + lastName, message);
            }
        }
    }

    public List<String> getInbox() { return new ArrayList<>(inbox); }
    public void clearInbox() { inbox.clear(); }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public String getPassport() { return passport; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    public static class Builder {
        private final String firstName;
        private final String lastName;
        private String address;
        private String passport;
        private String phone;

        public Builder(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Builder address(String address)   { this.address = address; return this; }
        public Builder passport(String passport) { this.passport = passport; return this; }
        public Builder phone(String phone)       { this.phone = phone; return this; }
        public Client build() { return new Client(this); }
    }
}
