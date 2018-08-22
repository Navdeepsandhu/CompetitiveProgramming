package Problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class HuffmanEncoder {
	private class Node implements Comparable<Node> {
		Character data;
		int frequency;
		Node left;
		Node right;
		
		public int compareTo(Node o) {
			return this.frequency-o.frequency;
		}
	}
	
	
	HashMap<Character, String> encoder=new HashMap<>();
	HashMap<String, Character> decoder=new HashMap<>();
	
	public HuffmanEncoder(String blob) {
		
		HashMap<Character, Integer> map=new HashMap<>();
		
		for(int i=0; i<blob.length(); i++) {
			map.put(blob.charAt(i), map.containsKey(blob.charAt(i))? map.get(blob.charAt(i))+1: 1 );
		}
		
		
		PriorityQueue<Node> pqueue=new PriorityQueue<>();
		
		ArrayList<Character> al=new ArrayList<>(map.keySet());
		
		for(Character c: al) {
			Node temp=new Node();
			temp.data=c;
			temp.frequency=map.get(c);
			pqueue.add(temp);
		}
		
		while(pqueue.size()!=1) {
			Node one=pqueue.remove();
			Node two=pqueue.remove();
			Node temp=new Node();
			temp.frequency=one.frequency+two.frequency;
			temp.left=one;
			temp.right=two;
			pqueue.add(temp);
		}
		
		Node temp=pqueue.remove();
		
		traverse(temp,"");
		
	}
	
	private void traverse(Node last, String asf) {
		if(last.left==null && last.right==null) {
			encoder.put(last.data,asf);
			decoder.put(asf, last.data);
			
			return;
		}
		
		traverse(last.left,asf+"0");
		traverse(last.right, asf+"1");
	}
	
	public void encode(String str) {
		String encodedExp="";
		
		for(int i=0; i<str.length(); i++) {
			encodedExp+=encoder.get(str.charAt(i));
		}
		System.out.println(encodedExp);
	}
	
	public void decode(String str) {
		String decodedexp="";
		String prefix="";
		for(int i=0; i<str.length(); i++) {
			prefix+=str.charAt(i);
			if(decoder.containsKey(prefix)) {
				decodedexp+=decoder.get(prefix);
				prefix="";
			}
		}
		
		System.out.println(decodedexp);
	}

}
