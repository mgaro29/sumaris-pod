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

import net.sumaris.core.dao.technical.Page;
import net.sumaris.core.model.data.PhysicalGear;
import net.sumaris.core.vo.data.DataFetchOptions;
import net.sumaris.core.vo.data.PhysicalGearVO;
import net.sumaris.core.vo.filter.PhysicalGearFilterVO;

import java.util.List;

public interface PhysicalGearDao {

    List<PhysicalGearVO> findAll(PhysicalGearFilterVO filter, Page page, DataFetchOptions fetchOptions);

    List<PhysicalGearVO> getAllByTripId(int operationId);

    PhysicalGearVO save(PhysicalGearVO source);

    List<PhysicalGearVO> save(int tripId, List<PhysicalGearVO> sources);

    PhysicalGearVO toVO(PhysicalGear source, boolean withDetails);

    PhysicalGearVO toVO(PhysicalGear source);
}
