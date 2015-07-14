package instructure.technical.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Hello world!
 *
 */
public class App 
{
	private final Map<String, Object> propsMap = new HashMap<String, Object>();
	private List<Map<String, Object>> studentsList = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> coursesList = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> enrollmentList = new ArrayList<Map<String, Object>>();
	public App() {
		Gson gson = new Gson();
		try {
			getPropertiesFromClasspath("app.properties.json");
			String InputFilesDirectory=(String) propsMap.get("InputFilesDirectory");
			System.out.println("InputFilesDirectory = "+InputFilesDirectory);
			File[] files = new File(InputFilesDirectory).listFiles();
			for (File file : files) {
				Reader in = new FileReader(file);
				try {
					Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
					for (CSVRecord record : records) {
						
						HashMap<String, Object> recordHm=gson.fromJson(gson.toJson(record.toMap()),new TypeToken<HashMap<String, Object>>() {
						}.getType());;
						System.out.println("****** Record *******");
						System.out.println(gson.toJson(recordHm));
						if(recordHm.get("state")!=null&&recordHm.get("state").equals("active")) {
							if(recordHm.get("user_name")!=null){
								studentsList.add(recordHm);
							} else if(recordHm.get("course_name")!=null) {
								coursesList.add(recordHm);
							} else {
								enrollmentList.add(recordHm);
							}
						}
					    
					}
					System.out.println("****** Students *******");
					System.out.println(gson.toJson(studentsList));
					System.out.println("****** Courses *******");
					System.out.println(gson.toJson(coursesList));
					System.out.println("****** Enrollment *******");
					System.out.println(gson.toJson(enrollmentList));
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public static void main( String[] args )
    {
       App app = new App();
    }
    
    /**
     * loads properties file from classpath
     * 
     * @param propFileName
     *          the property file name
     * @return the properties if found.
     */
    private synchronized void getPropertiesFromClasspath(String propFileName) {

      try {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream == null) {
          System.out.println("property file '" + propFileName + "' not found in the classpath");
        } else {

          Type typ = new TypeToken<Map<String, Object>>() {
          }.getType();
          JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

          // read input file as lenient JSON with C-style comments
          reader.setLenient(true);
          Map<String, Object> myProps = new Gson().fromJson(reader, typ);

          propsMap.putAll(myProps);
        
        
         
        }

      } catch (Throwable ex) {
      
        System.out.println("Error Loading property file'" + propFileName + "'.");
        ex.printStackTrace();
      }
    }
}
