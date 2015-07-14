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
	private Map<String, Object> activeStudentsMap = new HashMap<String, Object>();
	private Map<String, Object> activeCoursesMap = new HashMap<String, Object>();
	private List<Map<String, Object>> activeEnrollmentList = new ArrayList<Map<String, Object>>();
	private Map<String, List<Object>> activeStudentsEnrolledInCoursesMap = new HashMap<String, List<Object>>();
	
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
								activeStudentsMap.put((String) recordHm.get("user_id"),recordHm.get("user_name"));
							} else if(recordHm.get("course_name")!=null) {
								activeCoursesMap.put((String) recordHm.get("course_id"),recordHm.get("course_name"));
							} else {
								activeEnrollmentList.add(recordHm);
							}
						}
					    
					}
					System.out.println("****** Students *******");
					System.out.println(gson.toJson(activeStudentsMap));
					System.out.println("****** Courses *******");
					System.out.println(gson.toJson(activeCoursesMap));
					System.out.println("****** Enrollment *******");
					System.out.println(gson.toJson(activeEnrollmentList));
					for(Map enrollmentMap:activeEnrollmentList){
						
						
					}
					
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
