/*
 * TradingBot - A Java Trading system..
 * 
 * Copyright (C) 2013 Philipz (philipzheng@gmail.com)
 * http://www.tradingbot.com.tw/
 * http://www.facebook.com/tradingbot
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Apache License, Version 2.0 授權中文說明
 * http://www.openfoundry.org/licenses/29
 * 利用 Apache-2.0 程式所應遵守的義務規定
 * http://www.openfoundry.org/tw/legal-column-list/8950-obligations-of-apache-20
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;

import jna.OMSignAPI;
import messenger.CalendarSample;
import messenger.PlurkApi;
import messenger.facebook;
import messenger.gtalk;

public class NewDdeClient {
	int current = 0;
	Queue<Integer> queue = new LinkedList<Integer>();
	List<Integer> ls = new LinkedList<Integer>();
	Options[] options = { null, null, null};
	int size = 400;
	int lsize = 25;
	int range = 16; // 變動範圍值
	double win = 0;
	double lost = 0;
	double total = 0;
	int high = 0;
	int low = 0;
	int higho = 0;
	int lowo = 0;
	int preSettle;
	static final String Email = "YOUR_EMAIL";
	static final String fb = "FB_ID_SN";
	static final String botname = "bot";
	double nowpercent;
	//double percent;
	static int delay = 270000;
	int multiple = 40;
	String version = "";
	boolean runflag = true;
	LogFile txt;
	int hlflag = 0;
	int signalflag = 0;
	String optionssignal = "台指權,TXO,"; // 2010/5,8100,P,A
	int opprice;
	int opweek;
	int opday;
	int currentmulti = 1;
	String YYMMDD;
	boolean inoutflag = false;
	static gtalk c = gtalk.getInstance();
	static PlurkApi p = PlurkApi.getInstance();
	static facebook f = facebook.getInstance();
	static CalendarSample cal = CalendarSample.getInstance();
	boolean close = false;
	boolean SGXclose = false;
	int upbound = 0;
	int lowbound = 0;
	int closeint = 0;
	int vol;
	int totalvol;
	SGXindex sgx;
	double SGXGap = 0.0015;
	double SGXGapA = 0.00185;
	double SGXGapB = 0.00285;
	double SGXGapC = 0.00162;
	double SGXGapL = 0.00123;
	double SGXpercent = 0;
	double SGXPreSettle = 0;
	double SGXindex = 0;
	String NextSGXSymbol;
	int Gapvolout = 26;
	String SGXTime;
	boolean into = false;
	int money = 100;
	double preSGXGap;
	boolean outsell = true;
	double inPcentGap = 0.022;
	boolean SGXflag = false;
	double kspercent = 0;
	double ksPreSettle = 0;
	int countSize = 100;
	int counter = 0;
	int counterPos = 0;
	boolean isOpen = false;
	boolean intoflag = true;
	double percent = 0.0; // 指標機率
	
	public static void main(String args[]) throws IOException {
		NewDdeClient client = new NewDdeClient();
		client.doit("Test");
	}
	
	public NewDdeClient(){
		try {
			FileInputStream fis = new FileInputStream("D:\\Dropbox\\SGX.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			sgx = (SGXindex) ois.readObject();
			ois.close();
			SGXPreSettle = sgx.getOpenindex();
			preSGXGap = sgx.getlastSGX();
			if (Math.abs(preSGXGap) > 0.00282) {
				SGXflag = true;
				SGXGapB = 0.0032;
			}
			fis = new FileInputStream("D:\\Dropbox\\KS.ser");
			ois = new ObjectInputStream(fis);
			KSindex ks = (KSindex) ois.readObject();
			ois.close();
			ksPreSettle = ks.getOpenindex();
			
			fis = new FileInputStream("D:\\Dropbox\\Updown.ser");
			ois = new ObjectInputStream(fis);
			UpdownNew ud = (UpdownNew) ois.readObject();
			ois.close();
			percent = ud.getDirection() * 0.1;
			
		} catch (Exception ex) {
			System.out.println(ex);
			c.alert(botname, Email, "Object Loading Error!! " + ex);
			f.alert(botname, fb, "Object Loading Error!! " + ex);
			System.exit(0);
		}	
		try {
			FileInputStream fis = new FileInputStream("D:\\Dropbox\\Op.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			Options op = (Options) ois.readObject();
			ois.close();
			new File("D:\\Dropbox\\Op.ser").delete();
			// Clean up the file
			options[signalflag] = op;
			opprice = op.getOpprice();
			current = op.getCurrent();
			if (op.isCallorput()) {
				txt.setOutput("上一交易日未平倉" + opprice + "CALL "
						+ Math.abs(current) + "口!!");
				c.alert(botname, Email, "上一交易日未平倉" + opprice + "CALL "
						+ Math.abs(current) + "口!!");
				f.alert(botname, fb, "上一交易日未平倉" + opprice + "CALL "
						+ Math.abs(current) + "口!!");
			}
			else {
				txt.setOutput("上一交易日未平倉" + opprice + "PUT "
						+ Math.abs(current) + "口!!");
				c.alert(botname, Email, "上一交易日未平倉" + opprice + "PUT "
						+ Math.abs(current) + "口!!");
				f.alert(botname, fb, "上一交易日未平倉" + opprice + "PUT "
						+ Math.abs(current) + "口!!");
			}
		} catch (Exception ex) {
		}
		boolean result = OMSignAPI.INSTANCE.IniDllAndPosition("TXO001", Math
				.abs(current));
		if (!result) {
			System.out.println("OMSignAPI IniDllAndPosition Error!!");
			c.alert(botname, Email, "OMSignAPI IniDllAndPosition Error!!");
			f.alert(botname, fb, "OMSignAPI IniDllAndPosition Error!!");
		}
		c.alert(botname, Email, "Start Trading System!!");
		f.alert(botname, fb, "Start Trading System!!");
		optionssignal = optionssignal + GetWednesday.compareWed1() + ",";
		YYMMDD = GetWednesday.gettoday();
		new File("D:\\Runtime").mkdir();
		txt = new LogFile("D:\\Runtime\\" + YYMMDD + "_OPlog.txt");
		opweek = GetWednesday.getOpweek();
		opday = GetWednesday.getOpday(YYMMDD);
		if (opday == 0) {
			SGXGapB = 0.0020;
		}
		if (YYMMDD.equals(GetWednesday.compareWed2(YYMMDD)))
			close = true;	
		if (GetWednesday.isSGXClose()) {
			SGXclose = true;
			NextSGXSymbol = GetWednesday.getNextSGXSymbol();
		}
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void doit(String input) {
		String[] temp = input.split(",");
		if (temp[0].equals("TX00")) {
			int a = Integer.parseInt(temp[5]);
			totalvol = Integer.parseInt(temp[7]);
			if (a != 0 && totalvol > 0) {
				preSettle = Integer.parseInt(temp[10]);
				high = Integer.parseInt(temp[8]);
				low = Integer.parseInt(temp[9]);
				vol = Integer.parseInt(temp[6]);
				detect(a);
				if (runflag) {
					add(a);
					if (isOpen) {
						check(a);
						if (counterPos != 0)
							counter++;
					}
					check_runtime();
				}
			}
			// txt.setOutput(getNowTime() + " " + a);
		} else if (temp[0].equals("TWN")) {
			SGXindex = Double.parseDouble(temp[1]);
			if (SGXindex != 0)
				SGXpercent = (SGXindex / SGXPreSettle) - 1;
			else
				SGXpercent = 0;
			setSGXTime();
		} else if (temp[0].equals("KOSPI")) {
			double ksindex = Double.parseDouble(temp[1]);
			kspercent = (ksindex / ksPreSettle) - 1;
		}
	}
	
	public double writetxt(String signal,int opprice, boolean buy, int abs) {
		String optionssignal1;
		if (buy) {
			optionssignal1 = optionssignal + opprice + ",C,A";
		} else {
			optionssignal1 = optionssignal + opprice + ",P,A";
		}
		double opprice1 = GetOpPrice.getprice(opprice, buy);
		//opprice1 = priceTrans(buy, opprice1); 高買低賣
		boolean result = OMSignAPI.INSTANCE.GoOrder(signal, optionssignal1,
				getNowTime(), abs, opprice1);
		if (!result) {
			System.out.println("OMSignAPI GoOrder Error!!");
			c.alert(botname, Email, "OMSignAPI GoOrder Error!!");
			f.alert(botname, fb, "OMSignAPI GoOrder Error!!");
		}
		txt.setOutput(getNowTime() + " current:" + abs + ", price:" + opprice1);
		System.out.println("current:" + abs + ", price:" + opprice1);
		return opprice1;
	}

	private void makeprice(int input, boolean buy) {
		double i = new Integer(input).doubleValue();
		i = i / 100;
		double d;
		if (buy) {
			if (opday < 5) {
				d = Math.floor(i) * 100;
				opprice = (int) d;
			} else {
				d = (Math.floor(i) + opweek) * 100;
				opprice = (int) d;
			}
		} else {
			if (opday < 5) {
				d = Math.ceil(i) * 100;
				opprice = (int) d;
			} else {
				d = (Math.ceil(i) - opweek) * 100;
				opprice = (int) d;
			}
		}
	}

/*	private int[] getbound(int input) {
		double i = new Integer(input).doubleValue();
		i = i / 100;
		double d;
		int[] bound = new int[2];
		d = Math.floor(i) * 100;
		bound[0] = (int) d;
		d = Math.ceil(i) * 100;
		bound[1] = (int) d;
		return bound;
	}*/

	public void check(int input) {
		/*double p1 = new Double(input).doubleValue();
		 * double p2 = new Double(preSettle).doubleValue();
		 * percent = new Double(Math.abs(1 - (p1 / p2)));
		 * double p3 = new Double(high).doubleValue(); double p4 = new
		 * Double(low).doubleValue(); percent1 = new Double(Math.abs(1 - (p3 /
		 * p4))); if (current != 0 || percent > 0.005 || percent1 > 0.015) {
		 */
		if (current == 0) {
			checkin(input);
		} else {
			checkout(input);
		}
		// }
	}

	private void detect(int input) {
		double p1 = new Double(input).doubleValue();
		double p2 = new Double(preSettle).doubleValue();
		nowpercent = (p1 / p2) - 1;
		double percent = Math.abs(nowpercent);
		if (!SGXflag) {
			if ((0.02 > percent) && (percent > 0.01)) {
				SGXGapB = 0.00291;
			}
		}
		if (0.008 > percent)
			multiple = 40;
		else if ((0.02 > percent) && (percent > 0.008))
			multiple = 10;
		else
			multiple = 30;
		if (higho == 0) {
			higho = input + multiple;
			inoutflag = false;
		}
		if (lowo == 0) {
			lowo = input - multiple;
			inoutflag = false;
		}
		if (input > higho) {
			higho = input;
			inoutflag = true;
			if (((hlflag == 0) || (hlflag == -1)) && inoutflag) {
				hlflag = 1;
			}
		}
		if (input < lowo) {
			lowo = input;
			inoutflag = true;
			if (((hlflag == 0) || (hlflag == 1))&& inoutflag) {
				hlflag = -1;
			}
		}
	}

