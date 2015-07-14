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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * This application reads in 3 different types of csv files Students, Courses,
 * and Enrollments. Enrollments is a join between Students and Courses.
 * 
 * student columns: user_id, user_name, state course columns: course_id,
 * course_name, state enrollment columns: user_id, course_id, state
 * 
 * when done processing it system outs a list of active courses, and for each
 * course a list of active students with active enrollments in that course.
 */
public class ActiveCourseList {
	private static final boolean debug = false;
	private final Map<String, Object> propsMap = new HashMap<String, Object>();
	private Map<String, String> activeStudentsMap = new HashMap<String, String>();
	private Map<String, String> activeCoursesMap = new HashMap<String, String>();
	private List<Map<String, List<String>>> activeCoursesList = new ArrayList<Map<String, List<String>>>();
	private List<Map<String, String>> activeEnrollmentList = new ArrayList<Map<String, String>>();
	private Map<String, List<String>> activeStudentsEnrolledInCoursesMap = new HashMap<String, List<String>>();

	/**
	 * default constructor is called by main method to execute program
	 */
	public ActiveCourseList() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try {
			getPropertiesFromClasspath("app.properties.json");
			String InputFilesDirectory = (String) propsMap
					.get("InputFilesDirectory");
			if (debug) {
				System.out.println("InputFilesDirectory = "
						+ InputFilesDirectory);
			}
			iterateThroughFiles(gson, InputFilesDirectory);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buildActiveStudentsEnrolledInCoursesList(gson);
		if (debug) {
			System.out.println("****** ActiveStudentsEnrolled *******");
			System.out.println(gson.toJson(activeStudentsEnrolledInCoursesMap));
		}
		if (debug) {
			System.out.println(gson.toJson(activeCoursesMap));
		}
		buildActiveCourseList();
		sortActiveCourseList();
		if (debug) {
			System.out
					.println("****** List of active Courses with list of active students in each course *******");
		}
		System.out.println(gson.toJson(activeCoursesList));

	}

	/**
	 * builds a map of active students enrolled in courses by merging the
	 * activeStudentsMap with the activeEnrollmentList
	 * 
	 * @param gson
	 */
	private void buildActiveStudentsEnrolledInCoursesList(Gson gson) {
		for (Map<String, String> enrollmentMap : activeEnrollmentList) {
			if (activeStudentsMap.containsKey(enrollmentMap.get("user_id"))) {
				if (debug) {
					System.out.println("have student enrolled");
					System.out.println(enrollmentMap.get("course_id"));
				}
				if (activeStudentsEnrolledInCoursesMap
						.containsKey(enrollmentMap.get("course_id"))) {
					List<String> enrolledStudentsList = activeStudentsEnrolledInCoursesMap
							.get(enrollmentMap.get("course_id"));
					if (!enrolledStudentsList.contains(activeStudentsMap
							.get(enrollmentMap.get("user_id")))) {

						enrolledStudentsList.add(activeStudentsMap
								.get(enrollmentMap.get("user_id")));
					}
					if (debug) {
						System.out.println("****** enrolledStudentsList *****");
						System.out.println(gson.toJson(enrolledStudentsList));
					}
					Collections.sort(enrolledStudentsList);
					activeStudentsEnrolledInCoursesMap.put(
							enrollmentMap.get("course_id"),
							enrolledStudentsList);

				} else {
					List<String> enrolledStudentsList = new ArrayList<String>();
					enrolledStudentsList.add(activeStudentsMap
							.get(enrollmentMap.get("user_id")));
					if (debug) {
						System.out.println("****** enrolledStudentsList *****");
						System.out.println(gson.toJson(enrolledStudentsList));
					}
					
						activeStudentsEnrolledInCoursesMap.put(
								enrollmentMap.get("course_id"),
								enrolledStudentsList);
					
				}

			}

		}
	}

	/**
	 * builds the activeCoursesList by merging the activeCoursesMap with the
	 * activeStudentsEnrolledInCoursesMap
	 */
	private void buildActiveCourseList() {
		
		for (Entry<String, String> course : activeCoursesMap.entrySet()) {
			Map<String, List<String>> courseMap = new HashMap<String, List<String>>();
			List<String> enrolledStudentList = activeStudentsEnrolledInCoursesMap
					.get(course.getKey());
			if(enrolledStudentList==null) {
			  List<String> noStudentsList = new ArrayList<String>();
			  noStudentsList.add("No students enrolled in course" );
			  courseMap.put(course.getValue(), noStudentsList);
			} else {
			courseMap.put(course.getValue(), enrolledStudentList);
			}
			
			activeCoursesList.add(courseMap);
			

		}
	}

