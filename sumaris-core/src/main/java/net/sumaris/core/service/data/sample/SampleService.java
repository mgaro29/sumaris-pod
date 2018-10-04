package net.sumaris.core.service.data.sample;

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


import net.sumaris.core.vo.data.SampleVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author BLA
 * 
 */
@Transactional
public interface SampleService {


	@Transactional(readOnly = true)
	List<SampleVO> getAllByOperationId(int operationId);

	List<SampleVO> saveByOperationId(int operationId, List<SampleVO> samples);

	@Transactional(readOnly = true)
	SampleVO get(int id);

	SampleVO save(SampleVO sale);

	List<SampleVO> save(List<SampleVO> samples);

	void delete(int id);

	void delete(List<Integer> ids);

}
