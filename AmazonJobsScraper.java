import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class AmazonJobsScraper {

    public static final String URL = "https://www.amazon.jobs/en/search.json";
    public static final String OUTPUT_FILE = "jobs.json";
    public static final int RESULT_LIMIT = 1000;

    public static void main(String[] args) {
        System.out.println("Amazon Jobs Scraper");
        System.out.println("Starting");

        int totalPages = getTotalPages();
        List<JSONObject> allJobs = getJobs(totalPages);

        System.out.println("Total Jobs: " + allJobs.size());

        JSONObject jsonData = new JSONObject();
        jsonData.put("company", "Amazon");
        jsonData.put("career page url", URL);
        jsonData.put("total jobs", allJobs.size());
        jsonData.put("jobs", new JSONArray(allJobs));

        try (FileWriter file = new FileWriter(OUTPUT_FILE)) {
            file.write(jsonData.toString(4));
            System.out.println("Data saved to " + OUTPUT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    public static List<JSONObject> getJobs(int totalPages) {
        List<JSONObject> allJobs = new ArrayList<>();

        for (int i = 0; i < totalPages; i++) {
            JSONObject response = makeRequest(i * RESULT_LIMIT);
            if (!response.isNull("jobs")) {
                JSONArray jobs = response.getJSONArray("jobs");
                allJobs.addAll(toList(jobs));
            } else {
                System.out.println("No jobs found in response.");
            }
        }

        JSONObject response = makeRequest(totalPages * RESULT_LIMIT);
        if (!response.isNull("jobs")) {
            JSONArray jobs = response.getJSONArray("jobs");
            allJobs.addAll(toList(jobs));
        } else {
            System.out.println("No jobs found in final response.");
        }

        return allJobs;
    }

    public static JSONObject makeRequest(int offset) {
        try {
            URL url = new URL(URL + "?offset=" + offset);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream responseStream = connection.getInputStream();
            String jsonResponse = new Scanner(responseStream, "UTF-8").useDelimiter("\\A").next();
            responseStream.close();

            return new JSONObject(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static int getTotalPages() {
        JSONObject response = makeRequest(0);
        int hits = response.getInt("hits");
        return (hits + RESULT_LIMIT - 1) / RESULT_LIMIT;
    }

    public static List<JSONObject> toList(JSONArray jsonArray) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getJSONObject(i));
        }
        return list;
    }
}

// Run Command : java -cp "./json-20230618.jar" AmazonJobsScraper.java
// AmazonJobsScraper.java
