package com.waters;

//语法（文法）分析器，判断输入的句子是否为该文法的句子。
//本分析器用于检测if()then{}else{}矩形，支持if和else嵌套
//一个正例可参考 src/data.txt
//根据词法分析的结果，将识别到的单词类型的映射结果作为输入
//输出为 TRUE or FALSE

/* 文法
    (1) S->if (A) then F else F             开始符
    (2) A->CDC                              判断表达式
    (3) B->E|SE                             语句块
    (4) C->variable | int | double | CGC    数值或变量或表达式
    (5) D->==|<|>                           判断符号
    (6) E->variable=C;E|#                   表达式
    (7) F->{B}                              语句块
    (8) G->+|-|*|/                          运算符
*/

import java.io.*;

public class MyParser {

    static String lookAhead;
    static Integer[] tokens;
    static int pointer;

    // S->if (A) then F else F     开始符
    static void parseS(){
        if(tokens[pointer++] == 2) { // if
            if(tokens[pointer++] == 27) { // (
                parseA();
                if(tokens[pointer++] == 28) { // )
                    if(tokens[pointer++] == 3) { // then
                        parseF();
                        if(tokens[pointer++] == 4) { // else
                            parseF();
                        }
                        else {
                            System.out.println("分析S时错误，缺少 else");
                            System.exit(pointer);
                        }
                    }
                    else {
                        System.out.println("分析S时错误，缺少 then");
                        System.exit(pointer);
                    }
                }
                else {
                    System.out.println("分析S时错误，缺少 )");
                    System.exit(pointer);
                }
            }
            else {
                System.out.println("分析S时错误，缺少 (");
                System.exit(pointer);
            }
        }
        else {
            System.out.println("分析S时错误，缺少 if");
            System.exit(pointer);
        }
    }

    //(2) A->CDC                           判断表达式
    static void parseA(){
        parseC();
        parseD();
        parseC();
    }

    //(3) B->E|SE                           语句块
    static void parseB(){
        switch (tokens[pointer]){
            case 10: // variable =>  B->E
            case 19: // } =>  B->E
                parseE();
                break;
            case 2: // if => B->SE
                parseS();
                parseE();
                break;
            default:
                System.out.println("分析B时错误，缺少 variable 或 if");
                System.exit(pointer);
        }
    }

    // (4) C->variable | int | double | CGC      数值，变量或表达式
    //消除左递归: C->variableC1 | intC1 | doubleC1
    static void parseC(){
        switch (tokens[pointer++]){
            case 10: // variable
            case 11: // int number
            case 12: // double number
                parseC1();
                break;
            default:
                System.out.println("分析C时错误，缺少 variable, int number 或 double number");
                System.exit(pointer);
        }
    }

    // C1->GCC1 | #
    static void parseC1(){
        if( tokens[pointer] <= 16 && tokens[pointer] >=13 ) { // +, -, *, / => C1->GCC1
            parseG();
            parseC();
            parseC1();
        }
        else if(tokens[pointer] == 29 || tokens[pointer] == 23 ||tokens[pointer] == 20 ||
                tokens[pointer] == 28 || tokens[pointer] == 26 ){ // ==, <, >, ), ; => C1->#

        }
        else {
            System.out.println("分析C1时错误，缺少 +, -, * 或 /");
            System.exit(pointer);
        }
    }

    // (5) D->==|<|>                       判断符号
    static void parseD(){
        switch (tokens[pointer++]){
            case 29: // ==
                break;
            case 20: // <
                break;
            case 23: // >
                break;
            default:
                System.out.println("分析D时错误，缺少 ==, < 或 >");
                System.exit(pointer);
        }
    }

    // (6) E->variable=C;E|#               表达式
    static void parseE(){
        if(tokens[pointer] == 10) { // variable => E->variable=C;E
            pointer++;
            if(tokens[pointer++] == 25) { // =
                parseC();
                if(tokens[pointer++] == 26) { // ;
                    parseE();
                }
                else {
                    System.out.println("分析E时错误，缺少 ;");
                    System.exit(pointer);
                }
            }
            else {
                System.out.println("分析E时错误，缺少 =");
                System.exit(pointer);
            }
        }
        else if(tokens[pointer] == 19){ // } => E->#

        }
        else {
            System.out.println("分析E时错误，缺少 variable 或 }");
            System.exit(pointer);
        }
    }

    //(7) F->{B}                          语句块
    private static void parseF() {
        if(tokens[pointer++] == 18) { // {
            parseB();
            if(tokens[pointer++] == 19) { // }

            }
            else {
                System.out.println("分析F时错误，缺少 }");
                System.exit(pointer);
            }
        }
        else {
            System.out.println("分析F时错误，缺少 {");
            System.exit(pointer);
        }
    }

    //(8) G->+|-|*|/                          运算符
    static void parseG(){
        switch (tokens[pointer++]){
            case 13: // +
            case 14: // -
            case 15: // *
            case 16: // /
                break;
            default:
                System.out.println("分析G时错误，缺少 +, -, * 或 /");
                System.exit(pointer);
        }
    }

    public static void main(String[] args) throws IOException {
        pointer = 0;
        tokens = Lexer.getTokens().toArray(new Integer[0]);

        parseS();
        System.out.println("分析成功，该语句是文法的句子");
    }

}
