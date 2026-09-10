package com.contactmanager.infrastructure.persistence;

import com.contactmanager.domain.model.Contact;
import org.mapstruct.Mapper;

@Mapper
interface ContactPersistenceMapper {

    ContactEntity toEntity(Contact contact);

    Contact toDomain(ContactEntity entity);
}
