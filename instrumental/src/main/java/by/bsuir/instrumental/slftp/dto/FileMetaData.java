package by.bsuir.instrumental.slftp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileMetaData implements Serializable {
    String url;
    String hostId;
    long size;

//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeObject(url);
//        out.writeObject(hostId);
//        out.writeLong(size);
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        this.url = (String) in.readObject();
//        this.hostId = (String) in.readObject();
//        this.size = in.readLong();
//    }
}