/*	private int[] getQueryMaxMin(int input) {
		Object[] t = ls.toArray();
		Arrays.sort(t);
		List<Object> list = Arrays.asList(t);
		int min = (Integer) list.get(0);
		int max = (Integer) list.get(lsize - 1);
		int[] pair = { min, max };
		return pair;
	}*/

	private void checkin(int input) {
		if (into)
			SGXGapB = 0.0039;
		if (ls.size() >= lsize) {
		int[] pair = getQueryModel();
		if (pair[0] == 3) {
			if ((SGXTWGap() - SGXGap) > 0 && outsell) {
				makeprice(input, true);
				currentmulti(GetOpPrice.getprice(opprice, true));
				current = 1 * currentmulti;
				int abs = Math.abs(current);
				signalflag = 0;
				Options op;
				op = new Options(opprice, current, writetxt("TXO001", opprice,
						true, abs));
				options[signalflag] = op;
				alertIn(op, true, abs, "max1");
				into = true;
			}
		} else if (pair[0] == 4) {
			if ((SGXTWGap() + SGXGap) < 0 && outsell) {
				makeprice(input, false);
				currentmulti(GetOpPrice.getprice(opprice, false));
				current = -1 * currentmulti;
				int abs = Math.abs(current);
				signalflag = 0;
				Options op;
				op = new Options(opprice, current, writetxt("TXO001", opprice,
						false, abs));
				options[signalflag] = op;
				alertIn(op, false, abs, "min1");
				into = true;
			}
		}
		}
		if ((SGXTWGap() - SGXGapB) > 0 && outsell && intoflag && !close) {
			counterPos++;
			if (counter > countSize) {
				if (counterPos > (countSize * 3 / 4)) {
				makeprice(input, true);
				currentmulti(GetOpPrice.getprice(opprice, true));
				current = 1 * currentmulti;
				int abs = Math.abs(current);
				signalflag = 0;
				Options op;
				op = new Options(opprice, current, writetxt("TXO001", opprice,
						true, abs));
				options[signalflag] = op;
				alertIn(op, true, abs, "counter:" + counterPos);
				into = true;
				intoflag = false;
				} else {
					counter = 0;
					counterPos = 0;
				}
			}
		} else if ((SGXTWGap() + SGXGapB) < 0  && outsell && intoflag && !close) {
			counterPos--;
			if (counter > countSize) {
				if (counterPos < (countSize * -3 / 4)) {
				makeprice(input, false);
				currentmulti(GetOpPrice.getprice(opprice, false));
				current = -1 * currentmulti;
				int abs = Math.abs(current);
				signalflag = 0;
				Options op;
				op = new Options(opprice, current, writetxt("TXO001", opprice,
						false, abs));
				options[signalflag] = op;
				alertIn(op, false, abs, "counter:" + counterPos);
				into = true;
				intoflag = false;
				} else {
					counter = 0;
					counterPos = 0;
				}
			}
		}
	}
	/**
	 * 檢查一開盤是否有options背離現象，如果有，就買進。
	 * @param input
	 */
	private void scancheck(int input) {
		double i = new Integer(input).doubleValue();
		i = i / 100;
		int price = (int) Math.ceil(i);
		Double[] callprice = new Double[9];
		Double[] putprice = new Double[9];
		int y = 0;
		for(int x = price - 4; x <= price + 4; x++){
			callprice[y] = GetOpPrice.getprice(x * 100, true);
			putprice[y] = GetOpPrice.getprice(x * 100, false);
			y++;
		}
		y = 1;
		for(int x = price - 3; x <= price + 3; x++){
			double complex_price = x * 100 + callprice[y] - putprice[y];
			if (complex_price > (input + 25) || complex_price < (input - 25))
				scancheck1(x, y, callprice, putprice);
			y++;
		}
	}
	
	private void scancheck1(int x, int y, Double[] callprice, Double[] putprice) {
		if (!(callprice[y - 1] > callprice[y - 1] && callprice[y] > callprice[y + 1])) {
			opprice = x * 100;
			currentmulti(GetOpPrice.getprice(opprice, true));
			current = 1 * currentmulti;
			int abs = Math.abs(current);
			signalflag = 0;
			Options op;
			op = new Options(x * 100, current, writetxt("TXO001", opprice,
					true, abs));
			options[signalflag] = op;
			alertIn(op, true, abs, "背離現象買進CALL");
			into = true;
		} else if (!(putprice[y - 1] < putprice[y] && putprice[y] < putprice[y + 1])) {
			opprice = x * 100;
			currentmulti(GetOpPrice.getprice(opprice, false));
			current = -1 * currentmulti;
			int abs = Math.abs(current);
			signalflag = 0;
			Options op;
			op = new Options(x * 100, current, writetxt("TXO001", opprice,
					false, abs));
			options[signalflag] = op;
			alertIn(op, false, abs, "背離現象買進PUT");
			into = true;
		}
	}

	/**
	 * 1.一開盤，觀察價內第一檔，call + put > 120，代表有大波動。 2.call + put < 120
	 * 就算灌破上下緣，也是會回檔到平盤。 3.十一點及十二點以後，確認 call + put < 11X，代表會回檔。 4.確認那 call +
	 * put的decay速度越快，代表trend機率越低。
	 */

	/*private void closecheckin(int input) {
		int[] bound = new int[2];
		if (upbound == 0 && lowbound == 0)
			bound = getbound(input);
		upbound = bound[1];
		lowbound = bound[0];
		double upprice = GetOpPrice.getprice(lowbound, true);
		double lowprice = GetOpPrice.getprice(upbound, false);
		if (closeint == 1) {
			if ((upprice + lowprice) > 119) {
				c.alert(botname, Email, getNowTime() + " " + lowbound + "CALL! +"
						+ upbound + "PUT! 大於120");
				currentmulti = (int) ((upprice + lowprice) - 119);
				int callputflag = 0;
				if (Math.abs(upprice - lowprice) > 50) {
					if (upprice > lowprice)
						callputflag = 2;
					else if (upprice < lowprice)
						callputflag = 1;
				} else {
					if (upprice > lowprice)
						callputflag = 1;
					else if (upprice < lowprice)
						callputflag = 2;
				}
				if (callputflag == 1) {
					if (current < 0){
						int abs = Math.abs(current);
						Options op;
						op = new Options(opprice, 0, writetxt("TXO001", opprice, false, 0));
						totalsum(op,abs);
						alertOut(op, false, abs);
					}
					current = 1 * currentmulti;
					int abs = Math.abs(current);
					makeprice(input, true);
					signalflag = 0;
					Options op = new Options(opprice, current, writetxt("TXO001", opprice, true, abs));
					options[signalflag] = op;
					alertIn(op, true, abs);
				} else if (callputflag == 2) {
					if (current > 0){
						int abs = Math.abs(current);
						Options op;
						op = new Options(opprice, 0, writetxt("TXO001",
									opprice, true, 0));
						totalsum(op,abs);
						alertOut(op, true, abs);
					}
					current = -1 * currentmulti;
					int abs = Math.abs(current);
					makeprice(input, false);
					signalflag = 0;
					Options op = new Options(opprice, current, writetxt("TXO001", opprice, false, abs));
					options[signalflag] = op;
					alertIn(op, false, abs);
				}
			}
		} else if (closeint == 2) {
			if ((upprice + lowprice) > 109) {
				c.alert(botname, Email, getNowTime() + " " + lowbound + "CALL! +"
						+ upbound + "PUT! 大於110");
				currentmulti = (int) ((upprice + lowprice) - 109);
				int callputflag = 0;
				if (Math.abs(upprice - lowprice) > 50) {
					if (upprice > lowprice)
						callputflag = 2;
					else if (upprice > lowprice)
						callputflag = 1;
				} else {
					if (upprice > lowprice)
						callputflag = 1;
					else if (upprice > lowprice)
						callputflag = 2;
				}
				if (callputflag == 1) {
					if (current < 0){
						int abs = Math.abs(current);
						Options op;
						op = new Options(opprice, 0, writetxt("TXO001", opprice, false, 0));
						totalsum(op,abs);
						alertOut(op, false, abs);
					}
					current = 1 * currentmulti;
					int abs = Math.abs(current);
					makeprice(input, true);
					signalflag = 0;
					Options op = new Options(opprice, current, writetxt("TXO001", opprice, true, abs));
					options[signalflag] = op;
					alertIn(op, true, abs);
				} else if (callputflag == 2) {
					if (current > 0){
						int abs = Math.abs(current);
						Options op;
						op = new Options(opprice, 0, writetxt("TXO001",
									opprice, true, 0));
						totalsum(op,abs);
						alertOut(op, true, abs);
					}
					current = -1 * currentmulti;
					int abs = Math.abs(current);
					makeprice(input, false);
					signalflag = 0;
					Options op = new Options(opprice, current, writetxt("TXO001", opprice, false, abs));
					options[signalflag] = op;
					alertIn(op, false, abs);
				}
			} else if (current != 0) {
				int abs = Math.abs(current);
				if (current > 0) {
					Options op;
					op = new Options(opprice, 0, writetxt("TXO001", opprice, true, 0));
					totalsum(op,abs);
					alertOut(op, true, abs);
					current = 0;
				} else {
					Options op;
					op = new Options(opprice, 0, writetxt("TXO001", opprice, false, 0));
					totalsum(op,abs);
					alertOut(op, false, abs);
					current = 0;
				}
			}
		}
	}*/

	private void checkout(int input) {
		checkout1(input);
		HighMcheckout(input);
	}
	
	private void HighMcheckout(int input) {
		if (current > 0) {
			if (percent != 1) {
				if ((input <= (high - Gapvolout)) && hlflag == 1 && (SGXTWGap() - SGXGapB) < 0) { // 判斷是否高點回檔
					if (((SGXTWGap() - SGXGapA) < 0) || close) {
						int abs = Math.abs(current);
						Options op;
						op = new Options(opprice, 0, writetxt("TXO001",
								opprice, true, 0));
						totalsum(op, abs);
						alertOut(op, true, abs, "HighMcheckout");
						current = 0;
						higho = 0;
						lowo = 0;
						hlflag = 0;
						signalflag = signalflag + 1;
						if (signalflag > 2)
							signalflag = 1;
						if (Math.abs(nowpercent) > inPcentGap)
							outsell = false;
					}
				}
			}
		} else if (current < 0) {
			if (percent != -1) {
				if ((input >= (low + Gapvolout)) && hlflag == -1 && (SGXTWGap() + SGXGapB) > 0) { // 判斷是否低點回檔
					if (((SGXTWGap() + SGXGapA) > 0) || close) {
						int abs = Math.abs(current);
						Options op;
						op = new Options(opprice, 0, writetxt("TXO001",
								opprice, false, 0));
						totalsum(op, abs);
						alertOut(op, false, abs, "HighMcheckout");
						current = 0;
						higho = 0;
						lowo = 0;
						hlflag = 0;
						signalflag = signalflag + 1;
						if (signalflag > 2)
							signalflag = 1;
						if (Math.abs(nowpercent) > inPcentGap)
							outsell = false;
					}
				}
			}
		}
	}

	private void checkout1(int input) {
		if (current > 0) {
			if (percent != 1) {
				if ((SGXTWGap() + SGXGapC) < 0) {
					int abs = Math.abs(current);
					Options op;
					op = new Options(opprice, 0, writetxt("TXO001", opprice,
							true, 0));
					totalsum(op, abs);
					alertOut(op, true, abs, "checkout1");
					current = 0;
					higho = 0;
					lowo = 0;
					hlflag = 0;
					signalflag = signalflag + 1;
					if (signalflag > 2)
						signalflag = 1;
					if (Math.abs(nowpercent) > inPcentGap)
						outsell = false;
				}
			}
		} else if (current < 0) {
			if (percent != -1) {
				if ((SGXTWGap() - SGXGapC) > 0) {
					int abs = Math.abs(current);
					Options op;
					op = new Options(opprice, 0, writetxt("TXO001", opprice,
							false, 0));
					totalsum(op, abs);
					alertOut(op, false, abs, "checkout1");
					current = 0;
					higho = 0;
					lowo = 0;
					hlflag = 0;
					signalflag = signalflag + 1;
					if (signalflag > 2)
						signalflag = 1;
					if (Math.abs(nowpercent) > inPcentGap)
						outsell = false;
				}
			}
		}
	}

	void check_runtime() {
		java.util.Date now = new java.util.Date(); // 取得現在時間
		SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss E",
				java.util.Locale.TAIWAN);
		String sGMT = sf.format(now);
		int hour = Integer.valueOf(sGMT.substring(0, 2)).intValue();
		int min = Integer.valueOf(sGMT.substring(3, 5)).intValue();
		int sec = Integer.valueOf(sGMT.substring(6, 8)).intValue();
		CheckSGXTime(min); //在摩台結算會出現有部位卻停止 runflag = false; 因為13:30以後停止報價。
		if (current != 0) {
			if (hour > 12 && min > 28 && sec > 45) {
				if (close) {
					clear();
					runflag = false;
				}
			}
			if (hour > 12 && min > 43 && sec > 45) {
				clear();
				runflag = false;
			}
		} else {
			if (hour > 12 && min > 0 && (close || SGXclose)) {
				runflag = false;
			}
			if (hour > 12 && min > 20) {
				runflag = false;
			}
		}
		if (hour > 8 && min > 1 && sec > 0 && !isOpen) {
			isOpen = true;
			if (SGXindex != 0)
				runflag = true;
			else
				runflag = false;
		}
	}

	private String getNowTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		java.util.Date date = new java.util.Date();
		String datetime = dateFormat.format(date);
		return datetime;
	}

	private void clear() {
		if (current != 0) {
			double SGXPercent;
			if (SGXclose)
				SGXPercent = NextSGXTWGap();
			else
				SGXPercent = SGXTWGap();
			if (current > 0) {
				if (percent != 1) {
					if (close || ((SGXPercent - SGXGapL) < 0)) {
						int abs = Math.abs(current);
						Options op;
						op = new Options(opprice, 0, writetxt("TXO001",
								opprice, true, 0));
						totalsum(op, abs);
						alertOut(op, true, abs, "clear");
						current = 0;
					}
				}
			} else {
				if (percent != -1) {
					if (close || ((SGXPercent + SGXGapL) > 0)) {
						int abs = Math.abs(current);
						Options op;
						op = new Options(opprice, 0, writetxt("TXO001",
								opprice, false, 0));
						totalsum(op, abs);
						alertOut(op, false, abs, "clear");
						current = 0;
					}
				}
			}
		}
		runflag = false;
	}
	
	public void close(){
		if (!close) {
			if (current != 0) {
				try {
					FileOutputStream fos = new FileOutputStream("D:\\Dropbox\\Op.ser");
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(options[signalflag]);
					oos.close();
					
				} catch (Exception ex) {
					System.out.println("Exception thrown during writing Options: " + ex.toString());
				}
				txt.setOutput("OP留倉：" + options[signalflag].getOpprice() + " " + options[signalflag].getCurrent());
				c.alert(botname, Email, "OP留倉：" + options[signalflag].getOpprice() + " " + options[signalflag].getCurrent());
				f.alert(botname, fb, "OP留倉：" + options[signalflag].getOpprice() + " " + options[signalflag].getCurrent());
				}
		}
		c.alert(botname, Email, getNowTime() + " Trading System Stop!!");
		f.alert(botname, fb, getNowTime() + " Trading System Stop!!");
		p.logout();
		txt.setOutput("Win:" + win);
		txt.setOutput("Lost:" + lost);
		txt.setOutput("Total:" + total);
		txt.close();
	}
	
	private void alertIn(Options op, boolean callorput, int abs, String input) {
		if (callorput) {
			if (op.getSpouse() != null) {
				c.alert(botname, Email, getNowTime() + " Gap: " + (SGXTWGap() * 100) + "% 韓國:" + (kspercent * 100) + "% 買進 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口"
						+ " 賣出 " + op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口 " + input);
				f.alert(botname, getNowTime() + " 買進 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口"
						+ " 賣出 " + op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口 " + input);
				p.plurkAdd(getNowTime() + " 買進 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口" + " 賣出 "
						+ op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				cal.addEvent(getNowTime() + " 買進 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口" + " 賣出 "
						+ op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				txt.setOutput(getNowTime() + " 買進 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口" + " 賣出 "
						+ op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				System.out.println(getNowTime() + " 買進 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口"
						+ " 賣出 " + op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
			} else {
				c.alert(botname, Email, getNowTime() + " Gap: " + (SGXTWGap() * 100) + "% 韓國:" + (kspercent * 100) + "% 買進 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口 " + input);
				f.alert(botname, getNowTime() + " 買進 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口 " + input);
				p.plurkAdd(getNowTime() + " 買進 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口");
				cal.addEvent(getNowTime() + " 買進 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口");
				txt.setOutput(getNowTime() + " 買進 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口");
				System.out.println(getNowTime() + " 買進 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口");
			}
		} else {
			if (op.getSpouse() != null) {
				c.alert(botname, Email, getNowTime() + " Gap: " + (SGXTWGap() * 100) + "% 韓國:" + (kspercent * 100) + "% 買進 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口"
						+ " 賣出 " + op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口 " + input);
				f.alert(botname, getNowTime() + " 買進 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口"
						+ " 賣出 " + op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口 " + input);
				p.plurkAdd(getNowTime() + " 買進 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口" + " 賣出 "
						+ op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				cal.addEvent(getNowTime() + " 買進 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口" + " 賣出 "
						+ op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				txt.setOutput(getNowTime() + " 買進 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口" + " 賣出 "
						+ op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				System.out.println(getNowTime() + " 買進 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口"
						+ " 賣出 " + op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
			} else {
				c.alert(botname, Email, getNowTime() + " Gap: " + (SGXTWGap() * 100) + "% 韓國:" + (kspercent * 100) + "% 買進 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口 " + input);
				f.alert(botname, getNowTime() + " 買進 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口 " + input);
				p.plurkAdd(getNowTime() + " 買進 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口");
				cal.addEvent(getNowTime() + " 買進 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口");
				txt.setOutput(getNowTime() + " 買進 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口");
				System.out.println(getNowTime() + " 買進 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口");
			}
		}
	}
	
	private void alertOut(Options op, boolean callorput, int abs, String input) {
		if (callorput) {
			if (op.getSpouse() != null) {
				c.alert(botname, Email, getNowTime() + " Gap: " + (SGXTWGap() * 100) + "% 韓國:" + (kspercent * 100) + "% 賣出 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口"
						+ " 買進 " + op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口 " + input);
				f.alert(botname, getNowTime() + " 賣出 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口"
						+ " 買進 " + op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口 " + input);
				p.plurkAdd(getNowTime() + " 賣出 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口" + " 買進 "
						+ op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				cal.addEvent(getNowTime() + " 賣出 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口" + " 買進 "
						+ op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				txt.setOutput(getNowTime() + " 賣出 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口" + " 買進 "
						+ op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				System.out.println(getNowTime() + " 賣出 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口"
						+ " 買進 " + op.getSpouse().getOpprice() + "CALL! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
			} else {
				c.alert(botname, Email, getNowTime() + " Gap: " + (SGXTWGap() * 100) + "% 韓國:" + (kspercent * 100) + "% 賣出 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口 " + input);
				f.alert(botname, getNowTime() + " 賣出" + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口 " + input);
				p.plurkAdd(getNowTime() + " 賣出 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口");
				cal.addEvent(getNowTime() + " 賣出 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口");
				txt.setOutput(getNowTime() + " 賣出 " + opprice + "CALL! cost:"
						+ op.getPrice() + " " + abs + "口");
				System.out.println(getNowTime() + " 賣出 " + opprice
						+ "CALL! cost:" + op.getPrice() + " " + abs + "口");
			}
		} else {
			if (op.getSpouse() != null) {
				c.alert(botname, Email, getNowTime() + " Gap: " + (SGXTWGap() * 100) + "% 韓國:" + (kspercent * 100) + "% 賣出 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口"
						+ " 買進 " + op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口 " + input);
				f.alert(botname, getNowTime() + " 賣出" + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口"
						+ " 買進 " + op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口 " + input);
				p.plurkAdd(getNowTime() + " 賣出 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口" + " 買進 "
						+ op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				cal.addEvent(getNowTime() + " 賣出 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口" + " 買進 "
						+ op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				txt.setOutput(getNowTime() + " 賣出 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口" + " 買進 "
						+ op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
				System.out.println(getNowTime() + " 賣出 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口"
						+ " 買進 " + op.getSpouse().getOpprice() + "PUT! cost:"
						+ op.getSpouse().getPrice() + " " + abs + "口");
			} else {
				c.alert(botname, Email, getNowTime() + " Gap: " + (SGXTWGap() * 100) + "% 韓國:" + (kspercent * 100) + "% 賣出 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口 " + input);
				f.alert(botname, getNowTime() + " 賣出 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口 " + input);
				p.plurkAdd(getNowTime() + " 賣出 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口");
				cal.addEvent(getNowTime() + " 賣出 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口");
				txt.setOutput(getNowTime() + " 賣出 " + opprice + "PUT! cost:"
						+ op.getPrice() + " " + abs + "口");
				System.out.println(getNowTime() + " 賣出 " + opprice
						+ "PUT! cost:" + op.getPrice() + " " + abs + "口");
			}
		}
	}
	
	public void totalsum(Options op, int abs) {
		if (options[signalflag].getSpouse() != null) {
			if (op.getPrice() > options[signalflag].getPrice()) {
				win = win
						+ (((op.getPrice() - options[signalflag].getPrice()) - (op
								.getSpouse().getPrice() - options[signalflag]
								.getSpouse().getPrice())) * abs);
				total = total
						+ (((op.getPrice() - options[signalflag].getPrice()) - (op
								.getSpouse().getPrice() - options[signalflag]
								.getSpouse().getPrice())) * abs);
			} else {
				lost = lost
						+ (((options[signalflag].getPrice() - op.getPrice()) - (options[signalflag]
								.getSpouse().getPrice() - op.getSpouse()
								.getOpprice())) * abs);
				total = total
						- (((options[signalflag].getPrice() - op.getPrice()) - (options[signalflag]
								.getSpouse().getPrice() - op.getSpouse()
								.getOpprice())) * abs);
			}
		} else {
			if (op.getPrice() > options[signalflag].getPrice()) {
				win = win
						+ ((op.getPrice() - options[signalflag].getPrice()) * abs);
				total = total
						+ ((op.getPrice() - options[signalflag].getPrice()) * abs);
			} else {
				lost = lost
						+ ((options[signalflag].getPrice() - op.getPrice()) * abs);
				total = total
						- ((options[signalflag].getPrice() - op.getPrice()) * abs);
			}
		}
	}
	
	private double SGXTWGap(){
		return SGXpercent - nowpercent;
	}
	
	private void currentmulti(double input) {
		double multi = money / input;
		multi = Math.floor(multi);
		currentmulti = (int) multi;
	}
	
	private double NextSGXTWGap(){
		double SGXNextChange = GetOpPrice.getchange(NextSGXSymbol);
		double SGXNextPrepercent = GetOpPrice.getprice(NextSGXSymbol) - SGXNextChange;
		double SGXNextpercent = SGXNextChange / SGXNextPrepercent;
		return SGXNextpercent - nowpercent;
	}
	
	private void CheckSGXTime(int mm){
		if (SGXTime != null) {
			int min = Integer.valueOf(SGXTime.substring(3, 5)).intValue();
			if (mm > (min + 1) && runflag) {
				c.alert(botname, Email, getNowTime() + " Optionsbot SGXIndex Stop Error!!");
				f.alert(botname, fb, getNowTime() + " Optionsbot SGXIndex Stop Error!!");
				System.out.println("Optionsbot SGXIndex Stop Error!!");
				runflag = false;
			}
		}
	}
	
	private void setSGXTime(){
		java.util.Date now = new java.util.Date(); // 取得現在時間
		SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss E",
				java.util.Locale.TAIWAN);
		SGXTime = sf.format(now);
	}
	
	/*
	 * 權利金報價10點以下：0.1點(5元) 
	 * 權利金報價10點以上，50點以下：0.5點(25元)
	 * 權利金報價50點以上，500點以下：1點(50元)
	 * 權利金報價500點以上，1000點以下：5點(250元)
	 * */
	
	private double priceTrans(boolean buy, double price) {
		int updown = 0;
		if (buy)
			updown = 1;
		else
			updown = -1;
		if (price < 10) {
			price = price + (updown * 0.1);
		} else if (price < 50) {
			price = price + (updown * 0.5);
		} else if (price < 500) {
			price = price + (updown * 1);
		} else if (price < 1000) {
			price = price + (updown * 5);
		} else {
			price = price + (updown * 10);
		}
		return price;
	}

	private Integer[] fhw(Object in[]) {
		int size = in.length;
		Integer[] tmp = new Integer[size / 2];

		for (int i = 0; i < size; i += 2) {
			tmp[i / 2] = ((Integer) in[i] + (Integer) in[i + 1]) / 2;
		}
		return tmp;
	}

	private Integer[] dwt(Object in[]) {
		Integer[] temp = null;
		switch (size) {
		case 1600:
			temp = fhw(fhw(fhw(fhw(fhw(fhw(in))))));
			break;
		case 800:
			temp = fhw(fhw(fhw(fhw(fhw(in)))));
			break;
		case 400:
			temp = fhw(fhw(fhw(fhw(in))));
			break;
		case 200:
			temp = fhw(fhw(fhw(in)));
			break;
		case 100:
			temp = fhw(fhw(in));
			break;
		}
		return temp;
	}
	
	public void add(int input) {
		queue.add(input);
		if (queue.size() >= size) {
			Object[] I = queue.toArray();
			Integer[] w = dwt(I);
			ls = Arrays.asList(w);
			queue.remove();
		}
	}
	
	private int[] getQueryModel() {
		Object[] t = ls.toArray();
		Arrays.sort(t);
		List<Object> list = Arrays.asList(t);
		int min = (Integer) list.get(0);
		int max = (Integer) list.get(lsize - 1);
		int minpos = ls.lastIndexOf(min);
		int maxpos = ls.lastIndexOf(max);
		/*int q1 = ls.get(0);
		int q2 = ls.get(lsize/5);
		int q3 = ls.get(lsize/5*2);
		int q4 = ls.get(lsize/5*3);
		int q5 = ls.get(lsize/5*4);
		int q6 = ls.get(lsize-1);*/
		int vol = max - min;
		int[] pair = { 0, vol };
		if (vol > range) {
			if ((5 >= minpos) && (maxpos >= 20)) {
				pair[0] = 1;
			} else if ((5 >= maxpos) && (minpos >= 20)) {
				pair[0] = 2;
			}
			if ((16 <= minpos) && (minpos <= 20) && (maxpos >= 23)) {
				pair[0] = 3;
			} else if ((16 <= maxpos) && (maxpos <= 20) && (minpos >= 23)) {
				pair[0] = 4;
			}
		}
		return pair;
	}
}
/*
 * </PRE> </BODY> </HTML>
 */