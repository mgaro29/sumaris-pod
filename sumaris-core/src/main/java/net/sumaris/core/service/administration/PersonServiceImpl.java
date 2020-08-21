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


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.sumaris.core.dao.administration.user.DepartmentRepository;
import net.sumaris.core.dao.administration.user.PersonRepository;
import net.sumaris.core.dao.data.ImageAttachmentDao;
import net.sumaris.core.dao.technical.SortDirection;
import net.sumaris.core.exception.DataNotFoundException;
import net.sumaris.core.model.administration.user.Person;
import net.sumaris.core.model.data.ImageAttachment;
import net.sumaris.core.model.referential.StatusEnum;
import net.sumaris.core.model.referential.UserProfileEnum;
import net.sumaris.core.vo.administration.user.DepartmentVO;
import net.sumaris.core.vo.administration.user.PersonVO;
import net.sumaris.core.vo.data.ImageAttachmentVO;
import net.sumaris.core.vo.filter.PersonFilterVO;
import org.apache.commons.lang3.StringUtils;
import org.nuiton.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("personService")
public class PersonServiceImpl implements PersonService {

	private static final Logger log = LoggerFactory.getLogger(PersonServiceImpl.class);

	@Autowired
	protected PersonRepository personRepository;

	@Autowired
	protected DepartmentRepository departmentRepository;

	@Autowired
	private ImageAttachmentDao imageAttachmentDao;

	@Override
	public List<PersonVO> findByFilter(PersonFilterVO filter, int offset, int size, String sortAttribute, SortDirection sortDirection) {
	    if (filter == null) filter = new PersonFilterVO();
		return personRepository.findByFilter(filter, offset, size, sortAttribute, sortDirection);
	}

	@Override
	public Long countByFilter(final PersonFilterVO filter) {
		return personRepository.countByFilter(filter != null ? filter : new PersonFilterVO());
	}

	@Override
	public PersonVO get(final int personId) {
		// This find method was a find in PersonDaoImpl
		return personRepository.findById(personId);
	}

	@Override
	public PersonVO getByPubkey(final String pubkey) {
		Preconditions.checkNotNull(pubkey);
		PersonVO person = personRepository.findByPubkey(pubkey);
		if (person == null) {
			throw new DataNotFoundException(I18n.t("sumaris.error.person.notFound"));
		}
		return person;
	}

    @Override
    public boolean isExistsByEmailHash(final String hash) {
        Preconditions.checkArgument(StringUtils.isNotBlank(hash));
        return personRepository.existsByEmailMD5(hash);
    }

    @Override
	public ImageAttachmentVO getAvatarByPubkey(final String pubkey) {
		Preconditions.checkNotNull(pubkey);
		Optional<Person> person = Optional.ofNullable(personRepository.findByPubkey(pubkey))
			.flatMap(vo -> personRepository.findById(vo.getId()));

		Integer avatarId = person
			.map(Person::getAvatar)
			.map(ImageAttachment::getId)
			.orElseThrow(() -> new DataRetrievalFailureException(I18n.t("sumaris.error.person.avatar.notFound")));

		return imageAttachmentDao.get(avatarId);
	}

	@Override
	public List<String> getEmailsByProfiles(UserProfileEnum... userProfiles) {
		Preconditions.checkNotNull(userProfiles);

		return personRepository.getEmailsByProfiles(
				ImmutableList.copyOf(userProfiles).stream().map(up -> up.id).collect(Collectors.toList()),
				ImmutableList.of(StatusEnum.ENABLE.getId())
		);
	}

	@Override
	public PersonVO save(PersonVO person) {
		checkValid(person);

		// Make sure to fill department, before saving, because of cache
		if (person.getDepartment().getLabel() == null) {
			DepartmentVO department = departmentRepository.get(person.getDepartment().getId());
			person.setDepartment(department);
		}

		return personRepository.save(person);
	}

	@Override
	public List<PersonVO> save(List<PersonVO> persons) {
		Preconditions.checkNotNull(persons);

		return persons.stream()
				.map(this::save)
				.collect(Collectors.toList());
	}

	@Override
	public void delete(int id) {
		personRepository.deleteById(id);
	}

	@Override
	public void delete(List<Integer> ids) {
		Preconditions.checkNotNull(ids);

		ids.forEach(this::delete);
	}

	/* -- protected methods -- */
	protected void checkValid(PersonVO person) {
		Preconditions.checkNotNull(person);
		Preconditions.checkNotNull(person.getEmail(), I18n.t("sumaris.error.validation.required", I18n.t("sumaris.model.person.email")));
		Preconditions.checkNotNull(person.getFirstName(), I18n.t("sumaris.error.validation.required", I18n.t("sumaris.model.person.firstName")));
		Preconditions.checkNotNull(person.getLastName(), I18n.t("sumaris.error.validation.required", I18n.t("sumaris.model.person.lastName")));
		Preconditions.checkNotNull(person.getDepartment(), I18n.t("sumaris.error.validation.required", I18n.t("sumaris.model.person.department")));
		Preconditions.checkNotNull(person.getDepartment().getId(), I18n.t("sumaris.error.validation.required", I18n.t("sumaris.model.person.department")));

	}
}
