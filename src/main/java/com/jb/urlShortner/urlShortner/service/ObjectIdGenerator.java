package com.jb.urlShortner.urlShortner.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class ObjectIdGenerator {

    public ObjectId getObjectId() {
        return new ObjectId();
    }
}
