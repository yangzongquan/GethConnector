import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.SQLException;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.jce.ECKeyPairGenerator;
import org.ethereum.crypto.jce.SpongyCastleProvider;
import org.ethereum.util.ByteUtil;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;

public class EthFinder_mem {

	public static void main(String[] args) throws IOException {
		if (args.length != 5) {
			System.out.println("Example: $java -jar xx.jar 2 10000 8 '/root/unique_addrs.txt' 8880000, "
					+ "2:number of threads, 10000: max number of key pair every thread('0' represent no limit), "
					+ "8: min same char, '/root/unique_addrs.txt' is addrs file path, 8880000: addrs count in file");
			return;
		}

		// 初始化参数
		final int threads = Integer.valueOf(args[0]);
		long args1 = Long.valueOf(args[1]);
		final long maxPerThread = args1 == 0 ? Long.MAX_VALUE : args1;
		final int minSame = Integer.valueOf(args[2]);
		final String addrsFilePath = args[3];
		final int addrsCount = Integer.valueOf(args[4]);

		// 读取地址文件
		AddrMan.init(addrsFilePath, addrsCount);
		
		// 启动工作线程
		final long start = System.currentTimeMillis();
		for (int i = 0; i < threads; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						start(maxPerThread, minSame);
					} catch (SQLException e) {
						e.printStackTrace();
						new IllegalStateException(e.getMessage());
					}
					System.out.println("elapse-time:" + (System.currentTimeMillis() - start) + "ms");
				}
			}).start();
		}
	}

	private static void start(long max, int minSame) throws SQLException {
        // 初始化密钥算法
	    SecureRandom secureRandom = new SecureRandom();
	    Provider provider = SpongyCastleProvider.getInstance();
	    KeyPairGenerator keyPairGen = ECKeyPairGenerator.getInstance(provider, secureRandom);

    	for (long i = 0; i < max; i++) {
    		// 生成密钥对
        	KeyPair keyPair = keyPairGen.generateKeyPair();
        	PublicKey pubKey = keyPair.getPublic();
        	ECPoint pub = ((BCECPublicKey) pubKey).getQ();
//        	byte[] byteAddr = Hex.decode("000e503b9424f095c253fe65ce89c4e8e4e5bf7a");
        	byte[] byteAddr = ECKey.computeAddress(pub);
            String hexAddr = Hex.toHexString(byteAddr);
            
            // 已存在的地址
            if (AddrMan.contain(byteAddr)) {
            	PrivateKey privKey = keyPair.getPrivate();
                String hexPriv = Hex.toHexString(ByteUtil.bigIntegerToBytes(((BCECPrivateKey) privKey).getD(), 32));
            	System.out.println("gEtH:" + hexAddr + ":" + hexPriv);
            }
            // 检查是不是连号地址
            else {
            	if (EthKeyGen.isGoodAddress(hexAddr, minSame)) {
	            	PrivateKey privKey = keyPair.getPrivate();
	                String hexPriv = Hex.toHexString(ByteUtil.bigIntegerToBytes(((BCECPrivateKey) privKey).getD(), 32));
	            	System.out.println("gOoD:" + hexAddr + ":" + hexPriv);
            	}
            }
		}
	}
	
}
