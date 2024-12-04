import java.net.Socket;

public class Job {
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Job && ((Job)obj).id ==  this.id && ((Job)obj).type == this.type);
    }
    @Override
    public int hashCode() {
        int factor = (this.type == 'A') ? 1 : -1;
        return this.id * factor;
    }
    public int id;
    public char type;
    public Job(int id, char type) {
        this.id = id;
        this.type = type;
    }
}
