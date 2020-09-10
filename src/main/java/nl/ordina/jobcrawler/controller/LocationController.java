package nl.ordina.jobcrawler.controller;

import nl.ordina.jobcrawler.controller.exception.LocationNotFoundException;
import nl.ordina.jobcrawler.model.Location;
import nl.ordina.jobcrawler.model.Vacancy;
import nl.ordina.jobcrawler.model.assembler.LocationModelAssembler;
import nl.ordina.jobcrawler.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;
    private final LocationModelAssembler locationModelAssembler;

    @Autowired
    public LocationController(LocationService locationService, LocationModelAssembler locationModelAssembler) {
        this.locationService = locationService;
        this.locationModelAssembler = locationModelAssembler;
    }


    @GetMapping("/{id}")
    public EntityModel<Location> getLocation(@PathVariable UUID id) {
        Location location = locationService.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
        return locationModelAssembler.toModel(location);
    }

/*    @GetMapping("/{locationName}")
    public EntityModel<Location> getLocation(@PathVariable String locationName) {
        Location location = locationService.findByLocationName(locationName)
                .orElseThrow(() -> new LocationNotFoundException(locationName));
        return locationModelAssembler.toModel(location);
    }*/

    public CollectionModel<EntityModel<Location>> getLocations() {

        return locationModelAssembler.toCollectionModel(locationService.findAll());

    }


}
