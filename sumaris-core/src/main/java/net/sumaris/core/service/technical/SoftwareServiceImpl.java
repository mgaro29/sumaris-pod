package net.sumaris.core.service.technical;

/*-
 * #%L
 * SUMARiS:: Core
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

import com.google.common.base.Preconditions;
import net.sumaris.core.config.SumarisConfiguration;
import net.sumaris.core.config.SumarisConfigurationOption;
import net.sumaris.core.dao.schema.DatabaseSchemaDao;
import net.sumaris.core.dao.schema.event.DatabaseSchemaListener;
import net.sumaris.core.dao.schema.event.SchemaUpdatedEvent;
import net.sumaris.core.dao.technical.SoftwareDao;
import net.sumaris.core.exception.VersionNotFoundException;
import net.sumaris.core.vo.technical.SoftwareVO;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuiton.config.ApplicationConfig;
import org.nuiton.config.ApplicationConfigHelper;
import org.nuiton.config.ApplicationConfigProvider;
import org.nuiton.config.ConfigOptionDef;
import org.nuiton.version.Version;
import org.nuiton.version.VersionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component("softwareService")
public class SoftwareServiceImpl implements SoftwareService, DatabaseSchemaListener {

    private static final Log log = LogFactory.getLog(SoftwareServiceImpl.class);

    @Autowired
    private SoftwareDao dao;

    @Autowired
    private DatabaseSchemaDao databaseSchemaDao;

    private String defaultSoftwareLabel;


    public SoftwareServiceImpl(SumarisConfiguration configuration) {
        this.defaultSoftwareLabel = configuration.getAppName();
        Preconditions.checkNotNull(defaultSoftwareLabel);
    }

    @PostConstruct
    protected void init() {
        databaseSchemaDao.addListener(this);
    }

    @Override
    public void onSchemaUpdated(SchemaUpdatedEvent event) {
        overrideAppConfigFromDatabase(event.getSchemaVersion());
    }

    @Override
    public SoftwareVO getDefault() {
        return dao.getByLabel(defaultSoftwareLabel);
    }

    @Override
    public SoftwareVO get(int id) {
        return dao.get(id);
    }

    @Override
    public SoftwareVO getByLabel(String label) {
        Preconditions.checkNotNull(label);

        return dao.getByLabel(label);
    }

    @Override
    public SoftwareVO save(SoftwareVO source) {
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(source.getLabel());

        SoftwareVO result = dao.save(source);

        // If default
        if (Objects.equals(result.getLabel(), defaultSoftwareLabel)) {
            // Apply to appConfig
            overrideAppConfigFromProperties(result.getLabel(), result.getProperties());
        }

        return result;
    }

    /**
     * Auto detect IP
     *
     * @return the IP address or null
     */
    @Bean
    private Optional<String> whatsMyIp() {

        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            return Optional.of(in.readLine());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected boolean overrideAppConfigFromDatabase(@Nullable Version dbVersion) {

        try {
            dbVersion = dbVersion != null ? dbVersion : databaseSchemaDao.getSchemaVersion();
            Version minVersion = VersionBuilder.create("0.9.5").build();

            // Test if software table exists, if not, skip
            if (dbVersion == null || minVersion.after(dbVersion)) {
                log.warn(String.format("Skipping configuration override from database (expected min schema version {%s}). Waiting schema update...", minVersion.toString()));
                return false; // KO: will retry after schema update
            }
        } catch(VersionNotFoundException e) {
            // ok, continue (schema should be a new one)
        }

        // Override the configuration existing in the config file, using DB
        SoftwareVO software = getDefault();
        if (software == null) {
            log.info(String.format("No configuration for {%s} found in database. to enable configuration override from database, make sure to set the option '%s' to an existing row of the table SOFTWARE (column LABEL).", defaultSoftwareLabel, SumarisConfigurationOption.APP_NAME.getKey()));
            return true; // skip
        }

        // Apply properties to appConfig
        overrideAppConfigFromProperties(software.getLabel(), software.getProperties());

        return true;
    }

    protected void overrideAppConfigFromProperties(String label, Map<String, String> properties) {
        Preconditions.checkNotNull(label); // Need by log message
        if (MapUtils.isEmpty(properties)) return; // Skip if empty - TODO: applying defaults ?

        ApplicationConfig appConfig = SumarisConfiguration.getInstance().getApplicationConfig();

        log.info(String.format("Overriding configuration options, using those found in {%s} software's properties", label));

        // Load options from configuration providers
        Set<ApplicationConfigProvider> providers =
                ApplicationConfigHelper.getProviders(null,
                        null,
                        null,
                        true);
        Set<String> optionKeys = providers.stream()
                .map(ApplicationConfigProvider::getOptions)
                .flatMap(Stream::of)
                .map(ConfigOptionDef::getKey)
                .collect(Collectors.toSet());
        Set<String> transientOptionKeys = providers.stream()
                .map(ApplicationConfigProvider::getOptions)
                .flatMap(Stream::of)
                .filter(ConfigOptionDef::isTransient)
                .map(ConfigOptionDef::getKey)
                .collect(Collectors.toSet());

        boolean info = log.isInfoEnabled();

        properties.forEach((key, value) -> {
            if (!optionKeys.contains(key)) {
                if (info) log.info(String.format(" - Skipping unknown configuration option {%s=%s} found in {%s} software properties.", key, value, defaultSoftwareLabel));
            } else if (transientOptionKeys.contains(key)) {
                if (info) log.info(String.format(" - Skipping transient configuration option {%s=%s} found in {%s} software properties.", key, value, defaultSoftwareLabel));
            } else {
                if (info) log.info(String.format(" - Applying option {%s=%s}", key, value));
                appConfig.setOption(key, value);
            }
        });
    }

}
