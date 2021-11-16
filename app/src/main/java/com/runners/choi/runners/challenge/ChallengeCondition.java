package com.runners.choi.runners.challenge;

public class ChallengeCondition {

    public static void main(String[] args) {

        // TODO Auto-generated method stub

    }

    public static String setCondition(String salt){

        if( salt.equals("A") ){
            return "시속 347358960\nkm/h 이상";
        } else if( salt.equals("B") ){
            return "오차보너스 250초 이상";
        } else if( salt.equals("C") ){
            return "100초 이상 남은 상태에서 중단";
        } else if( salt.equals("D") ){
            return "원에서 탈출하기 직전 게임오버";
        } else if( salt.equals("E") ){
            return "시속보너스 50 이상";
        } else if( salt.equals("F") ){
            return "브론즈 티켓 획득";
        } else if( salt.equals("G") ){
            return "실버 티켓 획득";
        } else if( salt.equals("H") ){
            return "골드 티켓 획득";
        } else {
            return "오류 (CONDITION CLASS)";
        }

    }

}
