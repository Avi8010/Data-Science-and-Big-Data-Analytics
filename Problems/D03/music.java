package DS3;
import java.io.IOException;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class music {
	
	public static class TokenizerMapper extends Mapper <LongWritable, Text, Text, IntWritable>{
		public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException{
			String tmp = value.toString();
			String[] lines = tmp.split("\n");
			for(String line: lines){
				String[] eachLine = line.split(",");
				String userId=eachLine[0];
				String trackId = eachLine[1];
				String isShared = eachLine[2];
				if(Integer.parseInt(isShared) != 0){
					con.write(new Text(trackId), new IntWritable(1));
				}
				con.write(new Text(userId),new IntWritable(0));

			}
		}
	}
	
	public static class IntSumReducer extends Reducer <Text,IntWritable,Text,IntWritable>{
		
		public void reduce(Text key,Iterable<IntWritable> values, Context con) throws IOException, InterruptedException{
			int count = 0;
			for(IntWritable val: values){
				count += val.get();
			}
			con.write(key, new IntWritable(count));
		}
	}
	public static void main(String args[]) throws Exception{
		Configuration c = new Configuration();
		String[] files = new GenericOptionsParser(c,args).getRemainingArgs();
		Path input = new Path(files[0]);
		Path output = new Path(files[1]);
		Job j = Job.getInstance(c, "wordcount");
		j.setJarByClass(music.class);
		j.setMapperClass(TokenizerMapper.class);
		j.setReducerClass(IntSumReducer.class);
		j.setOutputKeyClass(Text.class);
		j.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(j, input);
		FileOutputFormat.setOutputPath(j, output);
		System.exit(j.waitForCompletion(true)?0:1);
	}

}
