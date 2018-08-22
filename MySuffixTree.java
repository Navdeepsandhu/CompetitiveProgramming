/*
 * Rule 1: Start the new phase by adding new character at the end of current edges.
 * Rule 2: if for a particular suffix of the current phase, path do not exist, create one.
 * Rule 3: If path exists for a particular suffix, break. This would mark end of phase  
 * 
 * Tricks:
 * Skip/ count
 * Edge level compression: On each edge, label is described by range.
 * Rule 3 extension is show stopper
 * Global end for leaves. So that with just end++, we can make rule 1 extension.
 * 
 * Suffix link: For every internal node V with path tA, where t is a single character and A is 
 * zero or more characters, there is another internal node sv with A as prefix of its edge label, 
 * sv is suffix link of V.
 * 
 * if A is zero character, internal node's suffix link is root.
 * 
 *Rules: if active length is zero, then the extension would start from root.
 *
 *when rule 3 extension happens, we just increment active edge and active length by 1.
 * 
 *If active length is non zero, our extension starts with active point, (from active node,
 *we move in the direction of edge with value as active edge and we move in active length no of
 *steps into it.)
 *		if next character to active point is same as current character then walkdown
 *		2 cases of walkdown: if there is no internal node between active point char and next char
 *							just do active length++;
 *							else{
 *								active node becomes that internal node
 *								active edge becomes starting index of that edge label
 *								active length becomes 1
 *							}	  
 *
 *
 * Active Points and rule 2 extension:
 * We create a new internal node and follow suffix link to start next extension. 
 * 
 * if(active node is not root){
 * 	we follow active node's suffix link and do not change active edge and active length
 * }
 * else
 * 	we increment active edge and decrement active length. 
 * 
 * 
 * Active point rules
 * 1) If rule 3 extension is applied then active length will increment by 1 if active length
 * is not greater then length of path on edge.
 * 2) If rule 3 extension is applied and if active length gets greater than length path of edge
 * then change active node, active edge and active length
 * 3) If active length is 0 then always start looking for the character from root.
 * 4) If rule 2 extension is applied and if active node is root then active edge is active edge + 1
 * and active length is active length -1
 * 5) If rule 2 extension is applied and if active node is not root then follow suffix link and 
 * make active node as suffix link and do no change 
 * anything.
 */

package Strings;

import java.util.ArrayList;
import java.util.List;

import Strings.MySuffixTree.EndOfPathException;

public class MySuffixTree {

	 public static void main(String args[]){
		 	//convert the given string into character array
	        MySuffixTree st = new MySuffixTree("xyzxyaxyz".toCharArray());
	        //build function called on mississippi
	        st.build();
	        st.dfsTraversal();
	        System.out.println(st.validate());
	    }
	    
	    private SuffixNode root;
	    private Active active;
	    private int remainingSuffixCount;
	    private End end;
	    private char input[];
	    private static char UNIQUE_CHAR = '$';
	    
	    public MySuffixTree(char input[]){
	        this.input = new char[input.length+1];
	        for(int i=0; i < input.length; i++){
	            this.input[i] = input[i];
	        }
	        this.input[input.length] = UNIQUE_CHAR;
	    }
	    
	    public void build(){
	        root = SuffixNode.createNode(1, new End(0));
	        root.index = -1;
	        active = new Active(root);
	        this.end = new End(-1);
	        //loop through string to start new phase
	        for(int i=0; i < input.length; i++){
	            startPhase(i);
	        }

	        if (remainingSuffixCount != 0) {
	            System.out.print("Something wrong happened");
	        }
	        //finally walk the tree again and set up the index.
	        setIndexUsingDfs(root, 0, input.length);
	    }
	    
