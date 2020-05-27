package wiki.zimo.window;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

import wiki.zimo.rfidreader.UHFReader09;

public class Window extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldSetAddr; // 设置读写器地址
	private JTextField textFieldReaderModel; // 读写器型号
	private JTextField textFieldReaderVersion; // 读写器版本
	private JTextField textFieldReaderAddr; // 读写器地址
	private JTextField textFieldReaderPower; // 读写器功率
	private JTextField textFieldMinFrequency; // 读写器最小频率
	private JTextField textFieldMaxFrequency; // 读写器最大频率
	private JTextField textFieldResponseTime; // 读写器查询命令最大响应时间
	
	private JCheckBox chckbxIso;
	private JCheckBox chckbxEpc;

	private JComboBox comboBoxOpenedPort; // 开启的端口
	private JComboBox comboBoxComPort; // 选择连接的端口
	private JButton buttonOpenPort; // 打开端口
	private JButton buttonClosePort; // 关闭端口
	
	IntByReference port = new IntByReference(0); // 端口，默认值是0，即AUTO
    ByteByReference fComAdr = new ByteByReference((byte) 0xFF); // 读写器地址
    Byte baud = 5; // 波特率
    IntByReference portHandle = new IntByReference(0); // 打开的端口句柄
	private int fCmdRet = 30; // 所有执行指令的返回值
	private int fOpenComIndex; // 打开的串口索引号
	private double fdminfre; // 最小频率
    private double fdmaxfre; // 最大频率
	
    private int openComPortResult = -1;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		/**
		 * 开启窗口
		 */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window frame = new Window();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(540, 400);
		setLocationRelativeTo(null);
		setTitle("rfid");
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int code = JOptionPane.showConfirmDialog(null, "确认退出？", "提示！", JOptionPane.YES_OPTION);
				if (code == JOptionPane.YES_OPTION) {
					if (openComPortResult != -1) {
						closeOpenedComport();
					}
					System.exit(0);
				}
			}
		});
		
		setResizable(false);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel label = new JLabel("读卡器参数设置");
		label.setBounds(0, 0, 105, 18);
		contentPane.add(label);
		
		JLabel label_1 = new JLabel("通信串口：");
		label_1.setBounds(14, 31, 75, 18);
		contentPane.add(label_1);
		
		comboBoxComPort = new JComboBox();
		comboBoxComPort.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int val = 0;
					String item = (String) e.getItem();
					if (item.startsWith("COM")) {
						val = Integer.parseInt(item.substring(3));
					}
					port.setValue(val);
				}
			}
		});
		comboBoxComPort.setModel(new DefaultComboBoxModel(new String[] {"AUTO", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10", "COM11", "COM12"}));
		comboBoxComPort.setBounds(89, 28, 75, 21);
		contentPane.add(comboBoxComPort);
		
		JLabel label_2 = new JLabel("读写器地址：");
		label_2.setBounds(14, 62, 90, 18);
		contentPane.add(label_2);
		
		textFieldSetAddr = new JTextField();
		textFieldSetAddr.setBounds(99, 59, 65, 21);
		textFieldSetAddr.setText("FF");
		textFieldSetAddr.setEditable(false);
		contentPane.add(textFieldSetAddr);
		textFieldSetAddr.setColumns(10);
		
		buttonOpenPort = new JButton("打开串口");
		buttonOpenPort.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (buttonOpenPort.isEnabled()) {
					if (port.getValue() == 0) {
						autoOpenComport();
						if (openComPortResult == 0) {
							buttonOpenPort.setEnabled(false);
							buttonClosePort.setEnabled(true);
							comboBoxOpenedPort.addItem("COM"+port.getValue());
							getReaderInfo();
						} else {
							JOptionPane.showMessageDialog(null, "请连接读卡器", "提示", JOptionPane.INFORMATION_MESSAGE);
						}
					} else {
						openComport();
						if (openComPortResult == 0) {
							buttonOpenPort.setEnabled(false);
							buttonClosePort.setEnabled(true);
							comboBoxOpenedPort.addItem("COM"+port.getValue());
							getReaderInfo();
						} else {
							JOptionPane.showMessageDialog(null, "请连接读卡器", "提示", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
			}
		});
		buttonOpenPort.setBounds(14, 93, 150, 27);
		contentPane.add(buttonOpenPort);
		
		JLabel label_3 = new JLabel("波特率：");
		label_3.setBounds(14, 133, 72, 18);
		contentPane.add(label_3);
		
		JComboBox comboBoxBaud = new JComboBox();
		comboBoxBaud.setModel(new DefaultComboBoxModel(new String[] {"9600bps", "19200bps", "38400bps", "57600bps", "115200bps"}));
		comboBoxBaud.setSelectedIndex(3);
		comboBoxBaud.setBounds(74, 130, 90, 21);
		contentPane.add(comboBoxBaud);
		
		JLabel label_4 = new JLabel("已开端口：");
		label_4.setBounds(14, 164, 75, 18);
		contentPane.add(label_4);
		
		comboBoxOpenedPort = new JComboBox();
		comboBoxOpenedPort.setBounds(89, 161, 75, 21);
		contentPane.add(comboBoxOpenedPort);
		
		buttonClosePort = new JButton("关闭串口");
		buttonClosePort.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (buttonClosePort.isEnabled()) {
					closeOpenedComport();
				}
			}
		});
		buttonClosePort.setBounds(14, 195, 150, 27);
		buttonClosePort.setEnabled(false);
		contentPane.add(buttonClosePort);
		
		JLabel label_5 = new JLabel("读卡器信息：");
		label_5.setBounds(198, 31, 95, 18);
		contentPane.add(label_5);
		
		JLabel label_6 = new JLabel("型号：");
		label_6.setBounds(198, 62, 53, 18);
		contentPane.add(label_6);
		
		textFieldReaderModel = new JTextField();
		textFieldReaderModel.setBounds(246, 60, 96, 21);
		contentPane.add(textFieldReaderModel);
		textFieldReaderModel.setColumns(10);
		
		JLabel label_7 = new JLabel("版本：");
		label_7.setBounds(356, 62, 45, 18);
		contentPane.add(label_7);
		
		textFieldReaderVersion = new JTextField();
		textFieldReaderVersion.setBounds(405, 60, 111, 21);
		contentPane.add(textFieldReaderVersion);
		textFieldReaderVersion.setColumns(10);
		
		JLabel label_8 = new JLabel("地址：");
		label_8.setBounds(198, 97, 53, 18);
		contentPane.add(label_8);
		
		textFieldReaderAddr = new JTextField();
		textFieldReaderAddr.setBounds(246, 94, 75, 21);
		contentPane.add(textFieldReaderAddr);
		textFieldReaderAddr.setColumns(10);
		
		JLabel label_9 = new JLabel("功率：");
		label_9.setBounds(356, 97, 45, 18);
		contentPane.add(label_9);
		
		textFieldReaderPower = new JTextField();
		textFieldReaderPower.setBounds(405, 94, 111, 21);
		contentPane.add(textFieldReaderPower);
		textFieldReaderPower.setColumns(10);
		
		JLabel label_10 = new JLabel("最低频率：");
		label_10.setBounds(198, 133, 75, 18);
		contentPane.add(label_10);
		
		textFieldMinFrequency = new JTextField();
		textFieldMinFrequency.setColumns(10);
		textFieldMinFrequency.setBounds(276, 128, 75, 21);
		contentPane.add(textFieldMinFrequency);
		
		JLabel label_11 = new JLabel("最高频率：");
		label_11.setBounds(366, 131, 75, 18);
		contentPane.add(label_11);
		
		textFieldMaxFrequency = new JTextField();
		textFieldMaxFrequency.setColumns(10);
		textFieldMaxFrequency.setBounds(441, 130, 75, 21);
		contentPane.add(textFieldMaxFrequency);
		
		JLabel label_12 = new JLabel("支持的协议：");
		label_12.setBounds(198, 228, 95, 18);
		contentPane.add(label_12);
		
		chckbxIso = new JCheckBox("ISO18000-6B");
		chckbxIso.setBounds(383, 195, 133, 27);
		contentPane.add(chckbxIso);
		
		chckbxEpc = new JCheckBox("EPCC1-G2");
		chckbxEpc.setBounds(383, 245, 133, 27);
		contentPane.add(chckbxEpc);
		
		JLabel label_13 = new JLabel("查询命令最大响应时间：");
		label_13.setBounds(198, 164, 172, 18);
		contentPane.add(label_13);
		
		textFieldResponseTime = new JTextField();
		textFieldResponseTime.setColumns(10);
		textFieldResponseTime.setBounds(376, 162, 140, 21);
		contentPane.add(textFieldResponseTime);
	}

	/**
	 * 打开特定串口
	 */
	protected void openComport() {
		openComPortResult = UHFReader09.INSTANCE.OpenComPort(port.getValue(), fComAdr, baud, portHandle);
	}

	/**
	 * 获取读卡器信息
	 */
	protected void getReaderInfo() {
		byte[] TrType = new byte[2];
        byte[] VersionInfo = new byte[2];
        ByteByReference ReaderType = new ByteByReference((byte) 0);
        ByteByReference ScanTime = new ByteByReference((byte) 0);
        ByteByReference dmaxfre = new ByteByReference((byte) 0);
        ByteByReference dminfre = new ByteByReference((byte) 0);
        ByteByReference powerdBm = new ByteByReference((byte) 0);
        byte FreBand = 0;

        fCmdRet = UHFReader09.INSTANCE.GetReaderInformation(fComAdr, VersionInfo, ReaderType, TrType, dmaxfre, dminfre, powerdBm,  ScanTime, portHandle.getValue());
        if (fCmdRet == 0) {
        	textFieldReaderVersion.setText(String.format("%02d", VersionInfo[0]) + "." + String.format("%02d", VersionInfo[1]));
            textFieldReaderPower.setText(String.format("%02d", powerdBm.getValue()));
            textFieldReaderAddr.setText(String.format("%02d", fComAdr.getValue()));
            textFieldReaderModel.setText("UHFReader"+String.format("%02d", ReaderType.getValue() + 1));
            FreBand = (byte) (((dmaxfre.getValue() & 0xc0) >> 4) | (dminfre.getValue() >> 6));
            switch (FreBand) {
                case 0: 
                    fdminfre = 902.6 + (dminfre.getValue() & 0x3F) * 0.4;
                    fdmaxfre = 902.6 + (dmaxfre.getValue() & 0x3F) * 0.4;
                    break;
                case 1:
                    fdminfre = 920.125 + (dminfre.getValue() & 0x3F) * 0.25;
                    fdmaxfre = 920.125 + (dmaxfre.getValue() & 0x3F) * 0.25;
                    break;
                case 2:
                    fdminfre = 902.75 + (dminfre.getValue() & 0x3F) * 0.5;
                    fdmaxfre = 902.75 + (dmaxfre.getValue() & 0x3F) * 0.5;
                    break;
                case 3:
                    fdminfre = 917.1 + (dminfre.getValue() & 0x3F) * 0.2;
                    fdmaxfre = 917.1 + (dmaxfre.getValue() & 0x3F) * 0.2;
                    break;
                case 4:
                    fdminfre = 865.1 + (dminfre.getValue() & 0x3F) * 0.2;
                    fdmaxfre = 865.1 + (dmaxfre.getValue() & 0x3F) * 0.2;
                    break;
            }
            textFieldMaxFrequency.setText(fdmaxfre + "MHz");
            textFieldMinFrequency.setText(fdminfre + "MHz");
            textFieldResponseTime.setText(ScanTime.getValue() + " * 100ms");
            
            if ((TrType[0] & 0x02) == 0x02) {
                chckbxEpc.setSelected(true);
                chckbxIso.setSelected(true);
            }
            else {
            	chckbxEpc.setSelected(false);
                chckbxIso.setSelected(false);
            }
        }
	}

	/**
	 * 关闭打开的串口
	 */
	protected void closeOpenedComport() {
		fCmdRet = UHFReader09.INSTANCE.CloseSpecComPort(port.getValue());
		if (fCmdRet == 0) {
			JOptionPane.showMessageDialog(null, "关闭成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
			
			comboBoxOpenedPort.removeAllItems();
			textFieldMaxFrequency.setText("");
			textFieldMinFrequency.setText("");
			textFieldReaderAddr.setText("");
			textFieldReaderModel.setText("");
			textFieldReaderPower.setText("");
			textFieldReaderVersion.setText("");
			textFieldResponseTime.setText("");
			chckbxEpc.setSelected(false);
			chckbxIso.setSelected(false);
			
			buttonClosePort.setEnabled(false);
			buttonOpenPort.setEnabled(true);
			openComPortResult = -1;
		}
	}

	/**
	 * 自动打开串口
	 */
	protected void autoOpenComport() {
        openComPortResult = UHFReader09.INSTANCE.AutoOpenComPort(port, fComAdr, baud, portHandle);
        System.out.println(openComPortResult);
	}
}
