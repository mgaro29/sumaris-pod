package net.sumaris.core.dao.data;

/*-
 * #%L
 * SUMARiS:: Core
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

import net.sumaris.core.dao.technical.model.IEntity;
import net.sumaris.core.model.administration.programStrategy.Program;
import net.sumaris.core.model.data.PhysicalGear;
import net.sumaris.core.model.data.Trip;
import net.sumaris.core.util.StringUtils;
import net.sumaris.core.vo.data.PhysicalGearVO;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public interface PhysicalGearRepositoryExtend
    extends IEntityConverter<PhysicalGear, PhysicalGearVO> {

    default Specification<PhysicalGear> hasVesselId(Integer vesselId) {
        if (vesselId == null) return null;
        return (root, query, cb) -> cb.equal(root.get(PhysicalGear.Fields.TRIP).get(Trip.Fields.VESSEL).get(IEntity.Fields.ID), vesselId);
    }

    default Specification<PhysicalGear> hasTripId(Integer tripId) {
        if (tripId == null) return null;
        return (root, query, cb) -> cb.equal(root.get(PhysicalGear.Fields.TRIP).get(Trip.Fields.ID), tripId);
    }

    default Specification<PhysicalGear> programLabel(String programLabel) {
        if (StringUtils.isBlank(programLabel)) return null;
        return (root, query, cb) -> cb.equal(root.get(PhysicalGear.Fields.TRIP)
                .get(Trip.Fields.PROGRAM).get(Program.Fields.LABEL), programLabel.trim());
    }


    default Specification<PhysicalGear> betweenDate(Date startDate, Date endDate) {
        if (startDate == null && endDate == null) return null;
        return (root, query, cb) -> {
            // Start + end date
            if (startDate != null && endDate != null) {
                return cb.not(
                    cb.or(
                        cb.lessThan(root.get(PhysicalGear.Fields.TRIP).get(Trip.Fields.RETURN_DATE_TIME), startDate),
                        cb.greaterThan(root.get(PhysicalGear.Fields.TRIP).get(Trip.Fields.DEPARTURE_DATE_TIME), endDate)
                    )
                );
            }
            // Start date
            else if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get(PhysicalGear.Fields.TRIP).get(Trip.Fields.DEPARTURE_DATE_TIME), startDate);
            }
            // End date
            else  {
                return cb.lessThanOrEqualTo(root.get(PhysicalGear.Fields.TRIP).get(Trip.Fields.RETURN_DATE_TIME), endDate);
            }
        };
    }
}
