package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;


@Slf4j
@Component
public class YachtVacancyScraper extends VacancyScraper {

    private static final String VACANCY_URL_PREFIX = "https://www.yacht.nl";

    private final Pattern dmyPattern = Pattern.compile("^(3[01]|[12][0-9]|0[1-9]) [a-z]+ [0-9]{4}$");
    private final DateTimeFormatter dmyFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(new Locale("nl", "NL"));

    RestTemplate restTemplate = new RestTemplate();

    public YachtVacancyScraper() {
        super(
                "https://www.yacht.nl/vacatures?_hn:type=resource&_hn:ref=r2_r1_r1&&vakgebiedProf=IT", // Required search URL. Can be retrieved using getSEARCH_URL()
                "Yacht" // Required broker. Can be retrieved using getBROKER()
        );
    }

    /**
     * This method retrieves all URLs and other available data of the page that shows multiple vacancies.
     *
     * @return List of VacancyURLs with as much details of the vacancy as possible.
     */

    @Override
    public List<VacancyDTO> getVacancies() {
        log.info("{} -- Start scraping", getBroker().toUpperCase());
        List<VacancyDTO> vacancyDTOs = new CopyOnWriteArrayList<>();

        int totalnumberOfPages = 1;
        for (int pageNumber = 1; pageNumber <= totalnumberOfPages; pageNumber++) {
            YachtVacancyResponse yachtVacancyResponse = scrapeVacancies(pageNumber);

            if (pageNumber == 1 && yachtVacancyResponse != null) {
                totalnumberOfPages = yachtVacancyResponse.getPages();
                log.info("{} -- Total number of pages: {}", getBroker(), totalnumberOfPages);
            }

            if (yachtVacancyResponse != null) {
                log.info("{} -- Retrieving vacancy urls from page: {} of {}", getBroker(), yachtVacancyResponse.getCurrentPage(), yachtVacancyResponse.getPages());

                yachtVacancyResponse.getVacancies().parallelStream().forEach((Map<String, Object> vacancyData) -> {
                    Map<String, Object> vacancyMetaData = (Map<String, Object>) vacancyData.get("meta");
                    String vacancyURL = (String) vacancyData.get("detailUrl");
                    vacancyURL = vacancyURL.contains("?") ? vacancyURL.split("\\?")[0] : vacancyURL;
                    vacancyURL = vacancyURL.contains("http") ? vacancyURL : VACANCY_URL_PREFIX + vacancyURL;
                    Document vacancyDoc = getDocument(vacancyURL);
                    VacancyDTO vacancyDTO = VacancyDTO.builder()
                            .vacancyURL(vacancyURL)
                            .title((String) vacancyData.get("title"))
                            .hours(getHours((String) vacancyMetaData.get("hours")))
                            .broker(getBroker())
                            .vacancyNumber((String) vacancyData.get("vacancyNumber"))
                            .locationString((String) vacancyMetaData.get("location"))
                            .postingDate(getPostingDate((String) vacancyData.get("date")))
                            .about(getVacancyAbout(vacancyDoc))
                            .salary((String) vacancyMetaData.get("salary"))
                            .company((String) vacancyData.get("company"))
                            .build();

                    vacancyDTOs.add(vacancyDTO);
                    log.info("{} - Vacancy found: {}", getBroker(), vacancyDTO.getTitle());
                });
            }
        }
        log.info("{} -- Returning scraped vacancies", getBroker());
        return vacancyDTOs;
    }

    /**
     * This method does a get request to Yacht to retrieve the vacancies from a specific page.
     *
     * @param pageNumber Pagenumber of which the vacancy data should be retrieved
     * @return json response from the get request
     */
    private YachtVacancyResponse scrapeVacancies(int pageNumber) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        ResponseEntity<YachtVacancyResponse> response
                = restTemplate.getForEntity(getSearchUrl() + "&pagina=" + pageNumber, YachtVacancyResponse.class);

        return response.getBody();
    }

    /**
     * This method selects the vacancy details from the html document
     *
     * @param doc jsoup document of a vacancy
     * @return cleaned html string of vacancy body
     */
    private String getVacancyAbout(Document doc) {
        // Extracts the about part from the vacancy
        Element vacancyBody = doc.select(".rich-text--vacancy").first();
        return Jsoup.clean(vacancyBody.html(), Whitelist.basic());
    }

    private LocalDateTime getPostingDate(String date) {
        return checkDatePattern(date) ? LocalDate.parse(date, dmyFormatter).atStartOfDay() : null;
    }

    private boolean checkDatePattern(String s) {
        return s != null && dmyPattern.matcher(s).matches();
    }

    private Integer getHours(String input) {
        if (input.length() > 2) {
            String possibleHours = input.substring(0, 2);
            try {
                return Integer.parseInt(possibleHours);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
