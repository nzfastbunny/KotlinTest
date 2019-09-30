package com.nurturecloud;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner command = new Scanner(System.in);
        boolean running = true;
        while(running){

            System.out.print("Please enter a suburb name: ");
            String suburbName = command.nextLine();

            System.out.print("Please enter the postcode: ");
            String postcode = command.nextLine();

            System.out.println(String.format("Nothing found for %s, %s!!\n", suburbName, postcode));

        }
        command.close();
    }
}
