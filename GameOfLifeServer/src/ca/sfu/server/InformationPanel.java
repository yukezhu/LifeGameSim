package ca.sfu.server;

import java.awt.Color;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class InformationPanel extends JFrame {

	private static final long serialVersionUID = 1L;

	JLabel cellNumLabel, cellNum;
	JLabel lifeNumLabel, lifeNum;
	JLabel cycleNumLabel, cycleNum;
	JLabel clientNumLabel, clientNum;
	JLabel targetLabel, target;
	
	public InformationPanel()
	{
		setSize(300, 300);
		
		cellNumLabel = new JLabel("Cell Number:");
		cellNumLabel.setBounds(20, 20, 140, 20);
		cellNum = new JLabel("0"); 
		cellNum.setBounds(150, 20, 180, 20);
		
		lifeNumLabel = new JLabel("Life Number:");
		lifeNumLabel.setBounds(20, 60, 180, 20);
		lifeNum = new JLabel("0");
		lifeNum.setBounds(150, 60, 180, 20);
		
		cycleNumLabel = new JLabel("Life Cycle:");
		cycleNumLabel.setBounds(20, 100, 180, 20);
		cycleNum = new JLabel("0");
		cycleNum.setBounds(150, 100, 180, 20);

		clientNumLabel = new JLabel("Client Number:");
		clientNumLabel.setBounds(20, 140, 180, 20);
		clientNum = new JLabel("73");
		clientNum.setBounds(150, 140, 180, 20);
		
		targetLabel = new JLabel("Target Client:");
		targetLabel.setBounds(20, 180, 180, 20);
		target = new JLabel("azure.csil.sfu.ca");
		target.setBounds(150, 180, 180, 20);
		
		Color midnightBlue = new Color(0x18, 0x74, 0xcd);
		cellNum.setForeground(midnightBlue);
		lifeNum.setForeground(midnightBlue);
		cycleNum.setForeground(midnightBlue);
		Color grassGreen = new Color(0x00, 0x64, 0x00);
		clientNum.setForeground(grassGreen);
		target.setForeground(grassGreen);
		
		add(cellNumLabel); add(cellNum);
		add(lifeNumLabel); add(lifeNum);
		add(cycleNumLabel); add(cycleNum);
		add(clientNumLabel); add(clientNum);
		add(targetLabel); add(target);
			
		setTitle("System Information");
		setLayout(null);
//		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	

	public void setCellNum(int cellNum) {
		this.cellNum.setText(NumberFormat.getInstance().format(cellNum));
	}

	public void setLifeNum(int lifeNum) {
		this.lifeNum.setText(NumberFormat.getInstance().format(lifeNum));
	}
	
	public void setCycleNum(int cycleNum) {
		this.cycleNum.setText(NumberFormat.getInstance().format(cycleNum));
	}

	public void setClientNum(int clientNum) {
		this.clientNum.setText(NumberFormat.getInstance().format(clientNum));
	}


	public void setTargetNum(String targe) {
		this.target.setText(targe);
	}



	public static void main(String[] argv)
	{
		JFrame frame = new InformationPanel();
		frame.setVisible(true);
	}
	
	
}
