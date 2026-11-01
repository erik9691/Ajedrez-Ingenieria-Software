package main;

import main.adapters.driving.menu.ConsoleMenuAdapter;

public class Main {

    public static void main(String[] args) {
        ConsoleMenuAdapter menu = new ConsoleMenuAdapter();
        menu.configureAndStartGame();
    }
}