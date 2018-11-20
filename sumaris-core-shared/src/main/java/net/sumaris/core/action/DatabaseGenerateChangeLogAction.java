package net.sumaris.core.action;

/*
 * #%L
 * SIH-Adagio :: Shared
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2014 Ifremer
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import net.sumaris.core.config.SumarisConfiguration;
import net.sumaris.core.dao.schema.DatabaseSchemaDao;
import net.sumaris.core.dao.schema.DatabaseSchemaDaoImpl;
import net.sumaris.core.dao.technical.Daos;
import net.sumaris.core.exception.SumarisTechnicalException;
import net.sumaris.core.exception.VersionNotFoundException;
import net.sumaris.core.service.ServiceLocator;
import net.sumaris.core.service.schema.DatabaseSchemaService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuiton.version.Version;

import java.io.File;

/**
 * <p>DatabaseChangeLogAction class.</p>
 *
 */
public class DatabaseGenerateChangeLogAction {
    /* Logger */
    private static final Log log = LogFactory.getLog(DatabaseCreateSchemaAction.class);

    /**
     * <p>run.</p>
     */
    public void run() {
        SumarisConfiguration config = SumarisConfiguration.getInstance();
        
        if (log.isInfoEnabled()) {            
            log.info("Starting change log file generation...");        
        }
        ActionUtils.logConnectionProperties();

        boolean isValidConnection = Daos.isValidConnectionProperties(config.getJdbcDriver(),
                config.getJdbcURL(),
                config.getJdbcUsername(),
                config.getJdbcPassword()); 

        if (!isValidConnection) {
            log.warn("Connection error: could not generate changelog file.");
            return;
        }

        DatabaseSchemaService service = ServiceLocator.instance().getDatabaseSchemaService();

        // Check if database is well loaded
        if (!service.isDbLoaded()) {
            log.warn("Database not start ! Could not generate changelog file.");
            return;
        }

        try {
            Version actualDbVersion = service.getDbVersion();
            if (actualDbVersion != null) {
                log.info("Database schema version is: " + actualDbVersion.toString());
            }

            Version modelVersion = config.getVersion();
            log.info("Model version is: " + modelVersion.toString());
        } catch (VersionNotFoundException e) {
            log.error("Error while getting versions.", e);
        }

        File outputFile = ActionUtils.checkAndGetOutputFile(false, this.getClass());
        
        try {
            log.info("Launching changelog file generation...");
            service.generateDiffChangeLog(outputFile);
            if (outputFile != null) {
                log.info(String.format("Database changelog file successfully generated at %s", outputFile));
            }
            else {
                log.info("Database changelog file successfully generated.");
            }
        } catch (SumarisTechnicalException e) {
            log.error("Error while generating changelog file.", e);
        }
    }
}