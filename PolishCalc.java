package com.company;

import java.util.*;
import java.lang.*;


public class PolishCalc {
    public static Double calc(List<String> expression) {
        Deque<Double> stack = new ArrayDeque<Double>();
        for (String x : expression) {
            switch (x) {
                case "+":
                    stack.push(stack.pop() + stack.pop());
                    break;
                case "-": {
                    Double b = stack.pop(), a = stack.pop();
                    stack.push(a - b);
                    break;
                }
                case "*":
                    stack.push(stack.pop() * stack.pop());
                    break;
                case "/": {
                    Double b = stack.pop(), a = stack.pop();
                    stack.push(a / b);
                    break;
                }
                case "-u":
                    stack.push(-stack.pop());
                    break;
                default:
                    if (x.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
                        stack.push(Double.valueOf(x));
                    else {
                        System.out.println("Bad Input");
                        return 0.0;
                    }
                    break;
            }
        }
        return stack.pop();
    }

    public static Double calc() {
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        List<String> expression = PolskaParser.getParsedStr(s);
        for (String x : expression) System.out.print(x + " ");  //RPN output
        if (!expression.contains("Error")) {
            System.out.println();
            System.out.println(calc(expression));               //Result output
            return calc(expression);
        }
        return null;
    }
}

class PolskaParser {
    private static final String operations = "+-*/";
    private static final String delimiters = "()" + operations;

    private static boolean isDelimiter(String token) {
        return delimiters.contains(token);
    }

    private static boolean isOperator(String token) {
        return operations.contains(token);
    }

    private static int getPriority(String token) {
        return "(+-*/)".indexOf(token);
    }

    private static String clearString(String s) {
        return s.replaceAll("[a-zA-Z ]", "");
    }

    private static boolean isDigit(String str) {
        return Character.isDigit(str.charAt(0));
    }

    private static List<String> getErrorRes(){
        return List.of("Error");
    }

    public static List<String> getParsedStr(String arg) {
        List<String> result = new ArrayList<String>();
        Deque<String> stack = new ArrayDeque<String>();
        String s = clearString(arg);
        StringTokenizer tokenizer = new StringTokenizer(s, delimiters, true);
        String curr = "";
        while (tokenizer.hasMoreTokens()) {
            curr = tokenizer.nextToken();
            if (isDigit(curr)) result.add(curr);
            if (curr.equals("(")) stack.push("(");
            if (curr.equals(")")){
                stack.remove("(");
                while (stack.peek() != null && !stack.peek().equals("("))
                    result.add(stack.pop());
            }
            if (isOperator(curr)) {
                while (!stack.isEmpty() && (getPriority(curr) <= getPriority(stack.peek()))) {
                    result.add(stack.pop());
                }
                stack.push(curr);
            }
        }
        while (!stack.isEmpty()) {
            if (isOperator(stack.peek())) result.add(stack.pop());
            else {
                System.out.println("Bad input");
                return getErrorRes();
            }
        }
        return result;
    }
}
