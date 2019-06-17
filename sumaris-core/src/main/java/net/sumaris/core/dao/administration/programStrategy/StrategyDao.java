package net.sumaris.core.dao.administration.programStrategy;

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

import net.sumaris.core.dao.cache.CacheNames;
import net.sumaris.core.model.administration.programStrategy.PmfmStrategy;
import net.sumaris.core.model.administration.programStrategy.Program;
import net.sumaris.core.vo.administration.programStrategy.PmfmStrategyVO;
import net.sumaris.core.vo.administration.programStrategy.ProgramVO;
import net.sumaris.core.vo.referential.ReferentialVO;
import org.springframework.cache.annotation.Cacheable;

import java.util.Date;
import java.util.List;

public interface StrategyDao {

    @Cacheable(cacheNames = CacheNames.PMFM_BY_PROGRAM_ID, key = "#programId", unless = "#result == null")
    List<PmfmStrategyVO> getPmfmStrategies(int programId);

    List<PmfmStrategyVO> getPmfmStrategiesByAcquisitionLevel(int programId, int acquisitionLevelId);

    List<ReferentialVO> getGears(int programId);

    List<ReferentialVO> getTaxonGroups(int programId);

    PmfmStrategyVO toPmfmStrategyVO(PmfmStrategy source);

}
