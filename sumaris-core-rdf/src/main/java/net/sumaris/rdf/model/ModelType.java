package net.sumaris.rdf.model;

/*-
 * #%L
 * SUMARiS:: RDF features
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


import com.google.common.base.Preconditions;

public enum ModelType {

    SCHEMA,
    DATA;

    public static ModelType fromUserString(String userType) {
        Preconditions.checkNotNull(userType);

        switch(userType.toLowerCase()) {
            case "voc":
            case "vocabulary":
            case "term":
            case "terms":
            case "schema":
                return SCHEMA;
            case "data":
            case "entities":
            case "object":
                return DATA;
            default:
                throw new IllegalArgumentException("Unknown model type: " + userType);
        }
    }
}
