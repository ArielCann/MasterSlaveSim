package newSlaves;

import java.io.IOException;

public class SlaveA extends AbstractSlave {
    public SlaveA(int port) throws IOException {
        super(port, 'A');
    }
}