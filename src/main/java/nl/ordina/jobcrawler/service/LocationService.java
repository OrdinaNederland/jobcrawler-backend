package nl.ordina.jobcrawler.service;

import lombok.extern.slf4j.Slf4j;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Skill;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.repo.LocationRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<Location> findByOrderByNameAsc() {
        return locationRepository.findByOrderByNameAsc();
    }

    public Optional<Location> findById(UUID id) {
        return locationRepository.findById(id);
    }

    public Optional<Location> findByLocationName(String locationName) {return locationRepository.findByName(locationName); }

    public Optional<Location> findByLocationNameInVacancyList(String locationName, List<Vacancy> allVacancies) {
        for(Vacancy vacancy : allVacancies) {
            if(!(vacancy.getLocation()==null)) {
                if (vacancy.getLocation().getName().equals(locationName)) {
                    return Optional.of(vacancy.getLocation());
                }
            }
        }
        return Optional.empty();
    }

    public static Location getCoordinates(String location) throws IOException, JSONException {
        final String apiKey = "Xd5hXSuQvqUJJbJh3iacOXZAcskvP7gI";
        String location2 = location.concat(", Nederland");
        location2 = location2.replace(" ","%20");
        final String url = "http://open.mapquestapi.com/nominatim/v1/search.php?key=" + apiKey + "&format=json&q=" + location2 + "&addressdetails=1&limit=1";

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        connection.setRequestMethod("GET");
        //add request header
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (connection.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.readLine();
            response = response.substring(1, response.length() - 1);
            log.debug(response);
            in.close();
            //Read JSON response and return
            JSONObject jsonResponse = new JSONObject(response);

            return new Location(location, jsonResponse.getDouble("lon"), jsonResponse.getDouble("lat"));

        }
        return new Location(location, 0, 0);
    }

    public static double getDistanceFromHome(Location homeLocation, Location vacancyLocation) {

        // Convert degrees to radians
        double lon1 = Math.toRadians(homeLocation.getLon());
        double lat1 = Math.toRadians(homeLocation.getLat());
        double lon2 = Math.toRadians(vacancyLocation.getLon());
        double lat2 = Math.toRadians(vacancyLocation.getLat());

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        // Radius of earth in kilometers. Use 3956 for miles
        double r = 6371;
        // calculate the result
        return (c * r);
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public Location save(Location location) {
        return locationRepository.save(location);
    }

    public boolean delete(UUID id) {
        locationRepository.deleteById(id);
        return false;
    }
}
