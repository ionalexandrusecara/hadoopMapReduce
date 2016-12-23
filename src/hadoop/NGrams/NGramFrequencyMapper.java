package hadoop.NGrams;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class NGramFrequencyMapper extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, Text>{
    int n;
    String type;
    public void configure(JobConf jobConf){
        try {
            n = Integer.parseInt(jobConf.get("ngram"));
        } catch(NumberFormatException e){
            System.out.println("The value of Ngrams has to be a number!");
        }
        type=jobConf.get("type");
    }

    public void map(LongWritable key, Text value, OutputCollector<LongWritable, Text> output, Reporter reporter) throws IOException {

        String line = value.toString();
        Scanner scanner = new Scanner(line);

        // The first token on the line is the word, and the second is the count.
        String word="";
        if(type.equals("W")) {
            for (int i = 0; i < n; i++) {
                word = word + scanner.next();
                if (i < n - 1) {
                    word = word + " ";
                }
            }
        } else {
            word=scanner.next();
        }

        String n=scanner.next();
        //System.out.println("n: " + n);
        LongWritable count = new LongWritable(Integer.parseInt(n));

        // The count now becomes the key, since the data needs to be sorted by decreasing
        // count before the final reducer is applied.
        output.collect(count, new Text(word));

    }


}
