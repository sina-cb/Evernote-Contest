import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;


public class InputCreator {

	public static void main(String[] args) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/sina/Desktop/output.txt")));
		
		for (int i = 0; i < 10000; i++){
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	        format.setTimeZone(TimeZone.getTimeZone("UTC"));
	        String date = format.format(Calendar.getInstance().getTime()).substring(0, 19) + "Z";

	        Random rand = new Random();
	        
			bw.write("CREATE\n");
			bw.write("<note>\n");
			bw.write("<guid>" + date + (rand.nextInt(10000000)) + "</guid>\n");
			bw.write("<created>" + date + "</created>\n");
			int len;
			for (int j = 0; j < 10; j++){
				len = rand.nextInt(10);
				
				String temp = "tag";
				for (int k = 0; k < len; k++){
					temp = temp + rand.nextInt(100);
				}
				
				bw.write("<tag>" + temp + "</tag>\n");
			}
			
			len = rand.nextInt(100);
			String temp = "content";
			for (int k = 0; k < len; k++){
				temp = temp + rand.nextInt(100);
			}
			
			bw.write("<content>\n" + temp + "\n</content>\n");
			bw.write("</note>\n");
			
		}
		
		bw.close();
		
		System.out.println("done");
	}
	
}
