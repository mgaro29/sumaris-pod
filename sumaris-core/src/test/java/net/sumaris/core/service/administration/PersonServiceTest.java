package net.sumaris.core.service.administration;

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

import net.sumaris.core.dao.DatabaseResource;
import net.sumaris.core.model.referential.UserProfile;
import net.sumaris.core.model.referential.UserProfileEnum;
import net.sumaris.core.service.AbstractServiceTest;
import net.sumaris.core.service.referential.ReferentialService;
import net.sumaris.core.util.crypto.MD5Util;
import net.sumaris.core.vo.administration.user.DepartmentVO;
import net.sumaris.core.vo.administration.user.PersonVO;
import net.sumaris.core.vo.filter.PersonFilterVO;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PersonServiceTest extends AbstractServiceTest{

    @ClassRule
    public static final DatabaseResource dbResource = DatabaseResource.writeDb();

    @Autowired
    private PersonService service;

    @Autowired
    private ReferentialService referentialService;

    @Test
    public void findPersons() {

        Integer observerProfileId =  dbResource.getFixtures().getUserProfileObserver();

        // Find by one profile
        PersonFilterVO filter = new PersonFilterVO();
        filter.setUserProfileId(observerProfileId);
        List<PersonVO> results = service.findByFilter(filter, 0, 100, null, null);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.size() > 0);
        PersonVO person = results.get(0);
        Assert.assertTrue(person.getProfiles().size() > 0);
        UserProfileEnum profile = UserProfileEnum.valueOf(person.getProfiles().get(0)) ;
        Assert.assertEquals(observerProfileId, new Integer(profile.id));

        // Find by many profile
        filter = new PersonFilterVO();
        filter.setUserProfileIds(new Integer[]{observerProfileId, dbResource.getFixtures().getUserProfileSupervisor()});
        results = service.findByFilter(filter, 0, 100, null, null);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.size() > 0);

        // Find by status (inactive person)
        filter = new PersonFilterVO();
        filter.setStatusIds(new Integer[]{getConfig().getStatusIdTemporary(), getConfig().getStatusIdValid()});
        results = service.findByFilter(filter, 0, 100, null, null);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.size() > 0);

        // Find by email
        filter = new PersonFilterVO();
        filter.setEmail(dbResource.getFixtures().getPersonEmail(0));
        results = service.findByFilter(filter, 0, 100, null, null);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());

        // Find by last name (case insensitive)
        filter = new PersonFilterVO();
        filter.setLastName("LaVEniER");
        results = service.findByFilter(filter, 0, 100, null, null);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void isExistsByEmailHash() {

        PersonVO person = service.get(dbResource.getFixtures().getPersonId(0));
        Assume.assumeNotNull(person);
        String emailHash = MD5Util.md5Hex(person.getEmail());

        boolean isExists = service.isExistsByEmailHash(emailHash);
        Assert.assertTrue(isExists);
    }

    @Test
    public void save() {
        PersonVO vo = new PersonVO();
        vo.setFirstName("first name");
        vo.setLastName("last name");
        vo.setEmail("test@sumaris.net");
        vo.setStatusId(getConfig().getStatusIdValid());

        DepartmentVO department = new DepartmentVO();
        department.setId(dbResource.getFixtures().getDepartmentId(0));

        vo.setDepartment(department);

        service.save(vo);
    }

    @Test
    public void delete() {

        long userProfileCountBefore = CollectionUtils.size(referentialService.findByFilter(UserProfile.class.getSimpleName(), null, 0, 1000));

        service.delete(dbResource.getFixtures().getPersonIdNoData());

        // Make there is no cascade on user profile !!
        long userProfileCountAfter = CollectionUtils.size(referentialService.findByFilter(UserProfile.class.getSimpleName(), null, 0, 1000));
        Assert.assertEquals(userProfileCountBefore, userProfileCountAfter);
    }

    @Test
    public void getEmailsByProfiles() {
        List<String> emails = service.getEmailsByProfiles(UserProfileEnum.ADMIN);
        Assert.assertNotNull(emails);
        Assert.assertTrue(emails.size() > 0);
    }
}
