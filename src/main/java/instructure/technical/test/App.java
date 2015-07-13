package instructure.technical.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * Hello world!
 *
 */
public class App 
{
	public App() {
		try {
			Reader in = new FileReader("path/to/file.csv");
			try {
				Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
