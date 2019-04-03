package net.sumaris.server.http.graphql.data;

/*-
 * #%L
 * SUMARiS:: Server
 * %%
 * Copyright (C) 2018 SUMARiS Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.leangen.graphql.annotations.*;
import net.sumaris.core.dao.technical.SortDirection;
import net.sumaris.core.model.data.*;
import net.sumaris.core.service.data.*;
import net.sumaris.core.service.data.BatchService;
import net.sumaris.core.service.data.SampleService;
import net.sumaris.core.service.referential.PmfmService;
import net.sumaris.core.vo.administration.user.DepartmentVO;
import net.sumaris.core.vo.administration.user.PersonVO;
import net.sumaris.core.vo.data.*;
import net.sumaris.core.vo.filter.ObservedLocationFilterVO;
import net.sumaris.core.vo.filter.OperationFilterVO;
import net.sumaris.core.vo.filter.TripFilterVO;
import net.sumaris.core.vo.filter.VesselFilterVO;
import net.sumaris.core.vo.referential.PmfmVO;
import net.sumaris.server.http.security.IsSupervisor;
import net.sumaris.server.http.security.IsUser;
import net.sumaris.server.service.administration.ImageService;
import net.sumaris.server.service.technical.ChangesPublisherService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class DataGraphQLService {

    private static final Logger log = LoggerFactory.getLogger(DataGraphQLService.class);

    @Autowired
    private VesselService vesselService;

    @Autowired
    private TripService tripService;

    @Autowired
    private ObservedLocationService observedLocationService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private VesselPositionService vesselPositionService;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private BatchService batchService;

    @Autowired
    private MeasurementService measurementService;

    @Autowired
    private PmfmService pmfmService;

    @Autowired
    protected PhysicalGearService physicalGearService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ChangesPublisherService changesPublisherService;

    /* -- Vessel -- */


    @GraphQLQuery(name = "vessels", description = "Search in vessels")
    @Transactional(readOnly = true)
    @IsUser
    public List<VesselFeaturesVO> findVesselsByFilter(@GraphQLArgument(name = "filter") VesselFilterVO filter,
                                                      @GraphQLArgument(name = "offset", defaultValue = "0") Integer offset,
                                                      @GraphQLArgument(name = "size", defaultValue = "1000") Integer size,
                                                      @GraphQLArgument(name = "sortBy", defaultValue = VesselFeaturesVO.PROPERTY_EXTERIOR_MARKING) String sort,
                                                      @GraphQLArgument(name = "sortDirection", defaultValue = "asc") String direction
    ) {
        return vesselService.findByFilter(filter, offset, size, sort,
                direction != null ? SortDirection.valueOf(direction.toUpperCase()) : null);
    }

    @GraphQLMutation(name = "saveVessel", description = "Create or update a vessel")
    @IsUser
    public VesselFeaturesVO saveVessel(@GraphQLArgument(name = "vessel") VesselFeaturesVO vessel) {
        return vesselService.save(vessel);
    }

    @GraphQLMutation(name = "saveVessels", description = "Create or update many vessels")
    @IsUser
    public List<VesselFeaturesVO> saveVessels(@GraphQLArgument(name = "vessels") List<VesselFeaturesVO> vessels) {
        return vesselService.save(vessels);
    }

    @GraphQLMutation(name = "deleteVessel", description = "Delete a vessel (by vessel features id)")
    @IsUser
    public void deleteVessel(@GraphQLArgument(name = "id") int id) {
        vesselService.delete(id);
    }

    @GraphQLMutation(name = "deleteVessels", description = "Delete many vessels (by vessel features ids)")
    @IsUser
    public void deleteVessels(@GraphQLArgument(name = "ids") List<Integer> ids) {
        vesselService.delete(ids);
    }


    /* -- Trip -- */

    @GraphQLQuery(name = "trips", description = "Search in trips")
    @Transactional(readOnly = true)
    @IsUser
    public List<TripVO> findTripsByFilter(@GraphQLArgument(name = "filter") TripFilterVO filter,
                                          @GraphQLArgument(name = "offset", defaultValue = "0") Integer offset,
                                          @GraphQLArgument(name = "size", defaultValue = "1000") Integer size,
                                          @GraphQLArgument(name = "sortBy", defaultValue = TripVO.PROPERTY_DEPARTURE_DATE_TIME) String sort,
                                          @GraphQLArgument(name = "sortDirection", defaultValue = "asc") String direction,
                                          @GraphQLEnvironment() Set<String> fields
                                  ) {

        final List<TripVO> result = tripService.findByFilter(filter, offset, size, sort,
                direction != null ? SortDirection.valueOf(direction.toUpperCase()) : null,
                getFetchOptions(fields));

        // Add additional properties if needed
        fillTrips(result, fields);

        return result;
    }

    @GraphQLQuery(name = "tripsCount", description = "Get total trips count")
    @Transactional(readOnly = true)
    @IsUser
    public long getTripsCount(@GraphQLArgument(name = "filter") TripFilterVO filter) {
        return tripService.countByFilter(filter);
    }

    @GraphQLQuery(name = "trip", description = "Get a trip, by id")
    @Transactional(readOnly = true)
    @IsUser
    public TripVO getTripById(@GraphQLArgument(name = "id") int id,
                              @GraphQLEnvironment() Set<String> fields) {
        final TripVO result = tripService.get(id);

        // Add additional properties if needed
        fillTripFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "saveTrip", description = "Create or update a trip")
    @IsUser
    public TripVO saveTrip(@GraphQLArgument(name = "trip") TripVO trip, @GraphQLEnvironment() Set<String> fields) {
        final TripVO result = tripService.save(trip, false);

        // Add additional properties if needed
        fillTripFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "saveTrips", description = "Create or update many trips")
    @IsUser
    public List<TripVO> saveTrips(@GraphQLArgument(name = "trips") List<TripVO> trips, @GraphQLEnvironment() Set<String> fields) {
        final List<TripVO> result = tripService.save(trips, false);

        // Add additional properties if needed
        fillTrips(result, fields);

        return result;
    }

    @GraphQLMutation(name = "deleteTrip", description = "Delete a trip")
    @IsUser
    public void deleteTrip(@GraphQLArgument(name = "id") int id) {
        tripService.delete(id);
    }

    @GraphQLMutation(name = "deleteTrips", description = "Delete many trips")
    @IsUser
    public void deleteTrips(@GraphQLArgument(name = "ids") List<Integer> ids) {
        tripService.delete(ids);
    }

    @GraphQLSubscription(name = "updateTrip", description = "Subscribe to changes on a trip")
    @IsUser
    public Publisher<TripVO> updateTrip(@GraphQLArgument(name = "id") final int id,
                                        @GraphQLArgument(name = "interval", defaultValue = "30", description = "Minimum interval to get changes, in seconds.") final Integer minIntervalInSecond) {

        Preconditions.checkArgument(id >= 0, "Invalid id");
        return changesPublisherService.getPublisher(Trip.class, TripVO.class, id, minIntervalInSecond, true);
    }

    @GraphQLMutation(name = "controlTrip", description = "Control a trip")
    @IsUser
    public TripVO controlTrip(@GraphQLArgument(name = "trip") TripVO trip, @GraphQLEnvironment() Set<String> fields) {
        final TripVO result = tripService.control(trip);

        // Add additional properties if needed
        fillTripFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "validateTrip", description = "Validate a trip")
    @IsSupervisor
    public TripVO validateTrip(@GraphQLArgument(name = "trip") TripVO trip, @GraphQLEnvironment() Set<String> fields) {
        final TripVO result = tripService.validate(trip);

        // Add additional properties if needed
        fillTripFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "unvalidateTrip", description = "Unvalidate a trip")
    @IsSupervisor
    public TripVO unvalidateTrip(@GraphQLArgument(name = "trip") TripVO trip, @GraphQLEnvironment() Set<String> fields) {
        final TripVO result = tripService.unvalidate(trip);

        // Add additional properties if needed
        fillTripFields(result, fields);

        return result;
    }

    /* -- Gears -- */

    @GraphQLQuery(name = "gears", description = "Get operation's gears")
    public List<PhysicalGearVO> getGearsByTrip(@GraphQLContext TripVO trip) {
        return physicalGearService.getPhysicalGearByTripId(trip.getId());
    }

    /* -- Observed location -- */

    @GraphQLQuery(name = "observedLocations", description = "Search in observed locations")
    @Transactional(readOnly = true)
    @IsUser
    public List<ObservedLocationVO> findObservedLocationsByFilter(@GraphQLArgument(name = "filter") ObservedLocationFilterVO filter,
                                                                @GraphQLArgument(name = "offset", defaultValue = "0") Integer offset,
                                                                @GraphQLArgument(name = "size", defaultValue = "1000") Integer size,
                                                                @GraphQLArgument(name = "sortBy", defaultValue = ObservedLocationVO.PROPERTY_START_DATE_TIME) String sort,
                                                                @GraphQLArgument(name = "sortDirection", defaultValue = "asc") String direction,
                                                                @GraphQLEnvironment() Set<String> fields
    ) {
        final List<ObservedLocationVO> result = observedLocationService.findByFilter(filter, offset, size, sort,
                direction != null ? SortDirection.valueOf(direction.toUpperCase()) : null,
                getFetchOptions(fields));

        // Add additional properties if needed
        fillObservedLocationsFields(result, fields);

        return result;
    }

    @GraphQLQuery(name = "observedLocationCount", description = "Get total number of observed locations")
    @Transactional(readOnly = true)
    @IsUser
    public long getObservedLocationsCount(@GraphQLArgument(name = "filter") ObservedLocationFilterVO filter) {
        return observedLocationService.countByFilter(filter);
    }

    @GraphQLQuery(name = "observedLocation", description = "Get an observed location, by id")
    @Transactional(readOnly = true)
    @IsUser
    public ObservedLocationVO getObservedLocationById(@GraphQLArgument(name = "id") int id,
                              @GraphQLEnvironment() Set<String> fields) {
        final ObservedLocationVO result = observedLocationService.get(id);

        // Add additional properties if needed
        fillObservedLocationFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "saveObservedLocation", description = "Create or update an observed location")
    @IsUser
    public ObservedLocationVO saveObservedLocation(@GraphQLArgument(name = "observedLocation") ObservedLocationVO observedLocation, @GraphQLEnvironment() Set<String> fields) {
        final ObservedLocationVO result = observedLocationService.save(observedLocation, false);

        // Fill expected fields
        fillObservedLocationFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "saveObservedLocations", description = "Create or update many observed locations")
    @IsUser
    public List<ObservedLocationVO> saveObservedLocations(@GraphQLArgument(name = "observedLocations") List<ObservedLocationVO> observedLocations, @GraphQLEnvironment() Set<String> fields) {
        final List<ObservedLocationVO> result = observedLocationService.save(observedLocations, false);

        // Fill expected fields
        fillObservedLocationsFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "deleteObservedLocation", description = "Delete an observed location")
    @IsUser
    public void deleteObservedLocation(@GraphQLArgument(name = "id") int id) {
        observedLocationService.delete(id);
    }

    @GraphQLMutation(name = "deleteObservedLocations", description = "Delete many observed locations")
    @IsUser
    public void deleteObservedLocations(@GraphQLArgument(name = "ids") List<Integer> ids) {
        observedLocationService.delete(ids);
    }

    @GraphQLSubscription(name = "updateObservedLocation", description = "Subscribe to changes on an observed location")
    @IsUser
    public Publisher<ObservedLocationVO> updateObservedLocation(@GraphQLArgument(name = "id") final int id,
                                        @GraphQLArgument(name = "interval", defaultValue = "30", description = "Minimum interval to get changes, in seconds.") final Integer minIntervalInSecond) {

        Preconditions.checkArgument(id >= 0, "Invalid id");
        return changesPublisherService.getPublisher(ObservedLocation.class, ObservedLocationVO.class, id, minIntervalInSecond, true);
    }

    @GraphQLMutation(name = "controlObservedLocation", description = "Control an observed location")
    @IsUser
    public ObservedLocationVO controlObservedLocation(@GraphQLArgument(name = "observedLocation") ObservedLocationVO observedLocation, @GraphQLEnvironment() Set<String> fields) {
        final ObservedLocationVO result = observedLocationService.control(observedLocation);

        // Add additional properties if needed
        fillObservedLocationFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "validateObservedLocation", description = "Validate an observed location")
    @IsSupervisor
    public ObservedLocationVO validateObservedLocation(@GraphQLArgument(name = "observedLocation") ObservedLocationVO observedLocation, @GraphQLEnvironment() Set<String> fields) {
        final ObservedLocationVO result = observedLocationService.validate(observedLocation);

        // Add additional properties if needed
        fillObservedLocationFields(result, fields);

        return result;
    }

    @GraphQLMutation(name = "unvalidateObservedLocation", description = "Unvalidate an observed location")
    @IsSupervisor
    public ObservedLocationVO unvalidateObservedLocation(@GraphQLArgument(name = "observedLocation") ObservedLocationVO observedLocation, @GraphQLEnvironment() Set<String> fields) {
        final ObservedLocationVO result = observedLocationService.unvalidate(observedLocation);

        // Add additional properties if needed
        fillObservedLocationFields(result, fields);

        return result;
    }


    /* -- Sales -- */

    @GraphQLQuery(name = "sales", description = "Get trip's sales")
    public List<SaleVO> getSalesByTrip(@GraphQLContext TripVO trip) {
        return saleService.getAllByTripId(trip.getId());
    }

    @GraphQLQuery(name = "sale", description = "Get trip's unique sale")
    public SaleVO getUniqueSaleByTrip(@GraphQLContext TripVO trip) {
        List<SaleVO> sales = saleService.getAllByTripId(trip.getId());
        return CollectionUtils.isEmpty(sales) ? null : CollectionUtils.extractSingleton(sales);
    }

    /* -- Operations -- */

    @GraphQLQuery(name = "operations", description = "Get trip's operations")
    @Transactional(readOnly = true)
    @IsUser
    public List<OperationVO> getOperationsByTripId(@GraphQLArgument(name = "filter") OperationFilterVO filter,
                                                   @GraphQLArgument(name = "offset", defaultValue = "0") Integer offset,
                                                   @GraphQLArgument(name = "size", defaultValue = "1000") Integer size,
                                                   @GraphQLArgument(name = "sortBy", defaultValue = OperationVO.PROPERTY_START_DATE_TIME) String sort,
                                                   @GraphQLArgument(name = "sortDirection", defaultValue = "asc") String direction) {
        Preconditions.checkNotNull(filter, "Missing tripFilter or tripFilter.tripId");
        Preconditions.checkNotNull(filter.getTripId(), "Missing tripFilter or tripFilter.tripId");
        List<OperationVO> res = operationService.getAllByTripId(filter.getTripId(), offset, size, sort, direction != null ? SortDirection.valueOf(direction.toUpperCase()) : null);
        return res;
    }

    @GraphQLQuery(name = "operations", description = "Get trip's operations")
    public List<OperationVO> getOperationsByTrip(@GraphQLContext TripVO trip) {
        return operationService.getAllByTripId(trip.getId(), 0, 100, OperationVO.PROPERTY_START_DATE_TIME, SortDirection.ASC);
    }

    @GraphQLQuery(name = "operation", description = "Get an operation")
    @Transactional(readOnly = true)
    @IsUser
    public OperationVO getOperation(@GraphQLArgument(name = "id") int id) {
        return operationService.get(id);
    }

    @GraphQLMutation(name = "saveOperations", description = "Save operations")
    @IsUser
    public List<OperationVO> saveOperations(@GraphQLArgument(name = "operations") List<OperationVO> operations) {
        return operationService.save(operations);
    }

    @GraphQLMutation(name = "saveOperation", description = "Create or update an operation")
    @IsUser
    public OperationVO saveOperation(@GraphQLArgument(name = "operation") OperationVO operation) {
        return operationService.save(operation);
    }

    @GraphQLMutation(name = "deleteOperation", description = "Delete an operation")
    @IsUser
    public void deleteOperation(@GraphQLArgument(name = "id") int id) {
        operationService.delete(id);
    }

    @GraphQLMutation(name = "deleteOperations", description = "Delete many operations")
    @IsUser
    public void deleteOperations(@GraphQLArgument(name = "ids") List<Integer> ids) {
        operationService.delete(ids);
    }

    @GraphQLSubscription(name = "updateOperation", description = "Subscribe to changes on an operation")
    @IsUser
    public Publisher<OperationVO> updateOperation(@GraphQLArgument(name = "id") final int id,
                                        @GraphQLArgument(name = "interval", defaultValue = "30", description = "Minimum interval to get changes, in seconds.") final Integer minIntervalInSecond) {

        Preconditions.checkArgument(id >= 0, "Invalid id");
        return changesPublisherService.getPublisher(Operation.class, OperationVO.class, id, minIntervalInSecond, true);
    }

    /* -- Vessel position -- */

    @GraphQLQuery(name = "positions", description = "Get operation's position")
    public List<VesselPositionVO> getPositionsByOperation(@GraphQLContext OperationVO operation) {
        // Avoid a reloading (e.g. when saving)
        if (CollectionUtils.isNotEmpty(operation.getPositions())) {
            return operation.getPositions();
        }
        return vesselPositionService.getAllByOperationId(operation.getId(), 0, 100, VesselPositionVO.PROPERTY_DATE_TIME, SortDirection.ASC);
    }

    /* -- Vessel features -- */

    @GraphQLQuery(name = "vesselFeatures", description = "Get trip vessel features")
    public VesselFeaturesVO getVesselFeaturesByTrip(@GraphQLContext TripVO trip) {
        return vesselService.getByVesselIdAndDate(trip.getVesselFeatures().getVesselId(), trip.getDepartureDateTime());
    }

    /* -- Sample -- */

    @GraphQLQuery(name = "samples", description = "Get operation's samples")
    public List<SampleVO> getSamplesByOperation(@GraphQLContext OperationVO operation) {
        // Avoid a reloading (e.g. when saving)
        if (CollectionUtils.isNotEmpty(operation.getSamples())) {
            return operation.getSamples();
        }

        return sampleService.getAllByOperationId(operation.getId());
    }

    /* -- Batch -- */

    @GraphQLQuery(name = "batches", description = "Get operation's batches")
    public List<BatchVO> getBatchesByOperation(@GraphQLContext OperationVO operation) {
        // Avoid a reloading (e.g. when saving): reuse existing VO
        if (CollectionUtils.isNotEmpty(operation.getBatches())) {
            return operation.getBatches();
        }

        // Reload, if not exist in VO
        return batchService.getAllByOperationId(operation.getId());
    }

    /* -- Measurements -- */

    @GraphQLQuery(name = "measurements", description = "Get trip's measurements")
    public List<MeasurementVO> getTripMeasurements(@GraphQLContext TripVO trip) {
        return measurementService.getVesselUseMeasurementsByTripId(trip.getId());
    }

    @GraphQLQuery(name = "measurements", description = "Get operation's measurements")
    public List<MeasurementVO> getOperationMeasurements(@GraphQLContext OperationVO operation) {
        return measurementService.getVesselUseMeasurementsByOperationId(operation.getId());
    }

    @GraphQLQuery(name = "gearMeasurements", description = "Get operation's gear measurements")
    public List<MeasurementVO> getOperationGearUseMeasurements(@GraphQLContext OperationVO operation) {
        return measurementService.getGearUseMeasurementsByOperationId(operation.getId());
    }

    @GraphQLQuery(name = "measurements", description = "Get physical gear measurements")
    public List<MeasurementVO> getPhysicalGearMeasurements(@GraphQLContext PhysicalGearVO physicalGear) {
        return measurementService.getPhysicalGearMeasurements(physicalGear.getId());
    }

    @GraphQLQuery(name = "measurements", description = "Get sample measurements")
    public List<MeasurementVO> getSampleMeasurements(@GraphQLContext SampleVO sample) {
        return measurementService.getSampleMeasurements(sample.getId());
    }

    @GraphQLQuery(name = "measurementValues", description = "Get measurement values (as a key/value map, using pmfmId as key)")
    public Map<Integer, String> getSampleMeasurementValues(@GraphQLContext SampleVO sample) {
        if (MapUtils.isEmpty(sample.getMeasurementValues())) {
            return measurementService.getSampleMeasurementsMap(sample.getId());
        }
        return sample.getMeasurementValues();
    }

    @GraphQLQuery(name = "measurementValues", description = "Get measurement values (as a key/value map, using pmfmId as key)")
    public Map<Integer, String> getBatchMeasurementValues(@GraphQLContext BatchVO batch) {
        if (MapUtils.isEmpty(batch.getMeasurementValues())) {
            Map<Integer, String> map = Maps.newHashMap();
            map.putAll(measurementService.getBatchSortingMeasurementsMap(batch.getId()));
            map.putAll(measurementService.getBatchQuantificationMeasurementsMap(batch.getId()));
            return map;
        }
        return batch.getMeasurementValues();
    }

