package team_mike.server;

public class MockDB implements DatabaseModule {
	
	public static long idCount = 0;
	
	@Override
	public void log(String s) {
		System.err.println(s);
	}

	@Override
	public long giveUniqueID() {
		return idCount++;
	}

	@Override
	public void addComputation(String name, long id, String param) {
		System.err.println("MockDB: Computation added: name=" + name + ", id=" + Long.toString(id) + "param=" + param + ".");
	}

	@Override
	public void setComputationActive(long id) {
		System.err.println("MockDB: Computation set active: id=" + Long.toString(id) + ".");

	}

	@Override
	public void setComputationFailed(long id) {
		System.err.println("MockDB: Computation set failed: id=" + Long.toString(id) + ".");
	}

	@Override
	public void computationComplete(long id, String result) {
		System.err.println("MockDB: Computation complete: id=" + Long.toString(id) + ", result=" + result + ".");

	}

	@Override
	public long[] getComputationIDArray() {
		return null;
	}

	@Override
	public ComputationDesc getComputation(long id) {
		return null;
	}

	@Override
	public void removeComputation(long id) {
		System.err.println("MockDB: Computation removed: id=" + Long.toString(id) + ".");

	}

	@Override
	public void addCompletedJob(Job j, long phoneID) {
		System.err.println("MockDB: Job added: job name=" + j.getJobName() + ", phoneId=" + Long.toString(phoneID) + ".");
	}

	@Override
	public long getPhoneID(long jobID) {
		return 0;
	}

	@Override
	public long getPhoneID(String jobName) {
		return 0;
	}

	@Override
	public long[] getJobsByPhone(long phoneID) {
		return null;
	}

}
