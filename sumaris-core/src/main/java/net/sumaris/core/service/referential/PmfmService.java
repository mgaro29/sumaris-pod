package net.sumaris.core.service.referential;

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

import net.sumaris.core.vo.referential.PmfmVO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PmfmService {

    @Transactional(readOnly = true)
    PmfmVO getByLabel(String label);

    @Transactional(readOnly = true)
    PmfmVO get(int pmfmId);

    @Transactional(readOnly = true)
    boolean isWeightPmfm(int pmfmId);

    @Transactional(readOnly = true)
    boolean isSortingPmfm(int pmfmId);

    @Transactional(readOnly = true)
    boolean isQuantificationPmfm(int pmfmId);

    @Transactional(readOnly = true)
    boolean isCalculatedPmfm(int pmfmId);

    @Transactional(readOnly = true)
    boolean isVesselUsePmfm(int pmfmId);

    @Transactional(readOnly = true)
    boolean isGearUsePmfm(int pmfmId);

    @Transactional(readOnly = true)
    boolean isGearPhysicalPmfm(int pmfmId);

}
