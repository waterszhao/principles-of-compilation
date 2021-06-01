package com.waters;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

//词法分析器
//以空格和回车分割句子为一个个单词
//将根据单词的格式，将其规定映射为某种类型，映射对照表如下 type 单词种别码
//输入为文件读取 src/data.txt（现在使用的） 或 控制台输入（已经注释掉了），根据学习习惯输入字符串以#结尾
//输出为每个单词的识别结果，以行为单位

public class Lexer {
    /*  初始化数据
      token为存放的单词自身字符串；

      type为单词种别码；
      1-9 关键字
      12---浮点数
      11---整数
      10---变量
      18---{
      19---}
      26---;
      27---(
      28---)
      23--->
      20---<
      25---=
      29---==
      15---*
      13---+
      14----
      16---/
      18---:=
      0---#
     */
    private static int type;

    private static int pointerInput;//索引输入字符串 input 扫描到的位置
    private static int pointerToken;//索引识别单词字符串 token 中的字符

    static int count;
    private static List<Integer> tokens;

    //关键字表
    private final static String[] KEYWORDS = {"true", "if", "then", "else", "while", "do", "false","int","switch"};

    public static void main(String[] args) throws IOException {
        wordAnalysis();
    }

    public static void wordAnalysis() throws IOException {
        //1、输入字符串   以#结束
        //input = "int x = 0;y = 1; <int> := sd 12 as1 #";

        String input = "";
        //1. 控制台输入
        //Scanner scanner = new Scanner(System.in);
        // System.out.println("请输入扫描字符串： 以#结尾");

        //2.文件读取
        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/data.txt")),"UTF-8"));
        char buf[] = new char[1024];
        int bytes = read.read(buf);
        input = new String(buf);
        read.close();

        //循环输入分析
        //while(!(input = scanner.nextLine()).equals("exit")) {
        tokens = new ArrayList<>();
        if (input.charAt(bytes - 1) != '#'){
            System.out.println("请以#结尾");
            //continue;
            System.exit(0);
        }

        pointerInput = 0;
        count = 0;
        String result;

        do {
            char[] token = new char[20];
            scan(input, token);
            String str = new String(token).trim();

            if (type > 0 && type < 10) {
                result = "KEY WORD";
            } else if (type == 10) {
                result = "VARIABLE";
            } else if (type == 11) {
                result = "INT NUMBER";
            } else if (type == 12) {
                result = "DOUBLE NUMBER";
            }
            else if (type > 12) {
                result = "SYMBOL";
            } else if (type == 0) {
                result = "END";
            } else {
                result = "ERROR";
            }
            System.out.printf("kind: %3s  letter: %8s  , " + " type: %11s count: %d\n",type,str,result,++count);
            tokens.add(type);
        } while (type != 0);

        //System.out.println("请输入扫描字符串： 以#结尾");
        System.out.println();
        //}
    }

    public static List<Integer> getTokens() throws IOException {
        wordAnalysis();
        return tokens;
    }

    //扫描程序
    private static void scan(String input, char []token) throws IOException {
//      1、初始化
        for (int i = 0; i < token.length; i++)
            token[i] = ' ';
        pointerToken = 0;
//      2、读取首字母
        char ch = input.charAt(pointerInput++);
        while (ch == ' ' || ch == '\n' || ch == '\r') {//如果是空格或换行，则取下一个字符
            ch = input.charAt(pointerInput++);
        }
//      3、开始执行扫描
//          1、是字母
//            读标识符，查保留字表
//            查到，换成属性字表，写到输出流
//            没查到， 查名表，换成属性字，写到输出流
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
            pointerToken = 0;
            //获取完整单词
            while ( (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z')) {
                token[pointerToken++] = ch;
                ch = input.charAt(pointerInput++);
            }
            --pointerInput;
            type = 10;//单词符号为letter(letter|digit)*
            //判断是哪个关键字

            String newStr = new String(token).trim();
            for (int i = 0; i < KEYWORDS.length; i++) {
                if (newStr.equals(KEYWORDS[i])) {
                    type = i + 1;
                    break;
                }
            }
        }
//          2、是数字
//            取数字，查常量表，换成属性字表，写到输出流
        else if (ch >= '0' && ch <= '9') {
            type = 11;//digit* 数字
            while (ch >= '0' && ch <= '9') {
                token[pointerToken++] = ch;
                ch = input.charAt(pointerInput++);
            }
            if (ch == '.') { //小数点
                token[pointerToken++] = ch;
                ch = input.charAt(pointerInput++);
                if(ch >= '0' && ch <= '9'){ //小数点后跟着数字
                    type = 12;
                    while (ch >= '0' && ch <= '9'){
                        token[pointerToken++] = ch;
                        ch = input.charAt(pointerInput++);
                    }
                    pointerInput--;
                }
                else{ // 如果不是数字
                    token[--pointerToken] = ' ';
                    --pointerInput;
                }
            }else{
                pointerInput--;
            }
        }
//          3、是特殊符号  没有建立
//              查特殊符号表，换成属性字。写到输出流
//          4、错误error
//      4、是否分析结束
//              未结束，到2
//              结束，到出口
        else {
            switch (ch) {
                case '<':
                    pointerToken = 0;
                    token[pointerToken++] = ch;
                    ch = input.charAt(pointerInput++);
                    if (ch == '>') {
                        type = 21;//<>
                    } else if (ch == '=') {
                        type = 22;//<=
                        token[pointerToken++] = ch;
                    } else {
                        type = 20;//<
                        --pointerInput;
                    }
                    break;
                case '>':
                    token[pointerToken++] = ch;
                    ch = input.charAt(pointerInput++);
                    if (ch == '=') {
                        type = 24;//>=
                        token[pointerToken++] = ch;
                    } else {
                        type = 23;//>
                        --pointerInput;
                    }
                    break;
                case ':':
                    token[pointerToken++] = ch;
                    ch = input.charAt(pointerInput++);
                    if (ch == '=') {
                        type = 18;//:=
                        token[pointerToken++] = ch;
                    } else {
                        type = 17;//:
                        --pointerInput;
                    }
                    break;
                case '+':
                    type = 13;
                    token[0] = ch;
                    break;
                case '-':
                    type = 14;
                    token[0] = ch;
                    break;
                case '*':
                    type = 15;
                    token[0] = ch;
                    break;
                case '/':
                    type = 16;
                    token[0] = ch;
                    break;
                case '=':
                    token[pointerToken++] = ch;
                    ch = input.charAt(pointerInput++);
                    if (ch == '=') {
                        type = 29;//==
                        token[pointerToken++] = ch;
                    } else {
                        type = 25;//=
                        --pointerInput;
                    }
                    break;
                case ';':
                    type = 26;
                    token[0] = ch;
                    break;
                case '(':
                    type = 27;
                    token[0] = ch;
                    break;
                case ')':
                    type = 28;
                    token[0] = ch;
                    break;
                case '{':
                    type = 18;
                    token[0] = ch;
                    break;
                case '}':
                    type = 19;
                    token[0] = ch;
                    break;
                case '#':
                    type = 0;
                    token[0] = ch;
                    break;
                default:
                    token[0] = ch;
                    type = -1;
                    break;
            }
        }
    }
}