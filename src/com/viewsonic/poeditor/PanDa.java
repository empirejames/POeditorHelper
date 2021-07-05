package com.viewsonic.poeditor;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.ScrollPaneConstants;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import javax.swing.JTextArea;
import java.awt.Font;

public class PanDa implements CallBackListener, ActionListener {

	private JFrame mFrame;
	private static HttpClientUtils mHc;
	private static MergeHelper mMergeHelper;
	private JTextField mTxtProjectID;
	private JTextArea mTxtAreaResult;
	private ArrayList<String> mLangs = new ArrayList<>();
	private DownloadHelper mDh = new DownloadHelper();
	private JTextField mTxtTags;
	private JComboBox<?> mCmBoxLangs;
	private static String[] mPoEditorStrings;
	private ArrayList<String> mFilePaths = new ArrayList<String>();
	private List<List<String>> mListOfRowData = new ArrayList<List<String>>();
	public static final String FILENAME = "Config.ini";
	public static String TOKEN = "";
	public static String EXPORT_API = "";
	
	public static void main(String[] args) {
		Ini ini;
		try {
			System.out.println("讀取參數檔 : " + System.getProperty("user.dir") + "/" + FILENAME);
			ini = new Ini(new File(System.getProperty("user.dir") + "/" + FILENAME));
			mPoEditorStrings = ini.get("Languages", "langs").split(",");
			EXPORT_API = ini.get("Poeditor-API", "export_API");
			TOKEN = ini.get("Poeditor-API", "api_token");
		} catch (InvalidFileFormatException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PanDa window = new PanDa();
					window.mFrame.setVisible(true);
					mHc = new HttpClientUtils();
					mMergeHelper = new MergeHelper();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PanDa() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mDh.setListener(this);
		mFrame = new JFrame();
		mFrame.setTitle("POEditor \u5C0F\u5E6B\u624B v1");
		mFrame.setBounds(100, 100, 766, 500);
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mFrame.getContentPane().setLayout(null);

		JButton btnStart = new JButton("Start");
		btnStart.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		btnStart.setBounds(10, 341, 730, 40);
		btnStart.addActionListener(this);

		JButton btnMerge = new JButton("Merge");
		btnMerge.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		btnMerge.setBounds(10, 385, 730, 40);
		btnMerge.addActionListener(this);

		mTxtProjectID = new JTextField();
		mTxtProjectID.setText("415711");
		mTxtProjectID.setBounds(91, 13, 109, 28);
		mTxtProjectID.setColumns(10);

		JLabel lbProjectID = new JLabel("Project ID :");
		lbProjectID.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		lbProjectID.setBounds(10, 17, 87, 15);

		mCmBoxLangs = new JComboBox<Object>(mPoEditorStrings);
		mCmBoxLangs.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		mCmBoxLangs.setBounds(284, 9, 95, 30);

		JLabel lbLanguage = new JLabel("Language : ");
		lbLanguage.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		lbLanguage.setBounds(210, 17, 81, 15);

		JButton btnAdd = new JButton("Add");
		btnAdd.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		btnAdd.setBounds(397, 9, 95, 30);
		btnAdd.addActionListener(this);

		mTxtAreaResult = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(mTxtAreaResult);
		mTxtAreaResult.setBounds(274, 42, 245, 159);
		scrollPane.setBounds(395, 49, 316, 274);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		mTxtAreaResult.setLineWrap(true);
		mTxtAreaResult.setWrapStyleWord(true);

		JButton btnAddAll = new JButton("Add all");
		btnAddAll.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		btnAddAll.setBounds(511, 9, 95, 30);
		btnAddAll.addActionListener(this);

		JLabel lbTags = new JLabel("Tags :");
		lbTags.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		lbTags.setBounds(40, 54, 46, 15);

		mTxtTags = new JTextField();
		mTxtTags.setBounds(91, 46, 109, 30);
		mTxtTags.setColumns(10);

		JButton btnClear = new JButton("Clear");
		btnClear.setFont(new Font("Times New Roman", Font.PLAIN, 15));
		btnClear.setBounds(616, 9, 95, 30);
		btnClear.addActionListener(this);

		mFrame.getContentPane().add(btnAddAll);
		mFrame.getContentPane().add(btnStart);
		mFrame.getContentPane().add(btnMerge);
		mFrame.getContentPane().add(mTxtProjectID);
		mFrame.getContentPane().add(lbProjectID);
		mFrame.getContentPane().add(mCmBoxLangs);
		mFrame.getContentPane().add(lbLanguage);
		mFrame.getContentPane().add(btnAdd);
		mFrame.getContentPane().add(scrollPane);
		mFrame.getContentPane().add(lbTags);
		mFrame.getContentPane().add(mTxtTags);
		mFrame.getContentPane().add(btnClear);
	}

	@Override
	public void downloadCallBack(String result) {
		mTxtAreaResult.append(result);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "Start") {
			// Clear text
			mTxtAreaResult.setText(null);
			for (final String languages : mLangs) {
				new Thread() {
					public void run() {
						mHc.doCurlPost(TOKEN, EXPORT_API, mTxtProjectID.getText(), languages, mTxtTags.getText());
					}
				}.start();
			}
		} else if (cmd == "Add") {
			mLangs.add(mCmBoxLangs.getSelectedItem().toString());
			mTxtAreaResult.append(mCmBoxLangs.getSelectedItem().toString() + "\n");
		} else if (cmd == "Add all") {
			for (int i = 0; i < mPoEditorStrings.length; i++) {
				mLangs.add(mPoEditorStrings[i]);
				mTxtAreaResult.append(mPoEditorStrings[i] + "\n");
			}
		} else if (cmd == "Clear") {
			mTxtAreaResult.setText("");
			mLangs.clear();
		} else if (cmd == "Merge") {
			mFilePaths = mMergeHelper.getFiles(System.getProperty("user.dir"));
			for (int i = 0; i < mFilePaths.size(); i++) {
				System.out.println(mFilePaths.get(i));
				mListOfRowData.addAll(mMergeHelper.readColContentFromExcel(mFilePaths.get(i).toString()));
			}
			mMergeHelper.writeExcel(mFilePaths.size(), mListOfRowData);
			System.out.println("合併結束");
			mFilePaths.clear();
			mListOfRowData.clear();
		}
	}
}
