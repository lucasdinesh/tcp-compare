package bank.ui.graphic.action;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import bank.business.AccountOperationService;
import bank.business.BusinessException;
import bank.business.domain.Deposit;
import bank.ui.TextManager;
import bank.ui.graphic.BankGraphicInterface;
import bank.ui.graphic.GUIUtils;

public class PendentDepositsAction extends AccountAbstractAction {

	private class TransactionTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 2497950520925208080L;

		private List<Deposit> deposits;

		public TransactionTableModel(List<Deposit> deposits) {
			this.deposits = new ArrayList<>(deposits);
		}

		@Override
		public int getColumnCount() {
			return 7;
		}

		@Override
		public String getColumnName(int column) {
			String key = null;
			switch (column) {
			case 0:
				key = "dep.num";
				break;
			case 1:
				key = "date";
				break;
			case 2:
				key = "location";
				break;
			case 3:
				key = "operation.type";
				break;
			case 4:
				key = "details";
				break;
			case 5:
				key = "pendent.amount";
				break;
			case 6:
				key = "status";
				break;
			default:
				assert false;
				break;
			}
			return textManager.getText(key);
		}

		@Override
		public int getRowCount() {
			return deposits.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Deposit d = deposits.get(rowIndex);
			Object val = null;
			switch (columnIndex) {
			case 0:
				val = rowIndex + 1;
				break;
			case 1:
				val = GUIUtils.DATE_TIME_FORMAT.format(d.getDate());
				break;
			case 2:
				val = d.getLocation();
				break;
			case 3:
				val = textManager.getText("operation." + d.getClass().getSimpleName());
				break;
			case 4:
				val = ((Deposit) d).getEnvelope();
				break;
			case 5:
				if (((Deposit) d).getPendentAmount() != 0) {
					val = "+ " + ((Deposit) d).getPendentAmount();
				} else {
					val = "+ " + ((Deposit) d).getAmount();
				}
				break;
			case 6:
				val = textManager.getText(String.valueOf(((Deposit) d).getStatus()));
				break;

			default:
				assert false;
				break;
			}
			return val;
		}

	}

	private static final long serialVersionUID = 5090183202921964451L;

	private JPanel cards;
	private JDialog dialog;
	private JTable depositsTable;
	protected JFormattedTextField nDeposit;

	public PendentDepositsAction(BankGraphicInterface bankInterface, TextManager textManager,
			AccountOperationService accountOperationService) {
		super(bankInterface, textManager, accountOperationService);

		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		putValue(Action.NAME, textManager.getText("action.pendents"));
	}

	public void close() {
		dialog.dispose();
		dialog = null;
	}

	@Override
	public void execute() throws BusinessException {

		// Selecionar o depósito
		JPanel selectDepositPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		this.nDeposit = new JFormattedTextField(NumberFormat.getIntegerInstance());
		nDeposit.setColumns(4);
		selectDepositPanel.add(new JLabel("Digite o número do depósito desejado:"));
		selectDepositPanel.add(nDeposit);
		selectDepositPanel.setVisible(true);

		// Cards
		JPanel radioBtPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		this.cards = new JPanel(new CardLayout());

		JPanel cardsPanel = new JPanel();
		cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.PAGE_AXIS));
		cardsPanel.add(radioBtPanel);
		cardsPanel.add(selectDepositPanel);
		cardsPanel.add(cards);

		List<Deposit> deposits = accountOperationService.getPendentDeposits();

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton(textManager.getText("button.close"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				close();
			}
		});
		buttonsPanel.add(cancelButton);
		JButton okButton = new JButton(textManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					showSelectionField(deposits);

				} catch (BusinessException invDep) {
					GUIUtils.INSTANCE.showMessage(bankInterface.getFrame(), invDep.getMessage(),
							invDep.getArgs(), JOptionPane.WARNING_MESSAGE);
					log.warn(invDep);
				}catch (Exception exc) {
					GUIUtils.INSTANCE.handleUnexceptedError(bankInterface.getFrame(),
							exc);
				}
			}
		});
		buttonsPanel.add(okButton);

		if (deposits.isEmpty()) {
			selectDepositPanel.setVisible(false);
			buttonsPanel.setVisible(false);
		} else {
			selectDepositPanel.setVisible(true);
			buttonsPanel.setVisible(true);
		}

		// Statement result
		JPanel depositsPanel = new JPanel();
		depositsPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		depositsTable = new JTable();
		JScrollPane scrollPane = new JScrollPane(depositsTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		depositsPanel.add(scrollPane);

		depositsTable.setModel(new TransactionTableModel(deposits));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		mainPanel.add(cardsPanel, BorderLayout.CENTER);
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

		JPanel pane = new JPanel(new BorderLayout());
		pane.add(mainPanel, BorderLayout.NORTH);
		pane.add(depositsPanel, BorderLayout.CENTER);

		this.dialog = GUIUtils.INSTANCE.createDialog(bankInterface.getFrame(), "action.pendents", pane);
		this.dialog.setVisible(true);
	}

	private void showSelectionField(List<Deposit> transactions) throws Exception{
		if (!transactions.isEmpty()) {
			
			if (!nDeposit.getText().isEmpty()) {
				final Integer selectedDeposit = Integer.parseInt(nDeposit.getText()) - 1;
				System.out.println(selectedDeposit);

				if (selectedDeposit < 0 || (selectedDeposit + 1) > transactions.size()) {
					throw new BusinessException("exception.invalid.deposit");
				}
				JPanel decisionDepositPanel = new JPanel(new GridLayout(5, 5, 5, 5));

				Deposit atual = transactions.get(selectedDeposit);
				decisionDepositPanel.add(new JLabel("Conta: " + atual.getAccount().getId().getNumber()));
				decisionDepositPanel.add(new JLabel("Envelope: " + atual.getEnvelope()));
				if (transactions.get(selectedDeposit).getPendentAmount() == 0) {
					decisionDepositPanel.add(new JLabel("Valor: " + atual.getAmount()));
				} else {
					decisionDepositPanel.add(new JLabel("Valor: " + atual.getPendentAmount()));
				}

				StringBuffer sb = new StringBuffer();

				JButton confirmButton = new JButton(textManager.getText("button.confirm"));
				JButton recuseButton = new JButton(textManager.getText("button.recuse"));
				confirmButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if (transactions.get(selectedDeposit).getPendentAmount() > 0) {
							atual.getAccount().setBalance(atual.getPendentAmount());
							atual.setAmount(atual.getPendentAmount());
							atual.setPendentAmount(0);
							atual.setStatus(1);
						} else {
							atual.setStatus(1);
						}

						sb.append(textManager.getText("message.operation.succesfull")).append("\n");
						int indice = selectedDeposit + 1;
						sb.append("Depósito " + indice + " FINALIZADO");

						GUIUtils.INSTANCE.showMessage(bankInterface.getFrame(), sb.toString(),
								JOptionPane.INFORMATION_MESSAGE);
						dialog.dispose();
					}
				});

				recuseButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if (transactions.get(selectedDeposit).getPendentAmount() == 0) {
							atual.getAccount().setBalance(-atual.getAmount());
							atual.setPendentAmount(atual.getAmount());
							atual.setAmount(0);
							atual.setStatus(3);
						} else {
							atual.setStatus(3);
						}

						sb.append(textManager.getText("message.operation.succesfull")).append("\n");
						int indice = selectedDeposit + 1;
						sb.append("Depósito " + indice + " CANCELADO");

						GUIUtils.INSTANCE.showMessage(bankInterface.getFrame(), sb.toString(),
								JOptionPane.INFORMATION_MESSAGE);
						dialog.dispose();
					}
				});

				this.dialog.dispose();

				decisionDepositPanel.add(confirmButton);
				decisionDepositPanel.add(recuseButton);

				this.dialog = GUIUtils.INSTANCE.createDialog(bankInterface.getFrame(), "decide.box",
						decisionDepositPanel);
				this.dialog.setVisible(true);
			}
		} else {
			transactions.clear();
		}
	}

}
