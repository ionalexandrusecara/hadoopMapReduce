package hadoop.NGrams;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class NGramCount {
    static boolean langFound=false;
    static boolean segFirst=false;
    static boolean ready=false;
    static String language;
    static String type;
    static int n;
    static String order; //Extension-Ordering of Ngrams (Either Alphabetical order or frequency based order)
    static String input;
    static String intermediate;
    static String output;
    public static void main(String[] args) throws IOException {
        language=getLanguage();
        type=getType();
        n=getNGram();
        order=getOrder(); //Extension-Ordering of Ngrams (Either Alphabetical order or frequency based order)

        // Output from reducer maps words to counts.


        if(order.equals("A")) {
            alphabeticalOrder();
        } else {
            prepareFrequencyOrder();
            frequencyOrder();
        }
    }

    public static void alphabeticalOrder() throws IOException{
        JobConf job = new JobConf();

        input=getInput();
        output = getOutput();

        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        job.set("lang", language);
        job.set("ngram", n + "");
        job.set("type", type);
        job.set("order", order);

        // Output from reducer maps words to counts.
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        job.setMapperClass(NGramAlphabeticalMapper.class);

        // The output of the reducer is the solely the first value/key pair encountered, swapped.
        // Since keys are ordered in decreasing order, the first is the most popular.
        job.setReducerClass(NGramAlphabeticalReducer.class);
        try {
            JobClient.runJob(job);
        } catch(InvalidInputException e){
            System.out.println("Your input file does not exist!");
        }

    }

    public static void prepareFrequencyOrder() throws IOException{
        JobConf job = new JobConf();

        input=getInput();
        intermediate = "intermediate";
        output = getOutput();

        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(intermediate));

        job.set("lang", language);
        job.set("ngram", n + "");
        job.set("type", type);
        job.set("order", order);

        // Output from reducer maps words to counts.
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        job.setMapperClass(NGramAlphabeticalMapper.class);

        // The output of the reducer is the solely the first value/key pair encountered, swapped.
        // Since keys are ordered in decreasing order, the first is the most popular.
        job.setReducerClass(NGramAlphabeticalReducer.class);

        try {
            JobClient.runJob(job);
        } catch(InvalidInputException e){
            System.out.println("Your input file does not exist!");
        }
    }

    public static void frequencyOrder() throws IOException{
        JobConf job = new JobConf();
        FileInputFormat.setInputPaths(job, new Path(intermediate));
        FileOutputFormat.setOutputPath(job, new Path(output));

        job.set("lang", language);
        job.set("ngram", n + "");
        job.set("type", type);
        job.set("order", order);

        // Output from reducer maps words to counts.
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        // Output from reducer maps words to counts.
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        // The output of the mapper is a map from counts (including duplicates) to words.
        job.setMapperClass(NGramFrequencyMapper.class);

        // The output of the reducer is the solely the first value/key pair encountered, swapped.
        // Since keys are ordered in decreasing order, the first is the most popular.
        job.setReducerClass(NGramFrequencyReducer.class);
        job.setOutputKeyComparatorClass(LongWritable.DecreasingComparator.class);

        try {
            JobClient.runJob(job);
        } catch(InvalidInputException e){
            System.out.println("Unexpected Problem (Input file not correct)!");
        }

        File dir = new File(intermediate);

        if(dir.exists()) {
            String[] files = dir.list();
            for (String file : files) {
                File current = new File(dir.getPath(), file);
                current.delete();
            }
            dir.delete();
        }
    }

    public static String getInput(){
        String input = null;
        Scanner scanner=new Scanner(System.in);
        while(input==null){
            System.out.println("Enter the input directory: ");
            input=scanner.next();
        }
        return input;
    }

    public static String getOutput(){
        String output = "intermediate";
        Scanner scanner=new Scanner(System.in);
        while(output.equals("intermediate")){
            System.out.println("Enter the output directory: ");
            output=scanner.next();
        }
        return output;
    }

    public static String getLanguage(){
        String language=null;
        Scanner scanner=new Scanner(System.in);
        while(language==null){
            System.out.println("Enter the language: ");
            language=scanner.next();
        }
        return language;
    }

    public static String getType(){
        String type="A";
        Scanner scanner=new Scanner(System.in);
        while(!(type.equals("C")) && !(type.equals("W"))){
            System.out.println("Type of n-gram (C)haracter or (W)ord: : ");
            type=scanner.next();
        }
        return type;
    }

    public static int getNGram(){
        int n=0;
        Scanner scanner=new Scanner(System.in);
        while(n<2){
            System.out.println("Value of N for n-grams: ");
            try {
                n = scanner.nextInt();
            } catch(InputMismatchException e){
                System.out.println("Please writ a number for ngrams size!");
                System.exit(1);
            }
        }
        return n;
    }

    public static String getOrder(){
        String choice="B";
        Scanner scanner=new Scanner(System.in);
        while(!(choice.equals("A")) && !(choice.equals("F"))){
            System.out.println("Ordering of ngrams (A)lphabetical or (F)requency: : ");
            choice=scanner.next();
        }
        return choice;
    }
}
