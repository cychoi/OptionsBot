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
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Options implements java.io.Serializable {
	/**
	 * opprice OP履約價
	 * current 口數
	 * price 成交價
	 * callorput 買權或賣權
	 * spouse 組合OP
	 */
	private static final long serialVersionUID = 1L;
	private double price;
	private int current;
	private int opprice;
	private boolean callorput;
	private Options spouse;

	public static void main(String args[]) {
		Options op = new Options(8800, 1, 100.0);
		Options op1 = new Options(8900, 1, 50.0);
		op.setSpouse(op1);
		try {
			FileOutputStream fos = new FileOutputStream("Opc.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(op);
			oos.close();
		} catch (Exception ex) {
			System.out.println("Exception thrown during writing Options: "
					+ ex.toString());
		}
	}

	public Options(int opprice, int current, double price) {
		this.opprice = opprice;
		this.current = current;
		if (current > 0)
			this.callorput = true;
		this.price = price;
	}

	public boolean isCallorput() {
		return callorput;
	}

	public void setCallorput(boolean callorput) {
		this.callorput = callorput;
	}

	public Options getSpouse() {
		return spouse;
	}

	public void setSpouse(Options value) {
		spouse = value;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getOpprice() {
		return opprice;
	}

	public void setOpprice(int opprice) {
		this.opprice = opprice;
	}

}
