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

import net.sumaris.core.dao.technical.SortDirection;
import net.sumaris.core.model.data.Operation;
import net.sumaris.core.vo.data.OperationVO;

import java.util.List;

public interface OperationDao {

    List<OperationVO> getAllByTripId(int tripId, int offset, int size, String sortAttribute, SortDirection sortDirection);

    Long countByTripId(int tripId);

    OperationVO get(int id);

    void delete(int id);

    OperationVO save(OperationVO trip);

    List<OperationVO> saveAllByTripId(int tripId, List<OperationVO> operations);

    OperationVO toVO(Operation operation);
}
