package by.bsuir.instrumental.service;

import lombok.Value;

import java.io.Serializable;

@Value
public class MetaDataRecord implements Serializable {
    String targetId;
    String path;
    long partitionsTransferred;
}
