/*
 * #%L
 * SUMARiS
 * %%
 * Copyright (C) 2019 SUMARiS Consortium
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

package net.sumaris.core.model;

import java.io.Serializable;
import java.util.Arrays;

public enum QualityFlagEnum implements Serializable {

    NOT_QUALIFED(0, "Not qualified"),
    GOOD(1, "Good"),
    OUT_STATS(2, "Out of statistics"),
    DOUBTFUL(3, "Doubtful"),
    BAD(4, "Bad"),
    FIXED(5, "Fixed"),
    NOT_COMPLETED(8, "Not completed"),
    MISSING(9, "Missing")
    ;

    public static QualityFlagEnum valueOf(final int id) {
        return Arrays.stream(values())
                .filter(level -> level.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown QualityFlagEnum: " + id));
    }

    public static QualityFlagEnum byLabel(final String label) {
        return Arrays.stream(values())
                .filter(level -> label.equals(level.label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown QualityFlagEnum: " + label));
    }

    private int id;
    private String label;

    QualityFlagEnum(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
