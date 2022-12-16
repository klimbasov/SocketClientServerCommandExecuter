package by.bsuir.instrumental.command;

import by.bsuir.instrumental.ftp.slftp.SlftpController;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResourceHolder {
    IdentificationHolder identificationHolder;
    SlftpController slftpController;
}
