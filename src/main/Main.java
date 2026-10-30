package main;

import main.adapters.driving.menu.ConsoleMenuAdapter;
import main.ports.in.MenuPort;

public class Main {

    public static void main(String[] args) {
        MenuPort menu = new ConsoleMenuAdapter();
        menu.configureAndStartGame();
    }
}