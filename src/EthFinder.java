import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.jce.ECKeyPairGenerator;
import org.ethereum.crypto.jce.SpongyCastleProvider;
import org.ethereum.util.ByteUtil;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

/**
 * http://www.ethdocs.org/en/latest/connecting-to-clients/index.html
 * https://github.com/web3j/web3j
 * https://web3j.readthedocs.io/en/latest/getting_started.html
 * https://github.com/ethereum/wiki/wiki/JSON-RPC
 * https://mvnrepository.com/
 *
 */
public class EthFinder {

	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.out.println("Example: $java -jar xx.jar 2 10000 8 '10.10.22.92:3306', 2:number of threads, 10000: max number of key pair every thread('0' represent no limit), 8: min same char");
			return;
		}

		final int threads = Integer.valueOf(args[0]);
		long args1 = Long.valueOf(args[1]);
		final long maxPerThread = args1 == 0 ? Long.MAX_VALUE : args1;
		final int minSame = Integer.valueOf(args[2]);
		final String host = args[3];

		final long start = System.currentTimeMillis();
		for (int i = 0; i < threads; i++) {
			final int dbIndex = i + 1;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						startByMysql(host, dbIndex, maxPerThread, minSame);
					} catch (SQLException e) {
						e.printStackTrace();
						new IllegalStateException(e.getMessage());
					}
					System.out.println("elapse-time:" + (System.currentTimeMillis() - start) + "ms");
				}
			}).start();
		}
	}

	private static void startByMysql(String host, int dbIndex, long max, int minSame) throws SQLException {
		// 初始化mysql连接
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + "/eth_" + dbIndex + "?user=root&password=123456&useSSL=false");
        PreparedStatement ps = conn.prepareStatement("select addr from eth_" + dbIndex + ".account where addr = ?");

        // 初始化密钥算法
	    SecureRandom secureRandom = new SecureRandom();
	    Provider provider = SpongyCastleProvider.getInstance();
	    KeyPairGenerator keyPairGen = ECKeyPairGenerator.getInstance(provider, secureRandom);

    	for (long i = 0; i < max; i++) {
    		// 生成密钥对
        	KeyPair keyPair = keyPairGen.generateKeyPair();
        	PublicKey pubKey = keyPair.getPublic();
        	ECPoint pub = ((BCECPublicKey) pubKey).getQ();
//            if (pubKey instanceof BCECPublicKey) {
//                
//            } else if (pubKey instanceof ECPublicKey) {
//                pub = ECKey.extractPublicKey((ECPublicKey) pubKey);
//            }
            String hexAddr = Hex.toHexString(ECKey.computeAddress(pub));
            // 查询地址是否已经在数据库
            ps.setString(1, hexAddr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
            	PrivateKey privKey = keyPair.getPrivate();
                String hexPriv = Hex.toHexString(ByteUtil.bigIntegerToBytes(((BCECPrivateKey) privKey).getD(), 32));
            	System.out.println("gEtH:" + hexAddr + ":" + hexPriv);
            }
            // 检查是不是连号地址
            else if (EthKeyGen.isGoodAddress(hexAddr, minSame)) {
            	PrivateKey privKey = keyPair.getPrivate();
                String hexPriv = Hex.toHexString(ByteUtil.bigIntegerToBytes(((BCECPrivateKey) privKey).getD(), 32));
            	System.out.println("gOoD:" + hexAddr + ":" + hexPriv);
            }
            rs.close();
		}
	}
	
	public static void startByGeth(long max, int minSame) {
//		final Web3j web3 = Web3j.build(new UnixIpcService("/Users/yang/Library/Ethereum/geth.ipc"));
		 final Web3j web3 = Web3j.build(new HttpService("http://localhost:8545"));

		EthGetBalance balance = null;
		DefaultBlockParameter param = DefaultBlockParameter.valueOf("latest");
		String addrPrfix = "0x";
		for (int i = 0; i < max; i++) {
			String[] keypair = EthKeyGen.generate();
			try {
				balance = web3.ethGetBalance(addrPrfix + keypair[0], param).send();
				if (!(balance.getBalance().compareTo(BigInteger.valueOf(0)) == 0)) {
					System.out.println(keypair[0] + ":" + keypair[1] + ":Balance:" + balance.getBalance());
				} else if (EthKeyGen.isGoodAddress(keypair[0], minSame)) {
					System.out.println(keypair[0] + ":" + keypair[1]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
