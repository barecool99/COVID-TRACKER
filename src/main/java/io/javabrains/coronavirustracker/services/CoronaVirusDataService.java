package io.javabrains.coronavirustracker.services;

import io.javabrains.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_US.csv";
    //Postconstruct-executes this service class when app starts
    private List<LocationStats> allStats = new ArrayList<>();
    public List<LocationStats> getAllStats() {
        return allStats;
    }
    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")//runs on daily basis on say AWS. Google CORN STARS SYSTEM.
    public void fetchVirusData() throws IOException, InterruptedException { // adds exception automatically.
        //create new stats and populate allStats List for concurrency reasons.
        List<LocationStats> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        //httprequest basically makes URL off VIRUS_DATA_URL variable
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        //take the body of github raw thing and return as a string
        HttpResponse<String> httpResponse = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        //READ TEXT FROM header from String we created which was github link.
        //use this to pass json from web.
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province_State"));
            locationStat.setCountry(record.get("Country_Region"));
            int prevDayCases= Integer.parseInt(record.get(record.size() - 2));
            int latestCases=Integer.parseInt(record.get(record.size() - 1));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay(latestCases-prevDayCases);
//            System.out.println(locationStat);
            newStats.add(locationStat);
        }
        this.allStats = newStats; // this populates it for concurrency reasons.
    }
}
