package Problem;

public class Client {

	public static void main(String[] args) {
		HuffmanEncoder hec=new HuffmanEncoder("aaaaaaaaaaaaaaabdbbcdddddd");
		hec.encode("aaaaaabbbbbccd");
		hec.decode("11111100100100100100100000001");
	}

}
