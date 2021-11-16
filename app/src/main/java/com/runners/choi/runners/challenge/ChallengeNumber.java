package com.runners.choi.runners.challenge;

public class ChallengeNumber {

    public static void main(String[] args) {

        // TODO Auto-generated method stub

    }

    public static String setNumber(String salt){

        if( salt.equals("A") ){
            return "1";
        } else if( salt.equals("B") ){
            return "2";
        } else if( salt.equals("C") ){
            return "3";
        } else if( salt.equals("D") ){
            return "4";
        } else if( salt.equals("E") ){
            return "5";
        } else if( salt.equals("F") ){
            return "6";
        } else if( salt.equals("G") ){
            return "7";
        } else if( salt.equals("H") ){
            return "8";
        } else {
            return "오류 (NUMBER CLASS)";
        }

    }

}
