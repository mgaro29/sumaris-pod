package net.sumaris.core.service.data.batch;

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


import com.google.common.base.Preconditions;
import net.sumaris.core.dao.data.MeasurementDao;
import net.sumaris.core.dao.data.batch.BatchDao;
import net.sumaris.core.dao.technical.Beans;
import net.sumaris.core.model.data.batch.BatchQuantificationMeasurement;
import net.sumaris.core.model.data.batch.BatchSortingMeasurement;
import net.sumaris.core.model.data.measure.IMeasurementEntity;
import net.sumaris.core.vo.data.BatchVO;
import net.sumaris.core.vo.data.MeasurementVO;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("batchService")
public class BatchServiceImpl implements BatchService {

	private static final Log log = LogFactory.getLog(BatchServiceImpl.class);

	@Autowired
	protected BatchDao batchDao;

	@Autowired
	protected MeasurementDao measurementDao;

	@Override
	public List<BatchVO> getAllByOperationId(int operationId) {
		return batchDao.getAllByOperationId(operationId);
	}

	@Override
	public BatchVO get(int saleId) {
		return batchDao.get(saleId);
	}

	@Override
	public List<BatchVO> saveByOperationId(int operationId, List<BatchVO> sources) {

		List<BatchVO> result = batchDao.saveByOperationId(operationId, sources);

		// Save measurements
		result.stream().forEach(savedBatch -> {
			// FIXME: use 2 maps, instead of one
			// Sorting+Quantification measurements
			if (savedBatch.getMeasurementValues() != null) {
				measurementDao.saveBatchSortingMeasurementsMap(savedBatch.getId(), savedBatch.getMeasurementValues());
			}
			else {
				// Sorting measurement
				if (savedBatch.getSortingMeasurements() != null) {
					measurementDao.saveBatchSortingMeasurementsMap(savedBatch.getId(), savedBatch.getSortingMeasurementValues());
				} else {
					List<MeasurementVO> measurements = Beans.getList(savedBatch.getSortingMeasurements());
					measurements.forEach(m -> fillDefaultProperties(savedBatch, m, BatchSortingMeasurement.class));
					measurements = measurementDao.saveBatchSortingMeasurements(savedBatch.getId(), measurements);
					savedBatch.setSortingMeasurements(measurements);
				}

				// Quantification measurement
				if (savedBatch.getQuantificationMeasurements() != null) {
					measurementDao.saveBatchQuantificationMeasurementsMap(savedBatch.getId(), savedBatch.getQuantificationMeasurementValues());
				} else {
					List<MeasurementVO> measurements = Beans.getList(savedBatch.getQuantificationMeasurements());
					measurements.forEach(m -> fillDefaultProperties(savedBatch, m, BatchQuantificationMeasurement.class));
					measurements = measurementDao.saveBatchQuantificationMeasurements(savedBatch.getId(), measurements);
					savedBatch.setQuantificationMeasurements(measurements);
				}
			}
		});

		return result;
	}

	@Override
	public BatchVO save(BatchVO batch) {
		Preconditions.checkNotNull(batch);
		Preconditions.checkArgument((batch.getOperation() != null && batch.getOperation().getId() != null) || batch.getOperationId() != null, "Missing batch.operation or batch.operationId");
		Preconditions.checkNotNull(batch.getRecorderDepartment(), "Missing batch.recorderDepartment");
		Preconditions.checkNotNull(batch.getRecorderDepartment().getId(), "Missing batch.recorderDepartment.id");

		return batchDao.save(batch);
	}

	@Override
	public List<BatchVO> save(List<BatchVO> sales) {
		Preconditions.checkNotNull(sales);

		return sales.stream()
				.map(this::save)
				.collect(Collectors.toList());
	}

	@Override
	public void delete(int id) {
		batchDao.delete(id);
	}

	@Override
	public void delete(List<Integer> ids) {
		Preconditions.checkNotNull(ids);
		ids.stream()
				.filter(Objects::nonNull)
				.forEach(this::delete);
	}

	/* -- protected methods -- */

	protected void fillDefaultProperties(BatchVO parent, MeasurementVO measurement, Class<? extends IMeasurementEntity> entityClass) {
		if (measurement == null) return;

		// Copy recorder department from the parent
		if (measurement.getRecorderDepartment() == null || measurement.getRecorderDepartment().getId() == null) {
			measurement.setRecorderDepartment(parent.getRecorderDepartment());
		}

		measurement.setOperationId(parent.getId());
		measurement.setEntityName(entityClass.getSimpleName());
	}
}