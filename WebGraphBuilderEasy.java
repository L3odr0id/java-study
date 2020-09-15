package com.company;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebGraphBuilderEasy {

    public static void main(String[] args) {
        build("https://student.mirea.ru/media/photo/");
    }

    public static void build(String url){
        Element doc = getPage(url);
        String rootName = getRootName(url);

        DependenciesFinder df = new DependenciesFinder(doc, url, rootName);
        GraphvizWriter writer = new GraphvizWriter();
        writer.setMode(1);
        writer.openFile();
        writer.doLinks(rootName);
        writer.doWriting(df.getAllMatches());
        writer.makePicture();
    }


    private static String getRootName(String str){
        Pattern p = Pattern.compile("[htps:]*\\/\\/([a-zA-Z0-9.]*)\\/");
        Matcher m = p.matcher(str);
        if (m.find())
            return m.group(1).replace(".","_");
        else
            return "Root";
    }

    public static Element getPage(String url){
        Element doc = null;
        try {
            doc = Jsoup.connect(url).get();
            System.out.println("[+] Got page by URL "+url);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[-] ERROR: Can't get page by URL.");
        }
        return doc;
    }

    public static class GraphvizWriter{
        private String fileName;
        private int mode=0;

        public GraphvizWriter(){
            fileName = "result.dot";
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setMode(int mode){
            this.mode = mode;
        }

        public boolean doLinks(String root){
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

                writer.append(root).append(" [shape=box];").append("\n");

                writer.close();
                return true;
            }catch (Exception e){
                System.out.println("[-] Writing in file "+fileName+" is failed!");
                return false;
            }
        }

        public boolean doWriting(List<WebGraphBuilderEasy.Dependency> dependencies){
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

                for (WebGraphBuilderEasy.Dependency dependency : dependencies) {
                    String strToWrite;
                    switch (mode) {
                        case 0: { strToWrite = dependency.toString();break; }
                        case 1: { strToWrite = getSmartStr(dependency);break; }
                        default: strToWrite = "";
                    }
                    writer.append(strToWrite).append("\n");
                }
                writer.append("}");

                writer.close();
                System.out.println("[+] Written in file "+fileName);
                return true;
            }catch (Exception e){
                System.out.println("[-] Writing in file "+fileName+" is failed!");
                return false;
            }
        }

        public boolean openFile(){
            try {
                Files.deleteIfExists(Paths.get(fileName));
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

                writer.append("digraph WebGraphBuilderEasy {").append("\n");
                writer.close();
                return true;
            }catch (Exception e){
                return false;
            }
        }

        public void makePicture() {
            try{
                Runtime.getRuntime().exec("dot -Tps "+fileName+" -o outfile.ps");
                System.out.println("[+] Picture is made");
            }catch (IOException e){
                System.out.println("[-] Picture making process is failed");
            }
        }

        private String getSmartStr(WebGraphBuilderEasy.Dependency dependency){
            String colors;
            switch (dependency.type){
                case 0: { colors = "gold1"; break; }
                case 1: { colors = "deepskyblue"; break; }
                default: colors = "none";
            }
            String toPrint = "node [shape=box,style=filled,color=\""+colors+"\"];\n";
            return toPrint+ dependency.toString();
        }
    }

    public static class DependenciesFinder{
        Element doc;
        String startUrl;
        List<WebGraphBuilderEasy.Dependency> allMatches = new ArrayList<>();
        String baseName;
        public DependenciesFinder(Element doc, String url, String baseName){
            this.doc = doc;
            this.baseName = baseName;
            setStartUrl(url);
            parseDependencies();
            deleteSame();
        }

        private void setStartUrl(String url){
            Pattern p = Pattern.compile("[htps:]*\\/\\/[a-zA-Z0-9.]*");
            Matcher m = p.matcher(url);
            if (m.find())
                startUrl = m.group();
            else {
                System.out.println("[-] Base address bad recognition. Urls may not work.");
                startUrl = url;
            }
        }

        private void deleteSame(){
            for (int i=0;i<allMatches.size()-1;++i){
                String tmp = allMatches.get(i).name;
                for (int j=i+1;j<allMatches.size();++j)
                    if (allMatches.get(j).name.equals(tmp)) {
                        allMatches.remove(j);
                        j=i+1;
                    }
            }
        }

        private void parseDependencies(){
            Pattern p = Pattern.compile("(href|src)=\"(\\/[a-zA-Z\\/?0-9]*\\..[a-zA-Z?\\/0-9.]*)\"");
            Matcher m = p.matcher(doc.toString());
            while (m.find()) {
                WebGraphBuilderEasy.Dependency tmp = new WebGraphBuilderEasy.Dependency(startUrl+m.group(2), baseName);
                if (tmp.type == 1 || tmp.type == 0)
                    allMatches.add(tmp);
            }
            System.out.println("[+] "+allMatches.size()+" dependencies found.");
        }

        public List<WebGraphBuilderEasy.Dependency> getAllMatches(){
            return allMatches;
        }
    }

    public static class Dependency{
        String name;
        String url;
        int type;
        String base;
        Dependency(String fullUrl, String base){
            url = fullUrl;
            this.base = base;
            name = getName(fullUrl);
            type = getType(fullUrl);
        }

        private String getName(String url){
            Pattern p = Pattern.compile("\\/([a-zA-Z0-9_\\.]*)[?_0-9]*$");
            Matcher m = p.matcher(url);
            if (m.find())
                return m.group(1);
            else
                return "error";
        }

        private int getType(String url){
            Pattern p = Pattern.compile("[a-zA-Z0-9\\.]*\\.([a-zA-Z]*)([?0-9]*|)$");
            Matcher m = p.matcher(url);
            if (m.find())
                return strToIntType(m.group(1));
            return -1;
        }

        private int strToIntType(String type){
            type = type.replace(".", "");
            switch (type){
                case "js":{ return 0; }
                case "css":{ return 1; }
                case "jpg":{ return 2; }
                case "ico":{ return 3; }
                case "png":{ return 4; }
                case "jpeg":{ return 5; }
                case "gif":{ return 6; }
                default:
                    return -1;
            }
        }

        @Override
        public String toString() {
            String newBase = base.replace(".", "_");
            String newName = name.replace(".", "_");
            return newBase+" -> "+newName+";";
        }
    }
}
