package com.example.tempproject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.aem.api.AEMPrinter;
import com.aem.api.AEMPrinter.BARCODE_HEIGHT;
import com.aem.api.AEMPrinter.BARCODE_TYPE;
import com.aem.api.AEMScrybeDevice;
import com.aem.api.CardReader;
import com.aem.api.CardReader.CARD_TRACK;
import com.aem.api.IAemCardScanner;
import com.aem.api.IAemScrybe;
import com.google.zxing.WriterException;

public class MainActivity extends Activity implements IAemCardScanner,
		IAemScrybe 
{

	AEMScrybeDevice m_AemScrybeDevice;
	CardReader m_cardReader = null;
	AEMPrinter m_AemPrinter = null;

	EditText editText, rfdEditText;

	ArrayList<String> printerList;

	String creditData;

	ProgressDialog m_WaitDialogue;

	CARD_TRACK cardTrackType;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		printerList = new ArrayList<String>();
		creditData = new String();

		editText = (EditText) findViewById(R.id.edittext);
		rfdEditText = (EditText) findViewById(R.id.RFid);

		m_AemScrybeDevice = new AEMScrybeDevice(this);

		Button discoverButton = (Button) findViewById(R.id.pairing);
		registerForContextMenu(discoverButton);
		
	}

	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle("Select Printer to connect");

		for (int i = 0; i < printerList.size(); i++) 
		{
			menu.add(0, v.getId(), 0, printerList.get(i));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		super.onContextItemSelected(item);

		String printerName = item.getTitle().toString();

		try 
		{
			m_AemScrybeDevice.connectToPrinter(printerName);
			m_cardReader = m_AemScrybeDevice.getCardReader(this);
			m_AemPrinter = m_AemScrybeDevice.getAemPrinter();

			showAlert("Connected with " + printerName);
		} 
		catch (IOException e) 
		{
			if (e.getMessage().contains("Service discovery failed")) 
			{
				showAlert("Not Connected\n"
						+ printerName
						+ " is unreachable or off otherwise it is connected with other device");
			} 
			else if (e.getMessage().contains("Device or resource busy")) 
			{
				showAlert("the device is already connected");
			} 
			else 
			{
				showAlert("Unable to connect");
			}
		}
		return true;
	}

	@Override
	protected void onDestroy() 
	{
		if (m_AemScrybeDevice != null) 
		{
			try 
			{
				m_AemScrybeDevice.disConnectPrinter();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}
	
	public void onShowPairedPrinters(View v) 
	{
//		m_AemScrybeDevice.startDiscover(getApplicationContext());
		
		String p = m_AemScrybeDevice.pairPrinter("BTprinter0314");

		Toast.makeText(getApplicationContext(), p, Toast.LENGTH_SHORT).show();

		 printerList = m_AemScrybeDevice.getPairedPrinters();
		
		 if (printerList.size() > 0)
		 openContextMenu(v);
		 else
		 showAlert("No Paired Printers found");
	}

	public void onDisconnectDevice(View v) 
	{
		if (m_AemScrybeDevice != null) 
		{
			try 
			{
				m_AemScrybeDevice.disConnectPrinter();
				showAlert("disconnected");
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public void onReadSmartCard(View v) 
	{

		if (m_cardReader == null) 
		{
			showAlert("Printer not connected");
			return;
		}

		editText.setText("");
		try 
		{
			m_cardReader.readDL();
		} 
		catch (IOException e) 
		{
			showAlert("Printer not connected");
		}
	}

	public void onReadMSR(View v) 
	{
		if (m_cardReader == null) 
		{
			showAlert("Printer not connected");
			return;
		}

		editText.setText("");
		try 
		{
			m_cardReader.readMSR();
		} 
		catch (IOException e) 
		{
			showAlert("Printer not connected");
		}
	}

	public void onPrintBill(View v) 
	{

		if (m_AemPrinter == null) 
		{
			showAlert("Printer not connected");
			return;
		}

		try 
		{
			m_AemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
			m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
			
		/*	String data = "    INDER LOK METRO STATION";
			m_AemPrinter.print(data);
			String d = "________________________________";
			m_AemPrinter.print(d);
			data = "VEHICLE TYPE:  Car";
			m_AemPrinter.print(data);

			m_AemPrinter.print(d);

			data = "FARE: Rs.20/- Up To 6 Hours \n"
					+ "ENTRY DATE: 21/04/2014\n"
					+ "ENTRY TIME: 15:45\n"
					+ "_______________________________\n"
					+ "     ** Have A Nice Day **";
*/
			
			String data = "         THREE INCH PRINTER: TEST PRINT";
			m_AemPrinter.print(data);
			String d =    "________________________________________________";
			m_AemPrinter.print(d);
			data = 		  "CODE|   DESCRIPTION   |RATE(Rs)|QTY |AMOUNT(Rs)";
			m_AemPrinter.print(data);

			m_AemPrinter.print(d);

			data = " 13 |Colgate Total Gel | 35.00  | 02 |  70.00\n"+
				   " 29 |Pears Soap 250g   | 25.00  | 01 |  25.00\n"+
				   " 88 |Lux Shower Gel 500| 46.00  | 01 |  46.00\n"+
				   " 15 |Dabur Honey 250g  | 65.00  | 01 |  65.00\n"+
				   " 52 |Cadbury Dairy Milk| 20.00  | 10 | 200.00\n"+
				   "128 |Maggie Totamto Sou| 36.00  | 04 | 144.00\n"+  				   
				   "______________________________________________\n";
				   			
			m_AemPrinter.setFontType(AEMPrinter.FONT_NORMAL);
			m_AemPrinter.print(data);
			
			m_AemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
			m_AemPrinter.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER);
			
			data = "          TOTAL AMOUNT (Rs.)   550.00\n";
			m_AemPrinter.print(data);

			m_AemPrinter.setFontType(AEMPrinter.FONT_002);
			m_AemPrinter.print(d);
			data = "        Thank you! Have a pleasant day..\n";
			
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();

		} 
		catch (IOException e) 
		{
			if (e.getMessage().contains("socket closed"))
				showAlert("Printer not connected");
		}
	}

	public void onReadTrack12(View v) 
	{

	}

	public void onReadTrack3(View v) 
	{
	}

	public void onPrint(View v) 
	{
		if (m_AemPrinter == null) 
		{
			showAlert("Printer not connected");
			return;
		}

		try 
		{
			String data = editText.getText().toString();
//			String data = "";
			m_AemPrinter.print(data);
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
		} 
		catch (IOException e) 
		{
			if (e.getMessage().contains("socket closed"))
				showAlert("Printer not connected");
		}
	}

	public void onPrintQRCode(View v)
	{
		if (m_AemPrinter == null) 
		{
			showAlert("Printer not connected");
			return;
		}
		
		String text = editText.getText().toString();

		try 
		{
			Bitmap bitmap = m_AemPrinter.createQRCode(text);
			ImageView imageView = (ImageView)findViewById(R.id.image);
			try {
				m_AemPrinter.printImage(bitmap, getApplicationContext(), AEMPrinter.IMAGE_CENTER_ALIGNMENT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			imageView.setImageBitmap(bitmap);
			
		} 
		catch (WriterException e) 
		{
		}
		
	}
	
	public void onPrintBarcode(View v) 
	{
		if (m_AemPrinter == null) 
		{
			showAlert("Printer not connected");
			return;
		}

//		String text = editText.getText().toString();
		String text = ("------------------------------25/04/16             "
				+ " 14:42 PM------------------------------ Bill No: 116000005 (16-17)"
				+ "------------------------------"
				+ "Qty          MRP    Rate   Total------------------------------ "
				+ "1. Aashibue1.0 Pc         10     10      10"
				+ "2. Atta1.0 Pc         95     95      95"
				+ "3. Wrigleys Spearmint Gum . "+
				"1.0 Pc         10     10      10"+
				"4. Orbit Chewing Gum Lime1.0 Pc          5      5       5"+
				"5. Nivea Cream White 200ml     1.0 Pc        210    210     210"	);
		try 
		{
			m_AemPrinter.printBarcode(text, BARCODE_TYPE.CODE39,
					BARCODE_HEIGHT.DOUBLEDENSITY_FULLHEIGHT);
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
		} 
		catch (IOException e) 
		{
			showAlert("Printer not connected");
		}
	}

	public void onPrintImage(View v) 
	{
		if (m_AemPrinter == null) 
		{
			showAlert("Printer not connected");
			return;
		}

		try 
		{
//			m_AemPrinter.printAEMLogo();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			m_AemPrinter.setCarriageReturn();
			//InputStream is = getAssets().open("HindiText_rotated.jpg"); //400013994320019
			InputStream is = getAssets().open("DMRC_LOGO_5.jpg");
			Bitmap inputBitmap = BitmapFactory.decodeStream(is);
			m_AemPrinter.printImage(inputBitmap, getApplicationContext(),
					AEMPrinter.IMAGE_CENTER_ALIGNMENT);
		} 
		catch (IOException e) 
		{

		}
	}

	CardReader.MSRCardData creditDetails;

	public void onScanMSR(final String buffer, CARD_TRACK cardTrack) 
	{
		cardTrackType = cardTrack;

		creditData = buffer;
		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				editText.setText(buffer.toString());
			}
		});
	}

	public void onScanDLCard(final String buffer) 
	{
		CardReader.DLCardData dlCardData = m_cardReader.decodeDLData(buffer);

		String name = "NAME:" + dlCardData.NAME + "\n";
		String SWD = "SWD Of: " + dlCardData.SWD_OF + "\n";
		String dob = "DOB: " + dlCardData.DOB + "\n";
		String dlNum = "DLNUM: " + dlCardData.DL_NUM + "\n";
		String issAuth = "ISS AUTH: " + dlCardData.ISS_AUTH + "\n";
		String doi = "DOI: " + dlCardData.DOI + "\n";
		String tp = "VALID TP: " + dlCardData.VALID_TP + "\n";
		String ntp = "VALID NTP: " + dlCardData.VALID_NTP + "\n";

		final String data = name + SWD + dob + dlNum + issAuth + doi + tp + ntp;

		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				editText.setText(data);
			}
		});
	}

	public void onScanRCCard(final String buffer) 
	{
		CardReader.RCCardData rcCardData = m_cardReader.decodeRCData(buffer);

		String regNum = "REG NUM: " + rcCardData.REG_NUM + "\n";
		String regName = "REG NAME: " + rcCardData.REG_NAME + "\n";
		String regUpto = "REG UPTO: " + rcCardData.REG_UPTO + "\n";

		final String data = regNum + regName + regUpto;

		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				editText.setText(data);
			}
		});
	}

	@Override
	public void onScanRFD(final String buffer) 
	{
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(buffer);
		String temp = "";
		try 
		{
			temp = stringBuffer.deleteCharAt(8).toString();
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
		}
		final String data = temp;

		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				rfdEditText.setText("RF ID:   " + data);
				editText.setText("ID " + data);
			}
		});
	}

	public void onDiscoveryComplete(ArrayList<String> aemPrinterList) 
	{
		printerList = aemPrinterList;
		for(int i=0;i<aemPrinterList.size();i++)
		{
			String Device_Name=aemPrinterList.get(i);
		    String status = m_AemScrybeDevice.pairPrinter(Device_Name);
		    Log.e("STATUS", status);
		}

	}

	public void onDecodeCreditData(View v) 
	{
		if (m_cardReader == null) 
		{
			showAlert("Printer not connected");
			return;
		}

		if (!(creditData.length() > 0)) 
		{
			showAlert("The data is unavailable");
			return;
		}

		creditDetails = m_cardReader
				.decodeCreditCard(creditData, cardTrackType);

		String cardNumber = "cardNumber: " + creditDetails.m_cardNumber;
		String HolderName = "HolderName: " + creditDetails.m_AccoundHolderName;
		String ExpirayDate = "Expiray Date: " + creditDetails.m_expiryDate;
		String ServiceCode = "Service Code: " + creditDetails.m_serviceCode;
		String pvki = "PVKI: " + creditDetails.m_pvkiNumber;
		String pvv = "PVV: " + creditDetails.m_pvvNumber;
		String cvv = "CVV: " + creditDetails.m_cvvNumber;

		showAlert(cardNumber + "\n" + HolderName + "\n" + ExpirayDate + "\n"
				+ ServiceCode + "\n" + pvki + "\n" + pvv + "\n" + cvv);
	}

	public void showAlert(String alertMsg) 
	{
		AlertDialog.Builder alertBox = new AlertDialog.Builder(
				MainActivity.this);

		alertBox.setMessage(alertMsg).setCancelable(false)
				.setPositiveButton("OK", new OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int item) 
					{
						return;
					}
				});
		AlertDialog alert = alertBox.create();
		alert.show();
	}
	
}