	    private void startPhase(int i){
	        //set lastCreatedInternalNode to null before start of every phase.
	        SuffixNode lastCreatedInternalNode = null;
	        //global end for leaf. Does rule 1 extension as per trick 3 by incrementing end.
	        end.end++;
	        
	        //these many suffixes need to be created.
	        remainingSuffixCount++;
	        while(remainingSuffixCount > 0){
	            //if active length is 0 then look for current character from root.
	            if(active.activeLength == 0){
	                //if current character from root is not null then increase active length by 1 
	                //and break out of while loop. This is rule 3 extension and trick 2 (show stopper)
	                if(selectNode(i) != null){
	                    active.activeEdge = selectNode(i).start;
	                    active.activeLength++;
	                    break;
	                } //create a new leaf node with current character from leaf. This is rule 2 extension.
	                else {
	                    root.child[input[i]] = SuffixNode.createNode(i, end);
	                    remainingSuffixCount--;
	                }
	            } else{
	                //if active length is not 0 means we are traversing somewhere in middle. So check if next character is same as
	                //current character. 
	                try {
	                    char ch = nextChar(i);
	                    //if next character is same as current character then do a walk down. This is again a rule 3 extension and
	                    //trick 2 (show stopper).
	                    if(ch == input[i]){
	                        //if lastCreatedInternalNode is not null means rule 2 extension happened before this. Point suffix link of that node
	                        //to selected node using active point.
	                        //TODO - Could be wrong here. Do we only do this if when walk down goes past a node or we do it every time.
	                        if(lastCreatedInternalNode != null){
	                            lastCreatedInternalNode.suffixLink = selectNode();
	                        }
	                        //walk down and update active node if required as per rules of active node update for rule 3 extension.
	                        walkDown(i);
	                        break;
	                    }
	                    else {
	                        //next character is not same as current character so create a new internal node as per 
	                        //rule 2 extension.
	                        SuffixNode node = selectNode();
	                        int oldStart = node.start;
	                        node.start = node.start + active.activeLength;
	                        //create new internal node
	                        SuffixNode newInternalNode = SuffixNode.createNode(oldStart, new End(oldStart + active.activeLength - 1));

	                        //create new leaf node
	                        SuffixNode newLeafNode = SuffixNode.createNode(i, this.end);

	                        //set internal nodes child as old node and new leaf node.
	                        newInternalNode.child[input[newInternalNode.start + active.activeLength]] = node;
	                        newInternalNode.child[input[i]] = newLeafNode;
	                        newInternalNode.index = -1;
	                        active.activeNode.child[input[newInternalNode.start]] = newInternalNode;

	                        //if another internal node was created in last extension of this phase then suffix link of that
	                        //node will be this node.
	                        if (lastCreatedInternalNode != null) {
	                            lastCreatedInternalNode.suffixLink = newInternalNode;
	                        }
	                        //set this guy as lastCreatedInternalNode and if new internalNode is created in next extension of this phase
	                        //then point suffix of this node to that node. Meanwhile set suffix of this node to root.
	                        lastCreatedInternalNode = newInternalNode;
	                        newInternalNode.suffixLink = root;

	                        //if active node is not root then follow suffix link
	                        if(active.activeNode != root){
	                            active.activeNode = active.activeNode.suffixLink;
	                        }
	                        //if active node is root then increase active index by one and decrease active length by 1
	                        else{
	                            active.activeEdge = active.activeEdge  + 1;
	                            active.activeLength--;
	                        }
	                        remainingSuffixCount--;
	                    }
	           
	                } catch (EndOfPathException e) {

	                    //this happens when we are looking for new character from end of current path edge. Here we already have internal node so
	                    //we don't have to create new internal node. Just create a leaf node from here and move to suffix new link.
	                    SuffixNode node = selectNode();
	                    node.child[input[i]] = SuffixNode.createNode(i, end);
	                    if (lastCreatedInternalNode != null) {
	                        lastCreatedInternalNode.suffixLink = node;
	                    }
	                    lastCreatedInternalNode = node;
	                    //if active node is not root then follow suffix link
	                    if(active.activeNode != root){
	                        active.activeNode = active.activeNode.suffixLink;
	                    }
	                    //if active node is root then increase active index by one and decrease active length by 1
	                    else{
	                        active.activeEdge = active.activeEdge + 1;
	                        active.activeLength--;
	                    }
	                    remainingSuffixCount--;
	                }
	            }
	        }
	    }
	    
	    private void walkDown(int index){
	        SuffixNode node = selectNode();
	        //active length is greater than path edge length.
	        //walk past current node so change active point.
	        //This is as per rules of walk down for rule 3 extension.
	        if(diff(node) < active.activeLength){
	            active.activeNode = node;
	            active.activeLength = active.activeLength - diff(node);
	            active.activeEdge = node.child[input[index]].start;
	        }else{
	            active.activeLength++;
	        }
	    }
	    
	    //find next character to be compared to current phase character.
	    private char nextChar(int i) throws EndOfPathException{
	        SuffixNode node = selectNode();
	        if(diff(node) >= active.activeLength){
	            return input[active.activeNode.child[input[active.activeEdge]].start + active.activeLength];
	        }
	        if(diff(node) + 1 == active.activeLength ){
	            if(node.child[input[i]] != null){
	                return input[i];
	            }
	        }
	        else{
	            active.activeNode = node;
	            active.activeLength = active.activeLength - diff(node) -1;
	            active.activeEdge = active.activeEdge + diff(node)  +1;
	            return nextChar(i);
	        }
	        
	        throw new EndOfPathException();
	    }
	    
