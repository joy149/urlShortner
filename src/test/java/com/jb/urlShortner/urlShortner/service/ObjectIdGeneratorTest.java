package com.jb.urlShortner.urlShortner.service;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObjectIdGeneratorTest {

    @Test
    void getObjectIdReturnsValue() {
        ObjectIdGenerator generator = new ObjectIdGenerator();
        ObjectId id = generator.getObjectId();
        assertNotNull(id);
    }
}
