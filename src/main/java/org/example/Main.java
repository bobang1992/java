package org.example;

import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

interface TransactionManager {
    void saveTransactions(String filename, List<Transaction> transactions);
    List<Transaction> loadTransactions(String filename);
}

class Transaction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int amount;
    private final LocalDate date;

    public Transaction(int amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void showInfo() {
        System.out.println("Amount: " + amount + " Date: " + date);
    }
}

class FileTransactionManager implements TransactionManager {
    @Override
    public void saveTransactions(String filename, List<Transaction> transactions) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(transactions);
            System.out.println("Transactions have been saved to file.");
        } catch (IOException e) {
            System.out.println("Could not save to file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Transaction> loadTransactions(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<Transaction>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not load from file: " + e.getMessage());
            return new ArrayList<>();  // Return empty list if there's an error
        }
    }
}

class InputHandler {
    private final Scanner scanner = new Scanner(System.in);

    public char getChoice() {
        System.out.print("Choose an action to perform: ");
        return scanner.nextLine().trim().charAt(0);
    }

    public int getAmount() {
        System.out.print("Enter amount: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer amount.");
            scanner.next();
        }
        int amount = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        return amount > 0 ? amount : getAmount();
    }

    public String getFilename() {
        System.out.print("Enter filename: ");
        return scanner.nextLine().trim();
    }

    public LocalDate getDate(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        while (true) {
            try {
                System.out.print("Enter date (" + format + "): ");
                String input = scanner.nextLine().trim();
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }
    }

    public int getYear() {
        System.out.print("Enter year (e.g. 2024): ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid year.");
            scanner.next();
        }
        int year = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        return year;
    }

    public int getMonth() {
        System.out.print("Enter month (1-12): ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid month.");
            scanner.next();
        }
        int month = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        return (month >= 1 && month <= 12) ? month : getMonth();
    }
}

abstract class BankAccount {
    private int balance;
    protected List<Transaction> transactions = new ArrayList<>();
    private final TransactionManager transactionManager;

    public BankAccount(String customerId, TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public int getBalance() {
        return balance;
    }

    public void deposit(int amount) {
        if (amount > 0) {
            balance += amount;
            transactions.add(new Transaction(amount, LocalDate.now()));
            System.out.println("Deposited: " + amount);
        }
    }

    public void withdraw(int amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            transactions.add(new Transaction(-amount, LocalDate.now()));
            System.out.println("Withdrawn: " + amount);
        } else {
            System.out.println("Insufficient funds or invalid amount.");
        }
    }

    public void showTransactionHistory() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions executed.");
        } else {
            transactions.forEach(Transaction::showInfo);
        }
    }

    public void showTransactionHistoryByDay(LocalDate date) {
        System.out.println("Transactions on " + date + ":");
        transactions.stream()
                .filter(t -> t.getDate().equals(date))
                .forEach(Transaction::showInfo);
    }

    public void showTransactionHistoryByMonth(int year, int month) {
        System.out.println("Transactions in " + year + "-" + (month < 10 ? "0" + month : month) + ":");
        transactions.stream()
                .filter(t -> t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                .forEach(Transaction::showInfo);
    }

    public void showTransactionHistoryByYear(int year) {
        System.out.println("Transactions in " + year + ":");
        transactions.stream()
                .filter(t -> t.getDate().getYear() == year)
                .forEach(Transaction::showInfo);
    }

    public void saveTransactions(String filename) {
        transactionManager.saveTransactions(filename, transactions);
    }

    public void loadTransactions(String filename) {
        transactions = transactionManager.loadTransactions(filename);
    }

    public abstract void showMenu();
}

class SimpleBankAccount extends BankAccount {
    private final InputHandler inputHandler;

    public SimpleBankAccount(String customerId, InputHandler inputHandler, TransactionManager transactionManager) {
        super(customerId, transactionManager);
        this.inputHandler = inputHandler;
    }

    @Override
    public void showMenu() {
        char choice;
        do {
            System.out.println("\n1: Check balance\n2: Deposit\n3: Withdraw\n4: Show transaction history\n5: Show transaction history by day\n6: Show transaction history by month\n7: Show transaction history by year\n8: Save transactions\n9: Load transactions\n0: Exit");
            choice = inputHandler.getChoice();
            switch (choice) {
                case '1': System.out.println("Balance: " + getBalance()); break;
                case '2': deposit(inputHandler.getAmount()); break;
                case '3': withdraw(inputHandler.getAmount()); break;
                case '4': showTransactionHistory(); break;
                case '5': showTransactionHistoryByDay(inputHandler.getDate("yyyy-MM-dd")); break;
                case '6': showTransactionHistoryByMonth(inputHandler.getYear(), inputHandler.getMonth()); break;
                case '7': showTransactionHistoryByYear(inputHandler.getYear()); break;
                case '8': saveTransactions(inputHandler.getFilename()); break;
                case '9': loadTransactions(inputHandler.getFilename()); break;
                case '0': System.out.println("Exiting..."); break;
                default: System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != '0');
    }

    public static void main(String[] args) {
        InputHandler inputHandler = new InputHandler();
        TransactionManager fileTransactionManager = new FileTransactionManager();
        SimpleBankAccount account = new SimpleBankAccount("JD123", inputHandler, fileTransactionManager);
        account.showMenu();
    }
}
