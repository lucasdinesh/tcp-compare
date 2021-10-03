package bank.business.domain;

import java.util.ArrayList;
import java.util.List;

import bank.business.BusinessException;

/**
 * @author Ingrid Nunes
 * 
 */
public class CurrentAccount implements Credentials {

	private double balance;
	

	private Client client;
	private List<Deposit> deposits;
	private CurrentAccountId id;
	private List<Transfer> transfers;
	private List<Withdrawal> withdrawals;

	public CurrentAccount(Branch branch, long number, Client client) {
		this.id = new CurrentAccountId(branch, number);
		branch.addAccount(this);
		this.client = client;
		client.setAccount(this);
		this.deposits = new ArrayList<>();
		this.transfers = new ArrayList<>();
		this.withdrawals = new ArrayList<>();
	}

	public CurrentAccount(Branch branch, long number, Client client, double initialBalance) {
		this(branch, number, client);
		this.balance = initialBalance;
	}

	public Deposit deposit(OperationLocation location, long envelope, double amount, double pendentAmount, int status) throws BusinessException {
		depositAmount(amount, pendentAmount);

		Deposit deposit = new Deposit(location, this, envelope, amount, pendentAmount, status);

		this.deposits.add(deposit);

		return deposit;
	}

	private void depositAmount(double amount, double pendentAmount) throws BusinessException {
		if (!isValidAmount(amount, pendentAmount)) {
			throw new BusinessException("exception.invalid.amount");
		}
		if (amount < 100) {
			this.balance += amount;
		}
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @return the deposits
	 */
	public List<Deposit> getDeposits() {
		return deposits;
	}

	/**
	 * @return the id
	 */
	public CurrentAccountId getId() {
		return id;
	}

	public List<Transaction> getTransactions() {
		List<Transaction> transactions = new ArrayList<>(deposits.size() + withdrawals.size() + transfers.size());
		transactions.addAll(deposits);
		transactions.addAll(withdrawals);
		transactions.addAll(transfers);
		return transactions;
	}

	/**
	 * @return the transfers
	 */
	public List<Transfer> getTransfers() {
		return transfers;
	}

	/**
	 * @return the withdrawals
	 */
	public List<Withdrawal> getWithdrawals() {
		return withdrawals;
	}

	private boolean hasEnoughBalance(double amount) {
		return amount <= balance;
	}

	private boolean isValidAmount(double amount, double pendentAmount) {
		if(amount > 0 || pendentAmount > 0) {
			return true;
		} else {
			return false;
		}
	}

	public Transfer transfer(OperationLocation location, CurrentAccount destinationAccount, double amount)
			throws BusinessException {
		withdrawalAmount(amount);
		destinationAccount.depositAmount(amount, 0);

		Transfer transfer = new Transfer(location, this, destinationAccount, amount);
		this.transfers.add(transfer);
		destinationAccount.transfers.add(transfer);

		return transfer;
	}

	public Withdrawal withdrawal(OperationLocation location, double amount) throws BusinessException {
		withdrawalAmount(amount);

		Withdrawal withdrawal = new Withdrawal(location, this, amount);
		this.withdrawals.add(withdrawal);

		return withdrawal;
	}

	private void withdrawalAmount(double amount) throws BusinessException {
		if (!isValidAmount(amount, 0)) {
			throw new BusinessException("exception.invalid.amount");
		}

		if (!hasEnoughBalance(amount)) {
			throw new BusinessException("exception.insufficient.balance");
		}

		this.balance -= amount;
	}
	
	public void setBalance(double amount) {
		this.balance += amount;
	}

}
