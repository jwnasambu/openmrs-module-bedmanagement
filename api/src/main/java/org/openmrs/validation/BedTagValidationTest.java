/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc.
 */
package org.openmrs.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.BedTag;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Date;

/**
 * Tests methods on the {@link BedTagValidator} class.
 */
public class BedTagValidatorTest extends BaseContextSensitiveTest {

	@Autowired
	protected Validator bedTagValidator;

	/**
	 * @see BedTagValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		BedTag tag = new BedTag();
		tag.setName(null);

		Errors errors = new BindException(tag, "tag");
		bedTagValidator.validate(tag, errors);
		assertTrue(errors.hasFieldErrors("name"));

		tag.setName("");
		errors = new BindException(tag, "tag");
		bedTagValidator.validate(tag, errors);
		assertTrue(errors.hasFieldErrors("name"));

		tag.setName(" ");
		errors = new BindException(tag, "tag");
		bedTagValidator.validate(tag, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}

	/**
	 * @see BedTagValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfActiveBedTagWithSameNameAlreadyExists() {
		BedTag existing = new BedTag();
		existing.setName("Isolation");
		Context.getBedManagementService().saveBedTag(existing);

		BedTag duplicate = new BedTag();
		duplicate.setName("Isolation");

		Errors errors = new BindException(duplicate, "duplicate");
		bedTagValidator.validate(duplicate, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}

	/**
	 * @see BedTagValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfExistingTagWithSameNameIsExpired() {
		BedTag expired = new BedTag();
		expired.setName("Maternity");
		expired.setEndDate(new Date());
		Context.getBedManagementService().saveBedTag(expired);

		BedTag newTag = new BedTag();
		newTag.setName("Maternity");

		Errors errors = new BindException(newTag, "newTag");
		bedTagValidator.validate(newTag, errors);
		assertFalse(errors.hasFieldErrors("name"));
	}

	/**
	 * @see BedTagValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfNameIsUnique() {
		BedTag tag = new BedTag();
		tag.setName("Emergency");

		Errors errors = new BindException(tag, "tag");
		bedTagValidator.validate(tag, errors);

		assertFalse(errors.hasErrors());
	}

	/**
	 * @see BedTagValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		BedTag tag = new BedTag();
		tag.setName(
				"too long text too long text too long text too long text too long text too long text too long text too long text");

		Errors errors = new BindException(tag, "tag");
		bedTagValidator.validate(tag, errors);

		assertTrue(errors.hasFieldErrors("name"));
	}
}
