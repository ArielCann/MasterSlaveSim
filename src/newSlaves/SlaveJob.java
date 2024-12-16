package newSlaves;

public class SlaveJob {
    String jobType;
    int jobNumber;
    public SlaveJob(int jobNumber, char jobType) {
        this.jobNumber = jobNumber;
        this.jobType = String.valueOf(jobType);
    }
}
