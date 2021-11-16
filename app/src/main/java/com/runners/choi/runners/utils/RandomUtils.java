package com.runners.choi.runners.utils;

public class RandomUtils {


    /**

     * @param args

     */

    public static void main(String[] args) {

        // TODO Auto-generated method stub

        for(int i = 0 ; i < 100 ; i++){

            System.out.println(getRandomIntNum(1,10));

        }



    }

    public static int getRandomIntNum(int min, int max){

        Double randomNum = ( Math.random() * (max - min + 1) ) + min ;



        return  randomNum.intValue();

    }

}