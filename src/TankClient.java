import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.util.ArrayList;

public class TankClient extends Frame {
	public static final int GAME_WIDTH = 800;
	public static final int GAME_HEIGHT = 600;
	
	Tank myTank = new Tank(50, 50, true, Dir.STOP, this);
	
	List<Explode> explodes = new ArrayList<Explode>();
	List<Missile> missiles = new ArrayList<Missile>();
	List<Tank> tanks = new ArrayList<Tank>();
	Image offScreenImage = null;
	
	NetClient nc = new NetClient(this);
	
	ContDialog dialog = new ContDialog();
	
	public void paint(Graphics g) {
		g.drawString("missiles count:" + missiles.size(), 10, 50);
		g.drawString("explodes count:" + explodes.size(), 10, 70);
		g.drawString("tanks    count:" + tanks.size(), 10, 90);
		
		for(int i=0; i<missiles.size(); i++) {
			Missile m = missiles.get(i);
			//m.hitTanks(tanks);
			if(m.hitTank(myTank)) {
				TankDeadMsg msg = new TankDeadMsg(myTank.id);
				nc.send(msg);
				MissileDeadMsg mDMsg = new MissileDeadMsg(m.tankId, m.id);
				nc.send(mDMsg);
			}
			m.draw(g);
		}
		
		for(int i=0; i<explodes.size(); i++) {
			Explode e = explodes.get(i);
			e.draw(g);
		}
		
		for(int i=0; i<tanks.size(); i++) {
			Tank t = tanks.get(i);
			t.draw(g);
		}
		
		myTank.draw(g);
	}
	
	public void update(Graphics g) {
		if(offScreenImage == null) {
			offScreenImage = this.createImage(GAME_WIDTH, GAME_HEIGHT);
		}
		Graphics gOffScreen = offScreenImage.getGraphics();
		Color c = gOffScreen.getColor();
		gOffScreen.setColor(Color.GREEN);
		gOffScreen.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
		gOffScreen.setColor(c);
		paint(gOffScreen);
		g.drawImage(offScreenImage, 0, 0, null);
	}

	public void lauchFrame() {
		
		this.setLocation(200, 200);
		this.setSize(GAME_WIDTH, GAME_HEIGHT);
		this.setTitle("TankWar");
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		this.setResizable(false);
		this.setBackground(Color.GREEN);
		
		this.addKeyListener(new KeyMonitor());
		
		setVisible(true);
		
		new Thread(new PaintThread()).start();
		
		dialog.setVisible(true);
		
		//nc.connect("127.0.0.1", TankServer.TCP_PORT);
	}

	public static void main(String[] args) {
		TankClient tc = new TankClient();
		tc.lauchFrame();
	}
	
	private class PaintThread implements Runnable {

		public void run() {
			while(true) {
				repaint();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class KeyMonitor extends KeyAdapter {

		public void keyReleased(KeyEvent e) {
			myTank.keyReleased(e);
		}

		public void keyPressed(KeyEvent e) {
			myTank.keyPressed(e);
		}
	}
	
	class ContDialog extends JDialog {
		JButton start = new JButton("Start !");
		JTextField tfIP = new JTextField("127.0.0.1", 12); 
		JTextField tfPort = new JTextField("" + TankServer.TCP_PORT, 4);
		JTextField tfUDPPort = new JTextField("2223", 4);
		
		public ContDialog() {
			super(TankClient.this, "Login", true);
			setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			this.setLayout(new FlowLayout());
			this.setLocation(300,300);
			this.add(new JLabel("IP:"));
			this.add(tfIP);
			this.add(new JLabel("Port:"));
			this.add(tfPort);
			this.add(new JLabel("My UDP Port:"));
			this.add(tfUDPPort);
			this.add(start);
			this.pack();
			start.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String IP = tfIP.getText().trim();
					int port = Integer.parseInt(tfPort.getText());
					int myUDPPort = Integer.parseInt(tfUDPPort.getText());
					nc.setUdpPort(myUDPPort);
					nc.connect(IP, port);
					setVisible(false);
				}
				
			});
		}
	}
}













