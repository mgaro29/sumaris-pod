package net.sumaris.server.http.graphql.technical;

/*-
 * #%L
 * SUMARiS:: Server
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.leangen.graphql.annotations.*;
import net.sumaris.core.service.administration.DepartmentService;
import net.sumaris.core.service.technical.SoftwareService;
import net.sumaris.core.vo.administration.user.DepartmentVO;
import net.sumaris.core.vo.technical.ConfigurationVO;
import net.sumaris.server.config.SumarisServerConfiguration;
import net.sumaris.server.config.SumarisServerConfigurationOption;
import net.sumaris.server.http.graphql.administration.AdministrationGraphQLService;
import net.sumaris.server.http.rest.RestPaths;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class ConfigurationGraphQLService {

    public static final String URI_DEPARTMENT_SUFFIX = "department:";
    public static final String URI_IMAGE_SUFFIX = "image:";
    public static final String JSON_START_SUFFIX = "{";

    private static final Log log = LogFactory.getLog(ConfigurationGraphQLService.class);

    @Autowired
    private SoftwareService service;

    @Autowired
    private AdministrationGraphQLService administrationGraphQLService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private String imageUrl;

    @Autowired
    public ConfigurationGraphQLService(SumarisServerConfiguration config) {
        super();

        // Prepare URL for String formatter
        imageUrl = config.getServerUrl() + RestPaths.IMAGE_PATH;
    }

    @GraphQLQuery(name = "configuration", description = "A software configuration")
    @Transactional(readOnly = true)
    public ConfigurationVO getConfiguration(
            @GraphQLArgument(name = "software") String software,
            @GraphQLEnvironment() Set<String> fields
    ){
        ConfigurationVO result  = StringUtils.isBlank(software) ? service.getDefault() : service.get(software);

        if (result == null) return null;

        // Fill partners departments
        if (fields.contains(ConfigurationVO.PROPERTY_PARTNERS)) {
            this.fillPartners(result);
        }

        // Fill background images URLs
        if (fields.contains(ConfigurationVO.PROPERTY_BACKGROUND_IMAGES)) {
            this.fillBackgroundImages(result);
        }

        // Fill logo URL
        String logoUri = getProperty(result, SumarisServerConfigurationOption.SITE_LOGO_SMALL.getKey());
        if (StringUtils.isNotBlank(logoUri)) {
            String logoUrl = getImageUrl(logoUri);
            result.getProperties().put(
                    SumarisServerConfigurationOption.SITE_LOGO_SMALL.getKey(),
                    logoUrl);
            result.setSmallLogo(logoUrl);
        }

        // Fill large logo
        String logoLargeUri = getProperty(result, SumarisServerConfigurationOption.LOGO_LARGE.getKey());
        if (StringUtils.isNotBlank(logoLargeUri)) {
            String logoLargeUrl = getImageUrl(logoLargeUri);
            result.getProperties().put(
                    SumarisServerConfigurationOption.LOGO_LARGE.getKey(),
                    logoLargeUrl);
            result.setLargeLogo(logoLargeUrl);
        }

        // Replace favicon ID by an URL
        String faviconUri = getProperty(result, SumarisServerConfigurationOption.SITE_FAVICON.getKey());
        if (StringUtils.isNotBlank(faviconUri)) {
            String faviconUrl = getImageUrl(faviconUri);
            result.getProperties().put(SumarisServerConfigurationOption.SITE_FAVICON.getKey(), faviconUrl);
        }

        return result;
    }

    @GraphQLMutation(name = "saveConfiguration", description = "Save the pod configuration")
    @Transactional
    public void save(@GraphQLArgument(name = "app") ConfigurationVO configuration){
        service.save(configuration);
    }

    /* -- protected methods -- */

    protected String getProperty(ConfigurationVO config, String propertyName) {
        return MapUtils.getString(config.getProperties(), propertyName);
    }

    protected String[] getPropertyAsArray(ConfigurationVO config, String propertyName) {
        String value = getProperty(config, propertyName);

        if (StringUtils.isBlank(value)) return null;

        try {
            return objectMapper.readValue(value, String[].class);
        } catch (IOException e) {
            log.warn(String.format("Unable to deserialize array value for option {%s}: %s", propertyName, value), e);
            return value.split(",");
        }
    }

    protected void fillPartners(ConfigurationVO result) {
        String[] values = getPropertyAsArray(result, SumarisServerConfigurationOption.SITE_PARTNER_DEPARTMENTS.getKey());

        if (ArrayUtils.isNotEmpty(values)) {

            // Get department from IDs
            int[] ids = Stream.of(values)
                    .map(String::trim)
                    .mapToInt(uri -> {
                if (uri.startsWith(URI_DEPARTMENT_SUFFIX)) {
                    return Integer.parseInt(uri.substring(URI_DEPARTMENT_SUFFIX.length()));
                }
                return -1;
            })
            .filter(id -> id >= 0).toArray();
            List<DepartmentVO> departments = departmentService.getByIds(ids);

            // Get department from JSON
            List<DepartmentVO> deserializeDepartments = Stream.of(values)
                    .map(String::trim)
                    .map(jsonStr -> {
                if (jsonStr.startsWith(JSON_START_SUFFIX)) {
                    try {
                        return objectMapper.readValue(jsonStr, DepartmentVO.class);
                    } catch(IOException e) {
                        log.warn(String.format("Unable to deserialize a value for option {%s}: %s", SumarisServerConfigurationOption.SITE_PARTNER_DEPARTMENTS.getKey(), jsonStr), e);
                        return null;
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            departments = Stream.concat(departments.stream(), deserializeDepartments.stream())
                    .map(administrationGraphQLService::fillLogo)
                    .collect(Collectors.toList());
            result.setPartners(departments);
        }
    }

    protected void fillBackgroundImages(ConfigurationVO result) {
        String[] values = getPropertyAsArray(result, SumarisServerConfigurationOption.SITE_BACKGROUND_IMAGES.getKey());

        if (ArrayUtils.isNotEmpty(values)) {

            List<String> urls = Stream.of(values)
                    .map(this::getImageUrl)
                    .collect(Collectors.toList());
            result.setBackgroundImages(urls);
        }
    }

    protected String getImageUrl(String imageUri) {
        if (StringUtils.isBlank(imageUri)) return null;

        // Resolve URI like 'image:<ID>'
        if (imageUri.startsWith(URI_IMAGE_SUFFIX)) {
            return imageUrl.replace("{id}", imageUri.substring(URI_IMAGE_SUFFIX.length()));
        }
        // should be a URL, so return it
        return imageUri;
    }

}