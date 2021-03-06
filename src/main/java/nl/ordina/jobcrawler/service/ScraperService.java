package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import nl.ordina.jobcrawler.scrapers.*;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/*
This 'starter' class uses the @Scheduled annotation. Every 15 minutes it executes the cronJobSch() function to retrieve all vacancies.
Upon fetching the vacancies it runs a check to verify if the vacancy is already present in the database.
*/

@Slf4j
@Service
public class ScraperService {

    private final VacancyService vacancyService;
    private final LocationService locationService;
    private final YachtVacancyScraper yachtVacancyScraper;
    private final HuxleyITVacancyScraper huxleyITVacancyScraper;
    private final JobBirdScraper jobBirdScraper;
    private final HeadfirstScraper headfirstScraper;
    private final StaffingGroupScraper staffingGroupScraper;
    private final JobCatcherScraper jobCatcherScraper;
    private final ModelMapper modelMapper;

    public ScraperService(VacancyService vacancyService, LocationService locationService,
                          YachtVacancyScraper yachtVacancyScraper, HuxleyITVacancyScraper huxleyITVacancyScraper,
                          JobBirdScraper jobBirdScraper, HeadfirstScraper headfirstScraper, JobCatcherScraper jobCatcherScraper, StaffingGroupScraper staffingGroupScraper, ModelMapper modelMapper) {
        this.vacancyService = vacancyService;
        this.locationService = locationService;
        this.yachtVacancyScraper = yachtVacancyScraper;
        this.huxleyITVacancyScraper = huxleyITVacancyScraper;
        this.jobBirdScraper = jobBirdScraper;
        this.headfirstScraper = headfirstScraper;
        this.jobCatcherScraper = jobCatcherScraper;
        this.staffingGroupScraper = staffingGroupScraper;
        this.modelMapper = modelMapper;
    }

    //@PostConstruct
    @Scheduled(cron = "0 0 12,18 * * *")
    // Runs two times a day. At 12pm and 6pm
    @Transactional
    public void scrape() {
        log.info("CRON Scheduled -- Scrape vacancies");

        List<VacancyDTO> allVacancyDTOs = startScraping();
        Vacancy vacancy;
        int existVacancy = 0;
        int newVacancy = 0;

        for (VacancyDTO vacancyDTO : allVacancyDTOs) {
            try {
                Optional<Vacancy> existCheck = vacancyService.findByURL(vacancyDTO.getVacancyURL());
                if (existCheck.isPresent()) {
                    existVacancy++;
                } else {
                    vacancy = modelMapper.map(vacancyDTO, Vacancy.class);
                    processVacancyLocation(vacancy, vacancyDTO);
                    newVacancy++;
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        log.info(newVacancy + " new vacancies added.");
        log.info(existVacancy + " existing vacancies found.");
        log.info("Finished scraping");
        allVacancyDTOs.clear();
    }

    private void processVacancyLocation(Vacancy vacancy, VacancyDTO vacancyDTO) throws IOException {
        String vacancyLocation = LocationService.normalizeName(vacancyDTO.getLocationString());
        if (vacancyLocation.endsWith(", Nederland")) {
            vacancyLocation = vacancyLocation.substring(0, vacancyLocation.length() - 11);
        }
        if (vacancyLocation.endsWith(", Netherlands")) {
            vacancyLocation = vacancyLocation.substring(0, vacancyLocation.length() - 13);
        }
        if (vacancyLocation.endsWith(", the Netherlands")) {
            vacancyLocation = vacancyLocation.substring(0, vacancyLocation.length() - 16);
        }
        if (vacancyLocation.equals("'s-Hertogenbosch")) {
            vacancyLocation = "Den Bosch";
        }
        if (!vacancyLocation.equals("")) {
            Optional<Location> existCheckLocation = locationService.findByLocationName(vacancyLocation);
            Location location;
            if (existCheckLocation.isEmpty()) {
                location = new Location(vacancyLocation, locationService.getCoordinates(vacancyLocation));
                locationService.save(location);
            } else {
                location = existCheckLocation.get();
            }
            vacancy.setLocation(location);
        }
        vacancyService.save(vacancy);
    }

    @Scheduled(cron = "0 30 11,17 * * *") // Runs two times a day. At 11.30am and 5.30pm.
    public void deleteNoMoreExistingVacancies() {
        log.info("CRON Scheduled -- Started deleting non-existing jobs");

        vacancyService.findAll().stream().filter(v -> !vacancyService.hasExistingURL(v))
                .forEach(v -> vacancyService.delete(v.getId()));

        log.info("Finished deleting non-existing jobs");
    }

    private List<VacancyDTO> startScraping() {
        List<VacancyDTO> vacancyDTOsList = new CopyOnWriteArrayList<>();
        Arrays.asList(yachtVacancyScraper, huxleyITVacancyScraper, jobBirdScraper, headfirstScraper, staffingGroupScraper, jobCatcherScraper)
                .parallelStream().forEach(vacancyScraper -> vacancyDTOsList
                .addAll(vacancyScraper.getVacancies()));
        return vacancyDTOsList;
    }

}
