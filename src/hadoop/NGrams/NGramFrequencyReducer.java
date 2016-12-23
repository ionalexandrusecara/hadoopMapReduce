package hadoop.NGrams;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

public class NGramFrequencyReducer extends MapReduceBase implements Reducer<LongWritable, Text, Text, LongWritable> {

    // The output of the reducer is the solely the first value/key pair encountered, swapped.

    // Use of this flag to detect the first call relies on all reduction taking place on the same node.
    // It wouldn't work if more than one Java program was performing reduction.
    // It is also not thread safe.

    public void reduce(LongWritable key, Iterator<Text> values, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException {

        while(values.hasNext()){
            output.collect(values.next(), key);
        }
    }
}
