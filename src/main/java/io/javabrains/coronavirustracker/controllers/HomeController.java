package io.javabrains.coronavirustracker.controllers;

import io.javabrains.coronavirustracker.models.LocationStats;
import io.javabrains.coronavirustracker.services.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@ComponentScan
public class HomeController {

    //since this is a service, we can @autowire in our controller
    @Autowired
    CoronaVirusDataService coronaVirusDataService;

    @GetMapping("/") // this maps to home.html in templates which should be in resources.
    public String home(Model model) {
        //gets info from Services and list it into allstats.
        List<LocationStats> allStats = coronaVirusDataService.getAllStats();
        //quick mapping totalreportedcases's sum.
        int totalReportedCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        // we instantiate model and add attribute which will allows us to access templates(html) via thymeleaf
        // using thymeleaf we can add this attribute string(testName)
        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalReportedCases", totalReportedCases);
        model.addAttribute("totalNewCases", totalNewCases);
        //point to template here, it works because in pom we have thymeleaf dependency
        return "home";
    }

}
