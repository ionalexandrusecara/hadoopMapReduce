package hadoop.NGrams;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class NGramAlphabeticalMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, LongWritable> {

    // The output of the mapper is a map from words (including duplicates) to the value 1.

    ArrayList<String> ngrams = new ArrayList();
    ArrayList<String> lines=new ArrayList();

    String language=null;
    String type=null;
    int n=0;
    String order;

    @Override
    public void configure(JobConf jobConf){
        language=jobConf.get("lang");
        try {
            n = Integer.parseInt(jobConf.get("ngram"));
        } catch(NumberFormatException e){
            System.out.println("The value of Ngrams has to be a number!");
        }
        type=jobConf.get("type");
        order=jobConf.get("order");
    }


    public void map(LongWritable key, Text value, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException {

        // The key is the character offset within the file of the start of the line, ignored.
        // The value is a line from the file.
        //System.out.println("HERE");
        String line = value.toString();

        if(line.indexOf("</tmx>")!=-1){
            NGramCount.ready=true;
        }
        //System.out.println("line: " + line);
        Scanner scanner = null;
        //Checking whether tag is for correct language
        if (line.indexOf("<tuv") != -1) {
            //System.out.println("HERE");
            if (line.indexOf("lang=\"" + language + "\"") != -1) {
                NGramCount.langFound = true;
                //System.out.println("HERE2");
            } else {
                NGramCount.langFound = false;
            }
        }

        if (NGramCount.langFound == true && line.indexOf("</tuv>") != -1) {
            NGramCount.langFound = false;
        }

        //System.out.println("langFound: " + NGramCount.langFound);


        if (NGramCount.langFound == true && line.indexOf("<seg>") != -1) {
            NGramCount.segFirst = true;
        }

        if (NGramCount.langFound == true && NGramCount.segFirst == true) {
            //System.out.println("langFound: " + NGramCount.langFound);
            //System.out.println("segFirst: " + NGramCount.segFirst);
            //System.out.println("line " + line);
            line = line.substring(5);
            line = line.substring(0, line.indexOf("</seg>"));
            NGramCount.segFirst = false;
            lines.add(line);
        }

        String allLines = "";
        if(NGramCount.ready==true) {
            for (int index = 0; index < lines.size(); index++) {
                allLines = allLines + lines.get(index);
                if (index < lines.size() - 1) {
                    allLines = allLines + " ";
                }
            }
        }
        if (NGramCount.ready == true && type.equals("W")) { // Word NGrams
             //group lines together - ngrams per file
            NGramCount.ready = false;
            allLines=allLines.replaceAll("[^a-zA-Z ]","");
            allLines=allLines.toLowerCase();
            scanner = new Scanner(allLines);
            String word;
            String ngram = "";
            for (int i = 1; i <= n; i++) {
                word = scanner.next();
                //System.out.println("word: " + word);
                ngrams.add(word);
            }
            for (int i = 0; i < ngrams.size(); i++) {
                ngram = ngram + ngrams.get(i);
                if (i < ngrams.size() - 1) {
                    ngram = ngram + " ";
                }
            }
            output.collect(new Text(ngram), new LongWritable(1)); //output first word ngram

            while (scanner.hasNext()) {
                for (int i = 1; i < n; i++) {
                    ngrams.set(i - 1, ngrams.get(i));
                }
                word = scanner.next();
                ngrams.set(n - 1, word);
                ngram = "";
                for (int i = 0; i < ngrams.size(); i++) {
                    ngram = ngram + ngrams.get(i);
                    if (i < ngrams.size() - 1) {
                        ngram = ngram + " ";
                    }
                }
                output.collect(new Text(ngram), new LongWritable(1)); //output the rest of word ngrams
            }
        }
        if(NGramCount.ready == true && type.equals("C")){ // Character NGrams
            //group lines together - ngrams per file
            NGramCount.ready = false;
            allLines=allLines.replaceAll("[^a-zA-Z ]","");
            String str;
            int i = 0;
            while (i < allLines.length()) {
                if (isNumber(allLines.charAt(i))) {
                    allLines = ignoreNumberAndPunctuation(allLines, i);
                    i--;
                } else if (isPunctuation(allLines.charAt(i))) {
                    allLines = ignoreNumberAndPunctuation(allLines, i);
                    i--;
                } else if (Character.isUpperCase(allLines.charAt(i))) {
                    allLines = fromUpperToLowerCase(allLines, allLines.charAt(i), i);
                } else if (isEndOfWord(allLines.charAt(i))) {
                    allLines = showEndOfWord(allLines, i);
                }
                i++;
            }
            i = 0;
            boolean ok;
            while (i <= allLines.length() - n) {
                ok = true;
                str = allLines.substring(i, i + n);
                for (int j = 0; j <= n - 2; j++) {
                    if (str.charAt(j) == '_') {
                        ok = false;
                    }
                }
                if (ok) {
                    ngrams.add(str);
                }
                i++;
            }
            for(int index=0;index<ngrams.size();index++){
                output.collect(new Text(ngrams.get(index)), new LongWritable(1));
            }
        }
    }

    public static boolean isNumber(char ch) {
        if (Character.isDigit(ch)) {
            return true;
        }
        return false;
    }

    public boolean isPunctuation(char ch) {
        if (ch == '!' || ch == '?' || ch == '.' || ch == ',' || ch == '&' || ch == '@'
                || ch == '#' || ch == '$' || ch == '%' || ch == '^' || ch == '*' || ch == ':'
                || ch == '/' || ch == ';' || ch == '<' || ch == '>') {
            return true;
        }
        return false;
    }

    public boolean isEndOfWord(char ch) {
        if (ch == ' ') {
            return true;
        }
        return false;
    }

    public String ignoreNumberAndPunctuation(String contents, int index) {
        contents = contents.substring(0, index) + contents.substring(index + 1);
        return contents;
    }

    public String showEndOfWord(String contents, int index) {
        contents = contents.substring(0, index) + "_" + contents.substring(index + 1);
        return contents;
    }

    public String fromUpperToLowerCase(String contents, char ch, int index) {
        ch = (char) (ch + 32);
        contents = contents.substring(0, index) + ch + contents.substring(index + 1);
        return contents;
    }
}
