package br.com.erudio.services;

import br.com.erudio.controllers.PersonController;
import br.com.erudio.data.dto.v1.PersonDTO;
import br.com.erudio.data.dto.v2.PersonDTOV2;
import br.com.erudio.exception.BadRequestException;
import br.com.erudio.exception.FileStorageException;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.exception.ResourceNotFoundException;
import br.com.erudio.file.exporter.contract.PersonExporter;
import br.com.erudio.file.exporter.factory.FileExporterFactory;
import br.com.erudio.file.importer.contract.FileImporter;
import br.com.erudio.file.importer.factory.FileImporterFactory;
import br.com.erudio.mapper.custom.PersonMapper;
import br.com.erudio.model.Person;
import br.com.erudio.repository.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static br.com.erudio.mapper.ObjectMapper.parseObject;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class PersonService {

    private Logger logger = Logger.getLogger(PersonService.class.getName());

    @Autowired
    PersonRepository repository;

    @Autowired
    FileImporterFactory importer;

    @Autowired
    FileExporterFactory exporter;

    @Autowired
    PagedResourcesAssembler<PersonDTO> assembler;

    @Autowired
    PersonMapper converter;

    // FIND ALL
    public PagedModel<EntityModel<PersonDTO>> findAll(Pageable pageable) {

        logger.info("Finding all people!");

        var people = repository.findAll(pageable);
        return buildPagedModel(pageable, people);
    }

    // FIND PEOPLE BY NAME
    public PagedModel<EntityModel<PersonDTO>> findPeopleByName(String firstName, Pageable pageable) {

        logger.info("Finding people by name!");

        var people = repository.findPeopleByName(firstName, pageable);
        return buildPagedModel(pageable, people);
    }

    // EXPORT PAGE
    public Resource exportPage(Pageable pageable, String acceptHeader) {

        logger.info("Exporting a People page!");

        var people = repository.findAll(pageable)
                .map(person -> parseObject(person, PersonDTO.class)).getContent();

        try {
            PersonExporter exporter = this.exporter.getExporter(acceptHeader);
            return exporter.exportPeople(people);
        } catch (Exception e) {
            throw new RuntimeException("Error during file export.", e);
        }
    }

    // EXPORT PERSON
    public Resource exportPerson(Long id, String acceptHeader) {
        logger.info("Exporting data of one Person!");

        var person = repository.findById(id)
                .map(entity -> parseObject(entity, PersonDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        try {
            PersonExporter exporter = this.exporter.getExporter(acceptHeader);
            return exporter.exportPerson(person);
        } catch (Exception e) {
            throw new RuntimeException("Error during file export!", e);
        }
    }

    // FIND BY ID
    public PersonDTO findById(Long id) {
        logger.info("Finding one Person!");

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

        var dto = parseObject(entity, PersonDTO.class);

        // HATEOS -> Link para requisição
        addHteosLinks(dto);
        return dto;
    }

    // CREATE
    public PersonDTO create(PersonDTO person) {
        
        if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one Person!");

        var entity = parseObject(person, Person.class);

        var dto = parseObject(repository.save(entity), PersonDTO.class);
        addHteosLinks(dto);

        return dto;
    }

    // MASSCREATION
    public List<PersonDTO> massCreation(MultipartFile file) {

        logger.info("Importing People from file!");

        if (file.isEmpty()) throw new BadRequestException("Please set a valid file");

        try (InputStream inputStream = file.getInputStream()) {
            String filename = Optional.ofNullable(file.getOriginalFilename())
                    .orElseThrow(() -> new BadRequestException("File name cannot be null!"));

            FileImporter importer = this.importer.getImporter(filename);

            List<Person> entities = importer.importFile(inputStream).stream()
                    .map(dto -> repository.save(parseObject(dto, Person.class)))
                    .toList();

            return entities.stream()
                .map(entity -> {
                    var dto = parseObject(entity, PersonDTO.class);
                    addHteosLinks(dto);

                    return dto;
                })
                .toList();

        } catch (Exception e) {
            throw new FileStorageException("Error processing the file!", e);
        }
    }

    // UPDATE
    public PersonDTO update(PersonDTO person) {

        if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Updating one Person!");

        Person entity = repository.findById(person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());
        entity.setEnabled(person.getEnabled());
        entity.setPhotoUrl(person.getPhotoUrl());
        entity.setProfileUrl(person.getProfileUrl());

        var dto = parseObject(repository.save(entity), PersonDTO.class);
        addHteosLinks(dto);

        return dto;
    }

    // DISABLE PERSON
    @Transactional
    public PersonDTO disablePerson(Long id) {
        logger.info("Disabling one Person!");

        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

        repository.disablePerson(id);

        var entity = repository.findById(id).get();

        var dto = parseObject(entity, PersonDTO.class);
        addHteosLinks(dto);

        return dto;
    }

    // DELETE
    public void delete(Long id) {
        logger.info("Deleting one Person!");

        Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID"));

        repository.delete(entity);
    }

    // BUILD PAGEG MODEL
    private PagedModel<EntityModel<PersonDTO>> buildPagedModel(Pageable pageable, Page<Person> people) {
        var peopleWithLinks = people.map(person -> {
            var dto = parseObject(person, PersonDTO.class);
            addHteosLinks(dto);

            return dto;
        });

        Link findAllLink = WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(PersonController.class)
                                .findAll(pageable.getPageNumber(), pageable.getPageSize(), String.valueOf(pageable.getSort())))
                .withSelfRel();

        return assembler.toModel(peopleWithLinks, findAllLink);
    }

    // HATEOS -> Link para requisição
    private void addHteosLinks(PersonDTO dto) {
        dto.add(linkTo(methodOn(PersonController.class).findById(dto.getId())).withSelfRel().withType("GET"));

        dto.add(linkTo(methodOn(PersonController.class).findAll(1, 12,"asc"))
                .withRel("findAll").withType("GET"));

        dto.add(linkTo(methodOn(PersonController.class).findPeopleByName("", 1, 12,"asc"))
                .withRel("findPeopleByName").withType("GET"));

        dto.add(linkTo(methodOn(PersonController.class).create(dto)).withRel("create").withType("POST"));

        dto.add(linkTo(methodOn(PersonController.class)).slash("massCreation").withRel("massCreation").withType("POST"));

        dto.add(linkTo(methodOn(PersonController.class).update(dto)).withRel("update").withType("PUT"));

        dto.add(linkTo(methodOn(PersonController.class).disablePerson(dto.getId())).withRel("disable").withType("PATCH"));

        dto.add(linkTo(methodOn(PersonController.class).delete(dto.getId())).withRel("delete").withType("DELETE"));

        dto.add(linkTo(methodOn(PersonController.class).exportPage(1, 12, "asc", null))
                .withRel("exportPage").withType("GET").withTitle("Export People"));
    }



    // ==================== V2 =========================

    public PersonDTOV2 createV2(PersonDTOV2 person) {
        logger.info("Creating one Person V2!");

        var entity = converter.convertDTOToEntity(person);

        return converter.convertEntityToDTO(repository.save(entity));
    }

}