	/**
	 * sort the course list by course name
	 */
	private void sortActiveCourseList() {
		Comparator<Map<String, List<String>>> mapComparator = new Comparator<Map<String, List<String>>>() {
			public int compare(Map<String, List<String>> m1,
					Map<String, List<String>> m2) {
				int rtnValue = 0;
				for (Entry<String, List<String>> m1Entry : m1.entrySet()) {
					for (Entry<String, List<String>> m2entry : m2.entrySet()) {
						rtnValue = m1Entry.getKey().compareTo(m2entry.getKey());
						break;
					}
					break;
				}
				return rtnValue;
			}
		};
		Collections.sort(activeCoursesList, mapComparator);
	}

	/**
	 * iterates through the csv files calling parseAndSortCSVFile on each one
	 * 
	 * @param gson
	 *            Gson library object for parsing
	 * @param InputFilesDirectory
	 *            the directory to iterate through
	 * @throws FileNotFoundException
	 */
	private void iterateThroughFiles(Gson gson, String InputFilesDirectory)
			throws FileNotFoundException {
		File[] files = new File(InputFilesDirectory).listFiles();
		for (File file : files) {
			Reader in = new FileReader(file);
			try {
				parseAndSortCSVFile(gson, in);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * parses and sorts the csv file into the correct hashmaps and lists based
	 * on the headers in the csv file
	 * 
	 * @param gson
	 *            the Gson library for parsing
	 * @param in
	 *            the file reader
	 * @throws IOException
	 */
	private void parseAndSortCSVFile(Gson gson, Reader in) throws IOException {
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
		for (CSVRecord record : records) {

			Map<String, String> recordHm = gson.fromJson(
					gson.toJson(record.toMap()),
					new TypeToken<HashMap<String, Object>>() {
					}.getType());
			;
			if (debug) {
				System.out.println("****** Record *******");
				System.out.println(gson.toJson(recordHm));
			}

			if (recordHm.get("state") != null
					&& recordHm.get("state").equals("active")) {
				if (recordHm.get("user_name") != null) {
					activeStudentsMap.put((String) recordHm.get("user_id"),
							(String) recordHm.get("user_name"));
				} else if (recordHm.get("course_name") != null) {
					activeCoursesMap.put((String) recordHm.get("course_id"),
							(String) recordHm.get("course_name"));
				} else {
					activeEnrollmentList.add(recordHm);
				}
			}

		}
		if (debug) {
			System.out.println("****** Students *******");
			System.out.println(gson.toJson(activeStudentsMap));
			System.out.println("****** Courses *******");
			System.out.println(gson.toJson(activeCoursesMap));
			System.out.println("****** Enrollment *******");
			System.out.println(gson.toJson(activeEnrollmentList));
		}
	}

	/**
	 * main method just calls constructor
	 * 
	 * @param args
	 *            not used for exercise but could get parameter for
	 *            inputDirectory here as well if required
	 */
	public static void main(String[] args) {
		ActiveCourseList app = new ActiveCourseList();
	}

	/**
	 * loads properties file from classpath
	 * 
	 * @param propFileName
	 *            the property file name
	 * @return the properties if found.
	 */
	private synchronized void getPropertiesFromClasspath(String propFileName) {

		try {
			InputStream inputStream = this.getClass().getClassLoader()
					.getResourceAsStream(propFileName);

			if (inputStream == null) {
				System.out.println("property file '" + propFileName
						+ "' not found in the classpath");
			} else {

				Type typ = new TypeToken<Map<String, Object>>() {
				}.getType();
				JsonReader reader = new JsonReader(new InputStreamReader(
						inputStream, "UTF-8"));

				// read input file as lenient JSON with C-style comments
				reader.setLenient(true);
				Map<String, Object> myProps = new Gson().fromJson(reader, typ);

				propsMap.putAll(myProps);

			}

		} catch (Throwable ex) {

			System.out.println("Error Loading property file'" + propFileName
					+ "'.");
			ex.printStackTrace();
		}
	}
}
