package net.sumaris.core.model.administration.programStrategy;

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

import java.io.Serializable;
import java.util.Arrays;

public enum ProgramEnum implements Serializable {

    SIH(0);

    private int id;

    ProgramEnum(int id) {
        this.id = id;
    }

    /**
     * Returns the database row id
     *
     * @return int the id
     */
    public int getId()
    {
        return this.id;
    }


    public static ProgramEnum valueOf(final int id) {
        switch (id) {
            case 0: return SIH;
        }
        throw new IllegalArgumentException("Unknown ProgramEnum: " + id);
    }

    public static ProgramEnum fromLabel(final String label) {
        return Arrays.stream(values()).filter(item -> item.name().equalsIgnoreCase(label)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ProgramEnum label: " + label));
    }
}