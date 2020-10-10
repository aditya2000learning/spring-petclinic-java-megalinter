/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.common.CommonAttribute;
import org.springframework.samples.petclinic.common.CommonError;
import org.springframework.samples.petclinic.dto.*;
import org.springframework.samples.petclinic.validator.PetDTOValidator;
import org.springframework.samples.petclinic.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Paul-Emmanuel DOS SANTOS FACAO
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private final OwnerService ownerService;

	private final PetService petService;

	private final PetTypeService petTypeService;

	@Autowired
	PetController(OwnerService ownerService, PetService petService, PetTypeService petTypeService) {
		this.ownerService = ownerService;
		this.petService = petService;
		this.petTypeService = petTypeService;
	}

	@ModelAttribute("types")
	public Collection<PetTypeDTO> populatePetTypes() {
		return this.petTypeService.findPetTypes();
	}

	@ModelAttribute("owner")
	public OwnerDTO findOwner(@PathVariable("ownerId") int ownerId) {
		return this.ownerService.findById(ownerId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields(CommonAttribute.OWNER_ID);
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetDTOValidator());
	}

	@GetMapping("/pets/new")
	public String initCreationForm(@ModelAttribute(CommonAttribute.OWNER) OwnerDTO owner, ModelMap model) {
		PetDTO pet = new PetDTO();
		owner.addPet(pet);
		model.put(CommonAttribute.PET, pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/new")
	public String processCreationForm(@ModelAttribute(CommonAttribute.OWNER) OwnerDTO owner,
									  @ModelAttribute(CommonAttribute.PET) @Valid PetDTO pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue(CommonAttribute.NAME, CommonError.DUPLICATE_ARGS, CommonError.DUPLICATE_MESSAGE);
		}
		owner.addPet(pet);
		if (result.hasErrors()) {
			model.put(CommonAttribute.PET, pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		} else {
			this.petService.save(pet);
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {
		PetDTO pet = this.petService.findById(petId);
		model.put(CommonAttribute.PET, pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@ModelAttribute(CommonAttribute.PET) @Valid PetDTO pet, BindingResult result, OwnerDTO owner, ModelMap model) {
		if (result.hasErrors()) {
			pet.setOwner(owner);
			model.put(CommonAttribute.PET, pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}
		else {
			owner.addPet(pet);
			this.petService.save(pet);
			return "redirect:/owners/{ownerId}";
		}
	}

}
