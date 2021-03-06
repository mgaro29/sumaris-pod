package net.sumaris.core.dao.technical;

/*-
 * #%L
 * SUMARiS:: Core shared
 * %%
 * Copyright (C) 2018 - 2019 SUMARiS Consortium
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

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

/**
 * Helper class
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>*
 */
public class Pageables  {

    protected Pageables() {

    }

    public static Pageable create(int offset, int size, String sortAttribute, SortDirection sortDirection) {
        if (sortAttribute != null) {
            return PageRequest.of(offset / size, size,
                    (sortDirection == null) ? Sort.Direction.ASC :
                            Sort.Direction.fromString(sortDirection.toString()),
                    sortAttribute);
        }
        return PageRequest.of(offset / size, size);
    }
}

