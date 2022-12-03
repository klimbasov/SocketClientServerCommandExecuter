package by.bsuir.instrumental.command;

import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.ftp.slftp.SlftpController;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResourceHolder {
    IdentificationHolder identificationHolder;
    SlftpController slftpController;
}
