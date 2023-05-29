package DS5;
import java.io.IOException;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class movie {
	
	public static class TokenizerMapper extends Mapper <LongWritable, Text, Text, FloatWritable>{
		public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException{
			String tmp = value.toString();
			String[] lines = tmp.split("\n");
			for(String line: lines){
				String[] eachLine = line.split(",");
				String movie = eachLine[1];
				String pop = eachLine[2];
				float pop1 = Float.parseFloat(pop);
				con.write(new Text(movie),new FloatWritable(pop1));
			}
		}
	}
	
	public static class IntSumReducer extends Reducer <Text,FloatWritable,Text,FloatWritable>{
		
		public void reduce(Text key,Iterable<FloatWritable> values, Context con) throws IOException, InterruptedException{
			float count = 0;
			for(FloatWritable val: values){
				count = Math.max(count,val.get());
			}
			con.write(key, new FloatWritable(count));
		}
	}
	public static void main(String args[]) throws Exception{
		Configuration c = new Configuration();
		String[] files = new GenericOptionsParser(c,args).getRemainingArgs();
		Path input = new Path(files[0]);
		Path output = new Path(files[1]);
		Job j = Job.getInstance(c, "wordcount");
		j.setJarByClass(movie.class);
		j.setMapperClass(TokenizerMapper.class);
		j.setReducerClass(IntSumReducer.class);
		j.setOutputKeyClass(Text.class);
		j.setOutputValueClass(FloatWritable.class);
		FileInputFormat.addInputPath(j, input);
		FileOutputFormat.setOutputPath(j, output);
		System.exit(j.waitForCompletion(true)?0:1);
	}

}
