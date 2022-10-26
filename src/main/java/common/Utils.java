package main.java.common;

import com.bluejungle.framework.crypt.IDecryptor;
import com.bluejungle.framework.crypt.IEncryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;

public class Utils {
	public static String encrypt(String text) {
		if (text == null || text.length() == 0) {
			return "";
		}
		IEncryptor encryptor = new ReversibleEncryptor();
		return encryptor.encrypt(text);
	}

	public static String decrypt(String text) {
		if (text == null || text.length() == 0) {
			return "";
		}
		IDecryptor decryptor = new ReversibleEncryptor();
		return decryptor.decrypt(text);
	}

	public static void main(String[] args) {
		System.out.println(Utils.encrypt("Sapnxd88"));
	}
}
