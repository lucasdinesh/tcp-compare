package bank.business.domain;

/**
 * @author Ingrid Nunes
 * 
 */
public class Deposit extends Transaction {

	private long envelope;
	private int status;
	
	public Deposit(OperationLocation location, CurrentAccount account, long envelope, double amount,
			double pendentAmount, int status) {
		super(location, account, amount);
		this.envelope = envelope;
		this.status = status;
	}
	

	/**
	 * @return the envelope
	 */
	public long getEnvelope() {
		return envelope;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
