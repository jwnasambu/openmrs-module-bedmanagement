/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, you can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc.
 */
package org.openmrs.validator;

import org.openmrs.BedTag;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link BedTag} object.
 *
 * Ensures:
 * - name is not null, empty, or whitespace
 * - no two active bed tags share the same name
 * - duplicate names are allowed only if at least one bed tag is expired
 */
@Handler(supports = {BedTag.class}, order = 50)
public class BedTagValidator implements Validator {

    /**
     * Determines if the command object being submitted is a valid type
     *
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> c) {
        return BedTag.class.isAssignableFrom(c);
    }

    /**
     * Checks the form object for any inconsistencies/errors.
     *
     * <strong>Should</strong> fail validation if name is null/empty/whitespace
     * <strong>Should</strong> fail validation if another active BedTag has the same name
     * <strong>Should</strong> pass validation if duplicate name belongs only to expired BedTags
     */
    @Override
    public void validate(Object obj, Errors errors) {
        if (!(obj instanceof BedTag)) {
            errors.reject("error.general", "Invalid object type for validation");
            return;
        }

        BedTag bedTag = (BedTag) obj;

        if (bedTag == null) {
            errors.rejectValue("bedTag", "error.general");
            return;
        }

        // Validate that name is not empty
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");

        // Check for duplicate names among active BedTags
        if (!errors.hasFieldErrors("name")) {
            BedTag existingBedTag = Context.getBedManagementService().getBedTagByName(bedTag.getName());

            if (existingBedTag != null
                    && !existingBedTag.getUuid().equals(bedTag.getUuid())
                    && !existingBedTag.isExpired()
                    && !bedTag.isExpired()) {
                errors.rejectValue("name", "general.error.nameAlreadyInUse");
            }
        }

        // Optional: Validate field lengths if utility exists
        ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name");
    }
}
