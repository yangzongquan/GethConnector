import java.io.IOException;
import java.math.BigInteger;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;

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
		if (args.length != 3) {
			System.out.println("Example: $java -jar xx.jar 2 10000 8, 2:number of threads, 10000: max number of key pair every thread('0' represent no limit), 8: min same char");
			return;
		}

		final int threads = Integer.valueOf(args[0]);
		long args1 = Long.valueOf(args[1]);
		final long maxPerThread = args1 == 0 ? Long.MAX_VALUE : args1;
		final int minSame = Integer.valueOf(args[2]);

		final long start = System.currentTimeMillis();
		for (int i = 0; i < threads; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
//					final Web3j web3 = Web3j.build(new UnixIpcService("/Users/yang/Library/Ethereum/geth.ipc"));
					 final Web3j web3 = Web3j.build(new HttpService("http://localhost:8545"));

					EthGetBalance balance = null;
					DefaultBlockParameter param = DefaultBlockParameter.valueOf("latest");
					String addrPrfix = "0x";
					for (int i = 0; i < maxPerThread; i++) {
						String[] keypair = EthKeyGen.generate();
//						if (i % 9999 == 0) {
//							keypair[0] = "ca63cfefd225ea1436a88133d077e49b3d07989b";
//						}
						try {
							balance = web3.ethGetBalance(addrPrfix + keypair[0], param).send();
							if (!(balance.getBalance().compareTo(BigInteger.valueOf(0)) == 0)) {
								System.out.println(keypair[0] + ":" + keypair[1] + ":Balance:" + balance.getBalance());
							} else {
								if (EthKeyGen.isGoodAddress(keypair[0], minSame)) {
									System.out.println(keypair[0] + ":" + keypair[1]);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					System.out.println("elapse-time:" + (System.currentTimeMillis() - start) + "ms");
				}
			}).start();
		}
	}

}
