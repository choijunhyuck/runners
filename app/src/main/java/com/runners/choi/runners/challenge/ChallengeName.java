package com.runners.choi.runners.challenge;

public class ChallengeName {

    public static void main(String[] args) {

        // TODO Auto-generated method stub

    }

    public static String setName(String salt){

        if( salt.equals("A") ){
            return "퀵 실버";
        } else if( salt.equals("B") ){
            return "모순덩어리";
        } else if( salt.equals("C") ){
            return "부상!";
        } else if( salt.equals("D") ){
            return "아슬아슬";
        } else if( salt.equals("E") ){
            return "케냐인";
        } else if( salt.equals("F") ){
            return "구리(COPPER)다!";
        } else if( salt.equals("G") ){
            return "은..애매한데?";
        } else if( salt.equals("H") ){
            return "황금이다!";
        } else {
            return "오류 (NAME CLASS)";
        }

    }

}
