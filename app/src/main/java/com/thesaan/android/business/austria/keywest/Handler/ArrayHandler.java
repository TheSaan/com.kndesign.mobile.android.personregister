package com.thesaan.android.business.austria.keywest.Handler;

/**
 * Created by mknoe on 14.04.2015.
 */
public class ArrayHandler {

    public static void printAllValues(String[] values){
        System.out.println("Values:");
        for(int i = 0; i<values.length;i++) {
            System.out.println(i+". "+values[i]);
        }
    }
    public static void printAllValues(int[] values){
        System.out.println("Values:");
        for(int i = 0; i<values.length;i++) {
            System.out.println(i+". "+values[i]);
        }
    }
    public static void printAllValues(boolean[] values){
        System.out.println("Values:");
        for(int i = 0; i<values.length;i++) {
            System.out.println(i+". "+values[i]);
        }
    }

}
