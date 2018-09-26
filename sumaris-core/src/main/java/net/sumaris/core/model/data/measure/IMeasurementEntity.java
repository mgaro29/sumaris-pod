package net.sumaris.core.model.data.measure;

/*-
 * #%L
 * SUMARiS:: Core
 * %%
 * Copyright (C) 2018 SUMARiS Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import net.sumaris.core.model.data.IDataEntity;
import net.sumaris.core.model.data.IRootDataEntity;
import net.sumaris.core.model.referential.Pmfm;
import net.sumaris.core.model.referential.QualitativeValue;

public interface IMeasurementEntity extends IDataEntity<Integer> {

    String PROPERTY_PMFM = "pmfm";
    String PROPERTY_NUMERICAL_VALUE = "numericalValue";
    String PROPERTY_ALPHANUMERICAL_VALUE = "alphanumericalValue";
    String PROPERTY_DIGIT_COUNT = "digitCount";
    String PROPERTY_PRECISION_VALUE = "precisionValue";

    Double getNumericalValue();

    void setNumericalValue(Double numericalValue);

    String getAlphanumericalValue();

    void setAlphanumericalValue(String alphanumericalValue);

    Integer getDigitCount();

    void setDigitCount(Integer digitCount);

    Double getPrecisionValue();

    void setPrecisionValue(Double precisionValue);

    QualitativeValue getQualitativeValue();

    void setQualitativeValue(QualitativeValue qualitativeValue);

    Pmfm getPmfm();

    void setPmfm(Pmfm pmfm);

}
