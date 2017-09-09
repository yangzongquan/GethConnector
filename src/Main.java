import java.io.IOException;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.WindowsIpcService;

/**
 * http://www.ethdocs.org/en/latest/connecting-to-clients/index.html
 * https://github.com/web3j/web3j
 * https://web3j.readthedocs.io/en/latest/getting_started.html
 * https://github.com/ethereum/wiki/wiki/JSON-RPC
 * https://mvnrepository.com/
 *
 */
public class Main {

	
	
	public static void main(String[] args) throws IOException {
		
		Web3j web3 = Web3j.build(new WindowsIpcService("/path/to/namedpipefile"));
//		Web3j web3 = Web3j.build(new HttpService("http://76.246.236.118:8545"));
		EthGetBalance e = web3.ethGetBalance("0xca63cfefd225ea1436a88133d077e49b3d07989b", DefaultBlockParameter.valueOf("latest")).send();

		System.out.println(e.getBalance());
	}
	
	
	
}
