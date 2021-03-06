package net.sumaris.core.dao.data;

/*-
 * #%L
 * SUMARiS:: Core
 * %%
 * Copyright (C) 2018 - 2020 SUMARiS Consortium
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


import net.sumaris.core.dao.referential.ReferentialDao;
import net.sumaris.core.dao.referential.location.LocationDao;
import net.sumaris.core.dao.technical.jpa.SumarisJpaRepositoryImpl;
import net.sumaris.core.model.data.FishingArea;
import net.sumaris.core.model.data.Operation;
import net.sumaris.core.model.referential.DepthGradient;
import net.sumaris.core.model.referential.DistanceToCoastGradient;
import net.sumaris.core.model.referential.NearbySpecificArea;
import net.sumaris.core.model.referential.location.Location;
import net.sumaris.core.vo.data.FishingAreaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author peck7 on 09/06/2020.
 */
public class FishingAreaRepositoryImpl
    extends SumarisJpaRepositoryImpl<FishingArea, Integer, FishingAreaVO>
    implements FishingAreaRepositoryExtend {

    private static final Logger log =
        LoggerFactory.getLogger(FishingAreaRepositoryImpl.class);

    private final LocationDao locationDao;
    private final ReferentialDao referentialDao;

    @Autowired
    @Lazy
    private FishingAreaRepository loopBack;

    @Autowired
    public FishingAreaRepositoryImpl(EntityManager entityManager, LocationDao locationDao, ReferentialDao referentialDao) {
        super(FishingArea.class, entityManager);
        this.locationDao = locationDao;
        this.referentialDao = referentialDao;
    }

    @Override
    public Class<FishingAreaVO> getVOClass() {
        return FishingAreaVO.class;
    }

    @Override
    public void toVO(FishingArea source, FishingAreaVO target, boolean copyIfNull) {
        super.toVO(source, target, copyIfNull);

        target.setLocation(locationDao.toLocationVO(source.getLocation()));

        if (source.getDistanceToCoastGradient() != null)
            target.setDistanceToCoastGradient(referentialDao.toReferentialVO(source.getDistanceToCoastGradient()));
        if (source.getDepthGradient() != null)
            target.setDepthGradient(referentialDao.toReferentialVO(source.getDepthGradient()));
        if (source.getNearbySpecificArea() != null)
            target.setNearbySpecificArea(referentialDao.toReferentialVO(source.getNearbySpecificArea()));

        if (source.getOperation() != null)
            target.setOperationId(source.getOperation().getId());
    }

    @Override
    public void toEntity(FishingAreaVO source, FishingArea target, boolean copyIfNull) {
        super.toEntity(source, target, copyIfNull);

        if (copyIfNull || source.getLocation() != null) {
            if (source.getLocation() == null || source.getLocation().getId() == null) {
                target.setLocation(null);
            } else {
                target.setLocation(load(Location.class, source.getLocation().getId()));
            }
        }

        if (copyIfNull || source.getDistanceToCoastGradient() != null) {
            if (source.getDistanceToCoastGradient() == null || source.getDistanceToCoastGradient().getId() == null) {
                target.setDistanceToCoastGradient(null);
            } else {
                target.setDistanceToCoastGradient(load(DistanceToCoastGradient.class, source.getDistanceToCoastGradient().getId()));
            }
        }

        if (copyIfNull || source.getDepthGradient() != null) {
            if (source.getDepthGradient() == null || source.getDepthGradient().getId() == null) {
                target.setDepthGradient(null);
            } else {
                target.setDepthGradient(load(DepthGradient.class, source.getDepthGradient().getId()));
            }
        }

        if (copyIfNull || source.getNearbySpecificArea() != null) {
            if (source.getNearbySpecificArea() == null || source.getNearbySpecificArea().getId() == null) {
                target.setNearbySpecificArea(null);
            } else {
                target.setNearbySpecificArea(load(NearbySpecificArea.class, source.getNearbySpecificArea().getId()));
            }
        }

        // parent operation
        Integer operationId = source.getOperationId() != null ? source.getOperationId() : (source.getOperation() != null ? source.getOperation().getId() : null);
        source.setOperationId(operationId);
        if (copyIfNull || (operationId != null)) {
            if (operationId == null) {
                target.setOperation(null);
            } else {
                target.setOperation(load(Operation.class, operationId));
            }
        }

    }

    @Override
    public List<FishingAreaVO> getAllVOByOperationId(Integer operationId) {
        return loopBack.getAllByOperationId(operationId).stream()
            .map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<FishingAreaVO> saveAllByOperationId(int operationId, @Nonnull List<FishingAreaVO> fishingAreas) {

        // Save only non null objects
        List<FishingAreaVO> fishingAreasToSave = fishingAreas.stream().filter(Objects::nonNull).collect(Collectors.toList());

        // Set parent link
        fishingAreasToSave.forEach(fishingArea -> fishingArea.setOperationId(operationId));

        // Get existing fishing areas
        Set<Integer> existingFishingAreaIds = loopBack.getAllByOperationId(operationId).stream().map(FishingArea::getId).collect(Collectors.toSet());

        // Save
        fishingAreasToSave.forEach(fishingArea -> {
            save(fishingArea);
            existingFishingAreaIds.remove(fishingArea.getId());
        });

        // Delete remaining objects
        existingFishingAreaIds.forEach(this::deleteById);

        return fishingAreasToSave;
    }
}
