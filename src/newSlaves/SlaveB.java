package newSlaves;

import java.io.IOException;

public class SlaveB extends AbstractSlave {
    public SlaveB(int port) throws IOException {
        super(port, 'B');
    }
}
