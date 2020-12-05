package nl.ordina.jobcrawler.scrapers;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.payload.VacancyDTO;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Form;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HeadfirstScraper extends VacancyScraper {

    private final Pattern dmyPattern = Pattern.compile("^(3[01]|[12][0-9]|0[1-9]) [a-z]+ [0-9]{4}$");
    private final DateTimeFormatter dmyFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(new Locale("nl", "NL"));

    RestTemplate restTemplate = new RestTemplate();

    public HeadfirstScraper() {
        super(
                "https://jobcatcher.nl/api2/v1/requestsearch/search?", // Required search URL. Can be retrieved using getSEARCH_URL()
                "JobCatcher" // Required broker. Can be retrieved using getBROKER()
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

        // Configure headers for request
        HttpHeaders headers = new HttpHeaders();

        headers.add("accept","application/json, text/plain, */*");
        headers.add("accept-encoding","gzip, deflate, br");
        headers.add("accept-language","en-US,en;q=0.9");
        headers.add("content-length","75");
        headers.add("content-type","application/x-www-form-urlencoded; charset=UTF-8");
        headers.add("cookie","_ga=GA1.2.301182803.1606140276; _gid=GA1.2.1677578532.1606985551; SELECT-AUTH-TOKEN=YmU3ZDJiYTMtYjU4NS00MGE3LWExNTYtYWQ2YThiMmJkYjY3; SELECT-ORIGIN=/nl/nl/login; _gat=1");
        headers.add("origin","https://portal.select.hr");
        headers.add("referer","https://portal.select.hr/nl/nl/login");
        headers.add("sec-fetch-dest","empty");
        headers.add("sec-fetch-mode","cors");
        headers.add("sec-fetch-site","same-origin");
        headers.add("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");

        // Configure request body parameters
        MultiValueMap<String, String> body= new LinkedMultiValueMap<String, String>();
        body.add("username","kees.hannema@ordina.nl");
        body.add("password","JC0112Jt*");
        body.add("country", "nl");
        body.add("language", "nl");

        // Build and trigger the request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

//        HeadfirstLoginResponse response = restTemplate.postForObject("https://portal.select.hr/login", request, HeadfirstLoginResponse.class);
//        String response = restTemplate.postForObject("https://portal.select.hr/login", request, String.class);

        ResponseEntity<String> response = restTemplate.exchange("https://portal.select.hr/login", HttpMethod.POST, request, String.class);

        HttpHeaders headers2 = new HttpHeaders();

        headers2.add("cookie",   "hblid=gtf0kJQ0YlA2yKjm7q9Li0O6ab4Pb6ao; " +
                "olfsk=olfsk035696944379663975; " +
                "_ga=GA1.3.301182803.1606140276; " +
                "SELECT-COUNT=0; " +
                "wcsid=be55TmV5Tm8ZQtv57q9Li0OPbAa4aSoj; " +
                "langKey=nl; " +
                "_ok=3218-442-10-9527; " +
                "_gid=GA1.3.1677578532.1606985551; " +
                "SELECT-ORIGIN=/nl/nl/login; " +
                "SELECT-JWT-TOKEN=NzllNWEyZWItN2ZmMy00ODNmLTliZjEtMzUzYmE1MjlmNDk1; " +
                "_okdetect=%7B%22token%22%3A%2216069882122260%22%2C%22proto%22%3A%22https%3A%22%2C%22host%22%3A%22headfirst.select.hr%22%7D; " +
                "_okbk=cd4%3Dtrue%2Ccd5%3Davailable%2Cvi5%3D0%2Cvi4%3D1606985566076%2Cvi3%3Dactive%2Cvi2%3Dfalse%2Cvi1%3Dfalse%2Ccd8%3Dchat%2Ccd6%3D0%2Ccd3%3Dfalse%2Ccd2%3D0%2Ccd1%3D0%2C; " +
                "JSESSIONID=0133E98FAB04CF2107BADE1B805CFC67; " +
                "_oklv=1607007280795%2Cbe55TmV5Tm8ZQtv57q9Li0OPbAa4aSoj");
//        headers2.add("Postman-Token","");
//        headers2.add("content-type","application/json");
//        headers2.add("content-length","");
//        headers2.add("Host","");
//        headers2.add("user-agent","PostmanRuntime/7.26.8");
//        headers2.add("accept","*/*");
        headers2.add("accept-encoding","gzip, deflate, br");
        headers2.add("connection","keep-alive");
        headers2.add("accept","application/json, text/plain, */*");
        headers2.add("X-Requested-With","XMLHttpRequest");
        headers2.add("SELECT-JWT-TOKEN","OTZhNTZlN2QtMTM3YS00ZWUyLTgyNjYtMDlmMjMzZWY1OGJm");
        headers2.add("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
        headers2.add("content-type","application/json;charset=UTF-8");

//        MultiValueMap<String, Integer> body2= new LinkedMultiValueMap<String, Integer>();
//        body2.add("page_start", 0);

        String body2 = "{\"page_start\": 0}";

        HttpEntity<String> request2 = new HttpEntity<String>(body2,headers2);
        ResponseEntity<String> response2 = restTemplate.exchange("https://headfirst.select.hr/api/v2/jobrequest/search", HttpMethod.POST, request2, String.class);

        List<VacancyDTO> vacancyDTOs = new CopyOnWriteArrayList<>();

        int nrVacancies = scrapeVacancies(0).getData().get(0).getAmount();
        List<Map<String, Object>> vacanciesList = scrapeVacancies(nrVacancies).getData().get(0).getList();

            vacanciesList.parallelStream().forEach((Map<String, Object> vacancyData) -> {
                String vacancyTitle = (String) vacancyData.get("jobrolename");
                String vacancyCompany = (String) vacancyData.get("requesterpartyname");
                String vacancyURL = "https://www.jobcatcher.nl/opdrachten/" + vacancyTitle.replace("/","-") + "/" + vacancyCompany.replace("/","-") + "/" + vacancyData.get("requestid");
                vacancyURL = vacancyURL.toLowerCase();
                vacancyURL = vacancyURL.replace(" ","-");
                vacancyURL = vacancyURL.replace("(","%28");
                vacancyURL = vacancyURL.replace(")","%29");
                Document vacancyDoc = getDocument(vacancyURL);
                String vacancySalary = !(vacancyData.get("maximumpurchaseprice")==null)?((String) vacancyData.get("maximumpurchaseprice")) + " per uur":"";

                VacancyDTO vacancyDTO = VacancyDTO.builder()
                        .vacancyURL(vacancyURL)
                        .title(vacancyTitle)
                        .hours((int) Double.parseDouble(((String) vacancyData.get("availability")).replace(',','.')))
                        .broker(getBroker())
                        .vacancyNumber(vacancyData.get("requestid").toString())
                        .locationString(toTitleCase((String) vacancyData.get("locationname")))
                        .postingDate(LocalDateTime.parse((String) vacancyData.get("publishdate"), DateTimeFormatter.ofPattern("yyyy-MM-dd[ ]['T']HH:mm:ss['Z']")))
                        .about(getVacancyAbout(vacancyDoc))
                        .salary(vacancySalary)
                        .company(vacancyCompany)
                        .build();

                vacancyDTOs.add(vacancyDTO);
                log.info("{} - Vacancy found: {}", getBroker(), vacancyDTO.getTitle());
            });
        log.info("{} -- Returning scraped vacancies", getBroker());
        return vacancyDTOs;
    }

    /**
     * This method does a get request to Yacht to retrieve the vacancies from a specific page.
     *
     * @param pageNumber Pagenumber of which the vacancy data should be retrieved
     * @return json response from the get request
     */
    private JobCatcherResponse scrapeVacancies(int nrOfVacancies) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        String url = getSearchUrl() + "itemsperpage=" + nrOfVacancies;

        ResponseEntity<JobCatcherResponse> response
                = restTemplate.getForEntity(url, JobCatcherResponse.class);

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
        String about = "";
        Elements els1 = doc.select("h3");
        for (int i = 0; i < els1.size(); i++) {
            Elements els2 = els1.get(i).parent().nextElementSiblings();
            for (int j = 0; j < els2.size(); j++) {
                about = about + els2.get(j).text() + "\n";
            }
            about = about + "\n";
        }
        return about;
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

    public String toTitleCase(String orig) {
        orig = orig.toLowerCase();
        orig = orig.substring(0,1).toUpperCase() + orig.substring(1,orig.length());
        for(int i = 1; i<orig.length(); i++) {
            if(orig.charAt(i-1)==' '||orig.charAt(i-1)=='-') {
                orig = orig.substring(0,i) + orig.substring(i,i+1).toUpperCase() + orig.substring(i+1,orig.length());
            }
        }
        return orig;
    }

}