//    @GraphQLQuery(name = "measurementValues", description = "Get measurement values (as a key/value map, using pmfmId as key)")
//    public List<Map.Entry<Integer, Object>> getBatchMeasurementsValues(@GraphQLContext BatchVO sample) {
//        Map<Integer, Object> map = Maps.newHashMap();
//        map.putAll(measurementService.getBatchSortingMeasurementsMap(sample.getId()));
//        map.putAll(measurementService.getBatchQuantificationMeasurementsMap(sample.getId()));
//        return ImmutableList.copyOf(map.entrySet());
//    }

    // TODO: remove if not used
//    @GraphQLQuery(name = "sortingMeasurementValues", description = "Get sorting measurement values (as a key/value map, using pmfmId as key)")
//    public Map<Integer, Object> getSortingMeasurementsMap(@GraphQLContext BatchVO sample) {
//        return measurementService.getBatchSortingMeasurementsMap(sample.getId());
//    }
//    @GraphQLQuery(name = "quantificationMeasurementValues", description = "Get quantification measurement values (as a key/value map, using pmfmId as key)")
//    public Map<Integer, Object> getQuantificationMeasurementsMap(@GraphQLContext BatchVO sample) {
//        return measurementService.getBatchQuantificationMeasurementsMap(sample.getId());
//    }

    @GraphQLQuery(name = "pmfm", description = "Get measurement's pmfm")
    public PmfmVO getMeasurementPmfm(@GraphQLContext MeasurementVO measurement) {
        return pmfmService.get(measurement.getPmfmId());
    }

    /* -- protected methods -- */

    protected void fillTripFields(TripVO trip, Set<String> fields) {
        // Add image if need
        if (hasImageField(fields)) fillImages(trip);

        // Add vessel if need
        if (hasVesselFeaturesField(fields) && trip.getVesselFeatures() != null && trip.getVesselFeatures().getVesselId() != null) {
            trip.setVesselFeatures(vesselService.getByVesselIdAndDate(trip.getVesselFeatures().getVesselId(), trip.getDepartureDateTime()));
        }
    }

    protected void fillTrips(List<TripVO> trips, Set<String> fields) {
        // Add image if need
        if (hasImageField(fields)) fillImages(trips);

        // Add vessel if need
        if (hasVesselFeaturesField(fields)) {
            trips.forEach(t -> {
                if (t.getVesselFeatures().getVesselId() != null) {
                    t.setVesselFeatures(vesselService.getByVesselIdAndDate(t.getVesselFeatures().getVesselId(), t.getDepartureDateTime()));
                }
            });
        }
    }

    protected void fillObservedLocationFields(ObservedLocationVO observedLocation, Set<String> fields) {
        // Add image if need
        if (hasImageField(fields)) fillImages(observedLocation);
    }

    protected void fillObservedLocationsFields(List<ObservedLocationVO> observedLocations, Set<String> fields) {
        // Add image if need
        if (hasImageField(fields)) fillImages(observedLocations);
    }

    protected boolean hasImageField(Set<String> fields) {
        return fields.contains(TripVO.PROPERTY_RECORDER_DEPARTMENT + File.separator + DepartmentVO.PROPERTY_LOGO) ||
                fields.contains(TripVO.PROPERTY_RECORDER_PERSON + File.separator + PersonVO.PROPERTY_AVATAR);
    }

    protected boolean hasVesselFeaturesField(Set<String> fields) {
        return fields.contains(TripVO.PROPERTY_VESSEL_FEATURES + File.separator + VesselFeaturesVO.PROPERTY_EXTERIOR_MARKING)
                || fields.contains(TripVO.PROPERTY_VESSEL_FEATURES + File.separator + VesselFeaturesVO.PROPERTY_NAME);
    }

    protected <T extends IRootDataVO<?>> List<T> fillImages(final List<T> results) {
        results.forEach(this::fillImages);
        return results;
    }

    protected <T extends IRootDataVO<?>> T fillImages(T result) {
        if (result != null) {

            // Fill avatar on recorder department (if not null)
            imageService.fillLogo(result.getRecorderDepartment());

            // Fill avatar on recorder persons (if not null)
            imageService.fillAvatar(result.getRecorderPerson());
        }

        return result;
    }

    protected DataFetchOptions getFetchOptions(Set<String> fields) {
        return DataFetchOptions.builder()
                .withObservers(fields.contains(IWithObserversEntityBean.PROPERTY_OBSERVERS + "/" + IDataEntity.PROPERTY_ID))
                .withRecorderDepartment(fields.contains(IWithRecorderDepartmentEntityBean.PROPERTY_RECORDER_DEPARTMENT + "/" + IDataEntity.PROPERTY_ID))
                .withRecorderPerson(fields.contains(IWithRecorderPersonEntityBean.PROPERTY_RECORDER_PERSON + "/" + IDataEntity.PROPERTY_ID))
                .build();
    }
}