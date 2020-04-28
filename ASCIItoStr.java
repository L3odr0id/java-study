package com.company;

import java.util.HashMap;
import java.util.Scanner;

public class ASCIItoStr {

    public static void convert() {
        Scanner in = new Scanner(System.in);
        String name = in.nextLine();

        HashMap<String, EmptySymbol> lettersMap  = new HashMap<>() {{
            //Put here new letters!
            put("l", new L()); put("o", new O()); put(" ", new Space()); put("k", new K()); put("e", new E());
        }};

        EmptySymbol[] objects = new EmptySymbol[name.length()];
        for (int i = 0;i<name.length();++i) {
            EmptySymbol temp = lettersMap.get(String.valueOf(name.charAt(i)));
            objects[i] = temp != null ? temp : new Space();
        }

        for (int i=0;i<5;++i) {
            for (int j = 0; j < name.length(); ++j)
                System.out.print(objects[j].getString(i));
            System.out.println();
        }
    }
}

abstract class EmptySymbol{
    String[] arr = new String[5];

    public String getString(int row_num) {
        StringBuilder result = new StringBuilder();
        for (int i=0;i<5;++i)
            result.append(arr[row_num].charAt(i));
        return result.toString()+" ";
    }
}

// Add new letters here

class L extends EmptySymbol{
    L(){
        arr = new String[]{
                " #   ",
                " #   ",
                " #   ",
                " #   ",
                " ### "
        };
    }
}

class O extends EmptySymbol{
    O(){
        arr = new String[]{
                "  #  ",
                " # # ",
                "#   #",
                " # # ",
                "  #  "
        };
    }
}

class K extends EmptySymbol{
    K(){
        arr = new String[]{
                "#   #",
                "#  # ",
                "# #  ",
                "# #  ",
                "#  # "
        };
    }
}

class E extends EmptySymbol{
    E(){
        arr = new String[]{
                " ### ",
                " #   ",
                " ### ",
                " #   ",
                " ### "
        };
    }
}

class Space extends EmptySymbol{
    Space(){
        arr = new String[]{
                "     ",
                "     ",
                "     ",
                "     ",
                "     "
        };
    }
}
