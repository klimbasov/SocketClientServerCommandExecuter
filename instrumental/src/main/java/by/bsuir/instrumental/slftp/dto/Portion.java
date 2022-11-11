package by.bsuir.instrumental.slftp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Portion implements Serializable {
    byte[] content;
    long portionNum;
    byte[] hostId;
    String fileUri;
    long actualSize;

//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeObject(content);
//        out.writeLong(portionNum);
//        out.writeObject(hostId);
//        out.writeObject(fileUri);
//        out.writeLong(actualSize);
//
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        in.read(content);
//    }
}