	    public static class EndOfPathException extends Exception{
	        
	    }
	    
	    private SuffixNode selectNode(){
	        return active.activeNode.child[input[active.activeEdge]];
	    }
	    
	    private SuffixNode selectNode(int index){
	        return active.activeNode.child[input[index]];
	    }
	    
	    
	    private int diff(SuffixNode node){
	        return node.end.end - node.start;
	    }
	  
	    private void setIndexUsingDfs(SuffixNode root,int val, int size){
	        if(root == null){
	            return;
	        }
	        
	        val += root.end.end - root.start + 1;
	        if(root.index != -1){
	            root.index = size - val;
	            return;
	        }
	        
	        for(SuffixNode node : root.child){
	            setIndexUsingDfs(node, val, size);
	        }
	    }
	    
	    /**
	     * Do a DFS traversal of the tree.
	     */
	    public void dfsTraversal(){
	        List<Character> result = new ArrayList<>();
	        for(SuffixNode node : root.child){
	            dfsTraversal(node, result);
	        }
	    }
	    
	    private void dfsTraversal(SuffixNode root, List<Character> result){
	        if(root == null){
	            return;
	        }
	        if(root.index != -1){
	            for(int i=root.start; i <= root.end.end; i++){
	                result.add(input[i]);
	            }
	            result.stream().forEach(System.out::print);
	            System.out.println(" " + root.index);
	            for(int i=root.start; i <= root.end.end; i++){
	                result.remove(result.size()-1);
	            }
	            return;
	        }
	        
	        for(int i=root.start; i <= root.end.end; i++){
	            result.add(input[i]);
	        }
	        
	        for(SuffixNode node : root.child){
	            dfsTraversal(node, result);
	        }
	        
	        for(int i=root.start; i <= root.end.end; i++){
	            result.remove(result.size()-1);
	        }
	       
	    }
	    
	    /**
	     * Do validation of the tree by comparing all suffixes and their index at leaf node.
	     */
	    private boolean validate(SuffixNode root, char[] input, int index, int curr){
	        if(root == null){
	            System.out.println("Failed at " + curr + " for index " + index);
	            return false;
	        }
	        
	        if(root.index != -1){
	            if(root.index != index){
	                System.out.println("Index not same. Failed at " + curr + " for index " + index);
	                return false;        
	            }else{
	                return true;
	            }
	        }
	        if(curr >= input.length){
	            System.out.println("Index not same. Failed at " + curr + " for index " + index);
	            return false;        
	        }
	        
	        SuffixNode node = root.child[input[curr]];
	        if(node == null){
	            System.out.println("Failed at " + curr + " for index " + index);
	            return false;
	        }
	        int j = 0;
	        for(int i=node.start ; i <= node.end.end; i++){
	            if(input[curr+j] != input[i] ){
	                System.out.println("Mismatch found " + input[curr+j] + " " + input[i]);
	                return false;
	            }
	            j++;
	        }
	        curr += node.end.end - node.start + 1;
	        return validate(node, input, index, curr);
	    }
	    
	    public boolean validate(){
	        for(int i=0; i < this.input.length; i++){
	            if(!validate(this.root, this.input, i, i)){
	                System.out.println("Failed validation");
	                return false;
	            }
	        }
	        return true;
	    }
	}

	class SuffixNode{
	    
	    private SuffixNode(){
	    }
	    
	    private static final int TOTAL = 256;
	    SuffixNode[] child = new SuffixNode[TOTAL];
	    
	    int start;
	    End end;
	    int index;
	    
	    SuffixNode suffixLink;
	    
	    public static SuffixNode createNode(int start, End end){
	        SuffixNode node = new SuffixNode();
	        node.start = start;
	        node.end = end;
	        return node;
	    }

	    @Override
	    public String toString() {
	        StringBuffer buffer = new StringBuffer();
	        int i=0;
	        for(SuffixNode node : child){
	            if(node != null){
	                buffer.append((char)i + " ");
	            }
	            i++;
	        }
	        return "SuffixNode [start=" + start + "]" + " " + buffer.toString();
	    }

}
	
	class End{
	    public End(int end){
	        this.end = end;
	    }
	    int end;
	}

	class Active{
	    Active(SuffixNode node){
	        activeLength = 0;
	        activeNode = node;
	        activeEdge = -1;
	    }
	    
	    @Override
	    public String toString() {
	        
	        return "Active [activeNode=" + activeNode + ", activeIndex="
	                + activeEdge + ", activeLength=" + activeLength + "]";
	    }

	    SuffixNode activeNode;
	    int activeEdge;
	    int activeLength;
	}	
