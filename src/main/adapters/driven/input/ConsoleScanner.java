package main.adapters.driven.input;

import java.util.Scanner;

public class ConsoleScanner {

    private static final Scanner INSTANCE = new Scanner(System.in);

    private ConsoleScanner() {}

    public static Scanner getInstance() {
        return INSTANCE;
    }
}