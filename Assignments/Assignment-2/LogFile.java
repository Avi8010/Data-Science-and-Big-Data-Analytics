package DS1;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class LogFile {
	public static class Tokenizermapper
	   extends Mapper<Object,Text,Text,IntWritable>{
		private static final SimpleDateFormat Date_format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
			String line=value.toString();
			String[] parts=line.split("\n");
			   for (String p: parts){
				String[] eachLine=p.split(",");
				String user=eachLine[1];
				String lgtime=eachLine[5];
				String lotime=eachLine[7];
					
					try {
						Date logintime = Date_format.parse(lgtime);
						Date logouttime=Date_format.parse(lotime);
						long sessiontime=logouttime.getTime()-logintime.getTime();
						int minutes=(int)(sessiontime/(1000*60));
						context.write(new Text(user),new IntWritable(minutes));
					} catch (ParseException e) {
						e.printStackTrace();
					}
			   }
			}	
	}
	
	public static class IntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
		  public IntWritable maxtime=new IntWritable();
		  public void reduce(Text key,Iterable<IntWritable> values, Context context) throws IOException,InterruptedException{
			  int max=Integer.MIN_VALUE;
			  for (IntWritable val: values){
				  if(max<val.get()){
					  max=val.get();
				  }
			  }
			  maxtime.set(max);
			  context.write(key,maxtime);
		  }
	}
	public static void main(String[] args) throws Exception{
		Configuration c=new Configuration();
		Job j= Job.getInstance(c,"word count");
		j.setJarByClass(LogFile.class);
		j.setMapperClass(Tokenizermapper.class);
		j.setCombinerClass(IntSumReducer.class);
		j.setReducerClass(IntSumReducer.class);
		j.setOutputKeyClass(Text.class);
		j.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(j, new Path(args[0]));
		FileOutputFormat.setOutputPath(j, new Path(args[1]));
		System.exit(j.waitForCompletion(true)? 0:1);
	}

}
