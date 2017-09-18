import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.http.util.TextUtils;
import org.bouncycastle.util.encoders.Hex;

public class AddrMan {

	private static byte[][] sAddrs;
	private static int sCount;

	/**
	 * 导入升序的Base16的地址列表。
	 * 文件格式为一行一条地址记录，一条地址40个Base16的字符，读取时遇到空行结束读取。
	 * @param addrsFilePath 存放地址的文件路径。
	 * @param count 文件中地址条数。
	 * @throws IOException
	 */
	public static final void init(String addrsFilePath, int count) throws IOException {
		sAddrs = new byte[count][20];
		BufferedReader reader = new BufferedReader(new FileReader(new File(addrsFilePath)));
		String hexAddr;
		for (int i = 0; !TextUtils.isEmpty((hexAddr = reader.readLine())) && i < count; i++) {
			byte[] byteAddr = Hex.decode(hexAddr);
			sAddrs[i] = byteAddr;
			sCount = i + 1;
		}
		reader.close();
	}

	/**
	 * 检查地址是否在地址缓存中。
	 * 采用二分查找，非常快。
	 * @param tAddr 地址，20个byte的数组。
	 * @return 地址在缓存中存在则返回true，否则返回false；
	 */
	public static final boolean contain(byte[] tAddr) {
		int low = 0;
		int high = sCount - 1;
		int middle;
		while (low <= high) {
			middle = (low + high) / 2;
			byte[] mAddr = sAddrs[middle];
			int result = compareByte20(mAddr, tAddr);
			if (result == 0) {
				return true;
			} else if (result == 1) {
				high = middle - 1;
			} else {
				low = middle + 1;
			}
		}
		return false;
	}

	public static final int compareByte20(byte[] bytes1, byte[] bytes2) {
		for (int i = 0; i < 20; i++) {
			int b1 = bytes1[i] & 0xFF;
			int b2 = bytes2[i] & 0xFF;
			if (b1 > b2) {
				return 1;
			}
			if (b1 < b2) {
				return -1;
			}
		}
		return 0;
	}

	// test code
	public static void main(String[] args) {
		sAddrs = new byte[][] {
				new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
						(byte) 11, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 },
				new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
						(byte) 11, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 },
				new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
						(byte) 255, (byte) 11, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 },
				new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
						(byte) 255, (byte) 255, (byte) 11, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 },
				new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
						(byte) 255, (byte) 255, (byte) 255, (byte) 11, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 },
				new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
						(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 11, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 } };
		System.out.println("-----sAddr------------------------------");
		System.out.println("      " + Hex.toHexString(sAddrs[0]));
		System.out.println("      " + Hex.toHexString(sAddrs[1]));
		System.out.println("      " + Hex.toHexString(sAddrs[2]));
		System.out.println("      " + Hex.toHexString(sAddrs[3]));
		System.out.println("      " + Hex.toHexString(sAddrs[4]));
		System.out.println("      " + Hex.toHexString(sAddrs[5]));
		System.out.println("----------------------------------------");
		
		byte[] addr1 = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
				        (byte) 11, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
		System.out.println("addr1:" + Hex.toHexString(addr1));

		byte[] addr2 = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
				        (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 11, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
		System.out.println("addr2:" + Hex.toHexString(addr2));

		byte[] addr3 = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
				        (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 10, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
		System.out.println("addr3:" + Hex.toHexString(addr3));
		
		System.out.println("Contain addr1:" + contain(addr1));
		System.out.println("Contain addr2:" + contain(addr2));
		System.out.println("Contain addr3:" + contain(addr3));
	}

}
